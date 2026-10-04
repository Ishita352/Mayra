# Mayra

Mayra is a work-in-progress personal assistant for Android and Windows.

## Current status

- Android debug APK builds through GitHub Actions.
- Android voice commands recognize Bengali, English, and Hindi phrases for phone actions and planned computer actions.
- The Windows agent is a **local-only MVP**. Its implemented local allowlist includes `PING`, `OPEN_NOTEPAD`, `OPEN_CALCULATOR`, and opening Windows/Network/Display/Sound Settings. Other declared capabilities remain unavailable until their modules are implemented.
- Phone-to-Windows pairing and remote commands are **not implemented yet**.
- Do not expose the Windows agent to a network. It binds to `127.0.0.1` intentionally until authenticated TLS pairing is implemented.

## Windows setup

See [Windows setup instructions in Bengali](WINDOWS_SETUP_BN.md). Launch helper: [windows/start_mayra.bat](windows/start_mayra.bat).

## Developer checks

Run the Windows-agent unit tests with Python 3:

```sh
python -m unittest discover -s tests -v
```

The tests are safe to run on a development computer; they do not open apps or make network connections.

## Safety notes

- Only the explicit allowlist in `mayra_agent.py` can be executed.
- Do not store passwords, OTPs, or sensitive personal files in this prototype.
- Build success is not the same as installation or device testing.
