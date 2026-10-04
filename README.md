# Mayra

Mayra is a work-in-progress personal assistant for Android and Windows.

## Current status

- Android debug APK builds through GitHub Actions.
- Android project structure and package identity are checked in CI before Android tests/build.
- Android build configuration is currently Gradle Kotlin DSL: `settings.gradle.kts`, root `build.gradle.kts`, and `app/build.gradle.kts`.
- Android voice commands support Bengali, Hindi, and English language selection, with owner authorization required for background commands.
- The Windows agent remains a **local-only MVP** with a strict command allowlist. Its implemented local allowlist includes `PING`, `OPEN_NOTEPAD`, `OPEN_CALCULATOR`, and selected Windows settings.
- Android contains the phone-side pairing/session architecture, one-time code flow, persistent session token, owner approval gates, and LAN transport helpers. A Windows companion must still be configured and verified on a real Windows 10 machine before phone↔PC operation can be declared production-ready.
- Do not expose an unverified Windows agent to an untrusted network.

## Windows setup

See [Windows setup instructions in Bengali](WINDOWS_SETUP_BN.md). Launch helper: [windows/start_mayra.bat](windows/start_mayra.bat).

## Developer checks

Run the Windows-agent unit tests with Python 3:

```sh
python -m unittest discover -s tests -v
```

The tests are safe to run on a development computer; they do not open apps or make network connections.

The Android release gate is: unit tests must pass, the debug APK must build, and the APK artifact must upload successfully. A failed gate is not counted as a completed project step.

## Safety notes

- Only the explicit allowlist in `mayra_agent.py` can be executed.
- Do not store passwords, OTPs, or sensitive personal files in this prototype.
- Build success is not the same as installation or device testing.
