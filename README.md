# Mayra

Mayra is a work-in-progress **Android-only** personal AI assistant.

## Current status

- Android debug APK builds through GitHub Actions.
- Android project structure and package identity are checked in CI before Android tests/build.
- Android voice commands support Bengali, Hindi, and English language selection, with owner authorization required for background commands.
- Mayra operates independently on the Android phone; the project scope is Android-only.
- No installation/bootstrap password is used. Owner verification uses the phone's supported biometric/device-credential mechanism.

## Developer checks

Run the Android unit tests and build:

```sh
gradle test --no-daemon
gradle assembleDebug --no-daemon
```

The Android release gate is: unit tests must pass, the debug APK must build, and the APK artifact must upload successfully.

## Safety notes

- Owner authorization is required for sensitive Mayra commands.
- Do not store passwords, OTPs, or sensitive personal files in this prototype.
- Build success is not the same as installation or device testing.
