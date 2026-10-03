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

    def test_remote_arbitrary_command_is_rejected(self):
        code = mayra_agent.start_pairing()
        self.assertTrue(mayra_agent.owner_approve(code))
        token = mayra_agent.approve_pairing(code)
        self.assertTrue(token)
        denied = mayra_agent.execute("RUN:whoami")
        self.assertFalse(denied["ok"])
        self.assertEqual(denied["error"], "Command not allowed")
        mayra_agent.revoke_session()


if __name__ == "__main__":
    unittest.main()
