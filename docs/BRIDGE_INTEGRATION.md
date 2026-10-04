# Mayra optional bridge integration

Mayra keeps its own authenticated Android↔Windows command transport and can use optional local tools when installed on Windows.

- **scrcpy**: screen/control bridge. Mayra can discover it and, after owner-authenticated command routing, start it with `BRIDGE_START_SCREEN`.
- **LocalSend**: local file-sharing bridge. Mayra can discover it and start it with `BRIDGE_START_FILE_SHARE`.
- **ADB**: capability discovery remains read-only; it is not exposed as arbitrary shell execution.
- Third-party tools are optional. If a tool is missing, Mayra reports the missing capability and keeps standalone operation.
- No arbitrary executable path is accepted from the phone. Only the fixed allowlisted executable names are resolved through the Windows PATH.
- LAN mode remains opt-in and the current Mayra transport is token-authenticated but not encrypted.

The bridge commands are part of the Mayra PC command catalog and are covered by Windows-agent unit tests.
