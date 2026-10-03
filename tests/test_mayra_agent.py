import unittest
from unittest.mock import patch

import mayra_agent


class MayraAgentTests(unittest.TestCase):
    def test_today_code_has_expected_date_format(self):
        self.assertRegex(mayra_agent.today_code(), r"^MAYRA-\d{8}$")

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


if __name__ == "__main__":
    unittest.main()
