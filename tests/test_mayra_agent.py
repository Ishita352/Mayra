import json
import socket
import unittest
from unittest.mock import patch

import mayra_agent


class MayraAgentTests(unittest.TestCase):
    def test_ping_is_allowed(self):
        result = mayra_agent.execute("PING")
        self.assertTrue(result["ok"])
        self.assertIn("online", result["message"])

    def test_unknown_commands_are_rejected(self):
        for command in ("", "DELETE_FILES", "RUN:whoami", None, {"command": "PING"}):
            with self.subTest(command=command):
                result = mayra_agent.execute(command)
                self.assertFalse(result["ok"])
                self.assertEqual(result["error"], "Command not allowed")

    @patch("mayra_agent.platform.system", return_value="Linux")
    def test_windows_commands_do_not_run_on_non_windows(self, _system):
        self.assertEqual(
            mayra_agent.execute("OPEN_NOTEPAD"),
            {"ok": False, "error": "This action is Windows-only"},
        )
        self.assertEqual(
            mayra_agent.execute("OPEN_CALCULATOR"),
            {"ok": False, "error": "This action is Windows-only"},
        )
        self.assertEqual(
            mayra_agent.execute("OPEN_WINDOWS_SETTINGS"),
            {"ok": False, "error": "This action is Windows-only"},
        )
        self.assertEqual(
            mayra_agent.execute("OPEN_NETWORK_SETTINGS"),
            {"ok": False, "error": "This action is Windows-only"},
        )
        self.assertEqual(
            mayra_agent.execute("OPEN_DISPLAY_SETTINGS"),
            {"ok": False, "error": "This action is Windows-only"},
        )
        self.assertEqual(
            mayra_agent.execute("OPEN_SOUND_SETTINGS"),
            {"ok": False, "error": "This action is Windows-only"},
        )

    def _handle_payload(self, payload):
        server_side, client_side = socket.socketpair()
        try:
            client_side.sendall(payload)
            mayra_agent.handle_connection(server_side)
            response = client_side.recv(4096)
            return json.loads(response.decode("utf-8"))
        finally:
            server_side.close()
            client_side.close()

    def test_non_loopback_bind_requires_explicit_opt_in(self):
        with patch.object(mayra_agent, "HOST", "0.0.0.0"), patch.object(mayra_agent, "ALLOW_LAN", False):
            with self.assertRaises(SystemExit):
                mayra_agent.main()

    def test_non_loopback_bind_is_allowed_with_explicit_trusted_lan_opt_in(self):
        with patch.object(mayra_agent, "HOST", "0.0.0.0"), patch.object(mayra_agent, "ALLOW_LAN", True):
            self.assertTrue(mayra_agent.ALLOW_LAN)

    def test_invalid_json_is_rejected(self):
        self.assertEqual(
            self._handle_payload(b"{not-json"),
            {"ok": False, "error": "Invalid request"},
        )

    def test_non_object_json_is_rejected(self):
        self.assertEqual(
            self._handle_payload(b'["PING"]'),
            {"ok": False, "error": "Invalid request"},
        )

    def test_oversized_request_is_rejected(self):
        self.assertEqual(
            self._handle_payload(b"x" * (mayra_agent.MAX_REQUEST_BYTES + 1)),
            {"ok": False, "error": "Request too large"},
        )

    def test_pairing_requires_owner_approval(self):
        code = mayra_agent.start_pairing()
        self.assertEqual(
            bool(mayra_agent.approve_pairing(code)),
            False,
        )
        self.assertTrue(mayra_agent.owner_approve(code))
        token = mayra_agent.approve_pairing(code)
        self.assertTrue(token)
        mayra_agent.revoke_session()

    def test_phone_endpoint_requires_authenticated_session(self):
        mayra_agent.start_pairing()
        self.assertFalse(mayra_agent.register_phone("192.168.1.10", 8766))
        self.assertTrue(mayra_agent.owner_approve(mayra_agent.pairing_code()))
        token = mayra_agent.approve_pairing(mayra_agent.pairing_code() or "")
        self.assertTrue(token)
        self.assertTrue(mayra_agent.register_phone("192.168.1.10", 8766, token))
        self.assertEqual(mayra_agent.phone_endpoint(), ("192.168.1.10", 8766))
        mayra_agent.revoke_session()
        self.assertIsNone(mayra_agent.phone_endpoint())

    @patch("mayra_agent.platform.system", return_value="Windows")
    @patch("mayra_agent.os.startfile")
    def test_browser_remote_control_is_executable(self, startfile, _system):
        self.assertTrue(mayra_agent.execute("OPEN_BROWSER")["ok"])
        self.assertTrue(mayra_agent.execute("OPEN_BROWSER:https://example.com")["ok"])
        self.assertEqual(startfile.call_args_list[0].args[0], "https://www.google.com")
        self.assertEqual(startfile.call_args_list[1].args[0], "https://example.com")

    @patch("mayra_agent.platform.system", return_value="Windows")
    @patch("mayra_agent.os.startfile")
    def test_browser_remote_control_rejects_unsafe_url(self, startfile, _system):
        result = mayra_agent.execute("OPEN_BROWSER:file:///C:/secret.txt")
        self.assertFalse(result["ok"])
        self.assertIn("http:// or https://", result["error"])
        startfile.assert_not_called()

    @patch("mayra_agent.platform.system", return_value="Windows")
    def test_media_remote_controls_are_executable_with_windows_api(self, _system):
        import sys
        fake_user32 = type("FakeUser32", (), {
            "keybd_event": staticmethod(lambda *args: None)
        })()
        fake_windll = type("FakeWindll", (), {"user32": fake_user32})()
        fake_ctypes = type("FakeCtypes", (), {"windll": fake_windll})()
        with patch.dict(sys.modules, {"ctypes": fake_ctypes}):
            for command in ("MEDIA_PLAY_PAUSE", "MEDIA_NEXT", "MEDIA_PREVIOUS"):
                with self.subTest(command=command):
                    result = mayra_agent.execute(command)
                    self.assertTrue(result["ok"])
                    self.assertIn("Media command sent", result["message"])


    def test_bridge_capabilities_is_read_only_and_safe(self):
        with patch("mayra_agent.shutil.which", side_effect=lambda name: ("C:\\tools\\" + name) if name in {"scrcpy", "adb"} else None):
            result = mayra_agent.execute("BRIDGE_CAPABILITIES")
        self.assertTrue(result["ok"])
        self.assertTrue(result["components"]["scrcpy"])
        self.assertTrue(result["components"]["adb"])
        self.assertFalse(result["components"]["localsend"])
        self.assertEqual(result["design"]["screen_control"], "scrcpy-compatible")

    @patch("mayra_agent.platform.system", return_value="Windows")
    @patch("mayra_agent.subprocess.Popen")
    @patch("mayra_agent.shutil.which")
    def test_optional_bridges_start_only_from_allowlisted_paths(self, which, popen, _system):
        def fake_which(name):
            return {"scrcpy": "C:\\tools\\scrcpy.exe", "localsend": "C:\\tools\\localsend.exe"}.get(name)
        which.side_effect = fake_which
        self.assertTrue(mayra_agent.execute("BRIDGE_START_SCREEN")["ok"])
        self.assertTrue(mayra_agent.execute("BRIDGE_START_FILE_SHARE")["ok"])
        self.assertEqual(popen.call_count, 2)
        self.assertEqual(popen.call_args_list[0].args[0], ["C:\\tools\\scrcpy.exe"])
        self.assertEqual(popen.call_args_list[1].args[0], ["C:\\tools\\localsend.exe"])

    @patch("mayra_agent.platform.system", return_value="Windows")
    @patch("mayra_agent.shutil.which", return_value=None)
    def test_optional_bridge_reports_missing_install_without_process_execution(self, which, _system):
        result = mayra_agent.execute("BRIDGE_START_SCREEN")
        self.assertFalse(result["ok"])
        self.assertIn("not installed", result["error"])

    def test_remote_arbitrary_command_is_rejected(self):
        code = mayra_agent.start_pairing()
        self.assertTrue(mayra_agent.owner_approve(code))
        token = mayra_agent.approve_pairing(code)
        self.assertTrue(token)
        denied = mayra_agent.execute("RUN:whoami")
        self.assertFalse(denied["ok"])
        self.assertEqual(denied["error"], "Command not allowed")
        mayra_agent.revoke_session()

    def test_quick_owner_link_auto_pairs_with_one_time_code(self):
        mayra_agent.start_pairing()
        code = mayra_agent.quick_pair_code()
        self.assertIsNotNone(code)
        token = mayra_agent.quick_pair("gopal-owner-test", code)
        self.assertTrue(token)
        self.assertTrue(mayra_agent.authenticated(token))
        self.assertIsNone(mayra_agent.quick_pair_code())
        mayra_agent.revoke_session()

    def test_persistent_login_survives_process_restart(self):
        import os
        import tempfile

        with tempfile.TemporaryDirectory() as temp_dir:
            old_state_file = mayra_agent.STATE_FILE
            try:
                mayra_agent.STATE_FILE = os.path.join(temp_dir, "windows_session.json")
                mayra_agent.revoke_session()
                mayra_agent.start_pairing()
                self.assertTrue(mayra_agent.owner_approve(mayra_agent.pairing_code()))
                token = mayra_agent.approve_pairing(mayra_agent.pairing_code() or "")
                self.assertTrue(token)
                self.assertTrue(mayra_agent.register_phone("192.168.1.20", 8766, token))

                # Simulate a Windows shutdown/restart by clearing in-memory state,
                # while keeping the persistent session file.
                mayra_agent._session_token = None
                mayra_agent._phone_endpoint = None
                mayra_agent.load_persistent_state()

                self.assertTrue(mayra_agent.authenticated(token))
                self.assertEqual(mayra_agent.phone_endpoint(), ("192.168.1.20", 8766))
            finally:
                mayra_agent.revoke_session()
                mayra_agent.STATE_FILE = old_state_file

    def test_quick_owner_link_rejects_wrong_code(self):
        mayra_agent.start_pairing()
        code = mayra_agent.quick_pair_code()
        self.assertIsNotNone(code)
        self.assertIsNone(mayra_agent.quick_pair("gopal-owner-test", "00000000"))
        self.assertIsNotNone(mayra_agent.quick_pair_code())
        mayra_agent.revoke_session()


if __name__ == "__main__":
    unittest.main()
