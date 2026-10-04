# Mayra Windows 10 PC Companion

> **Current implementation status (2026-10-04):** this file describes the target architecture. The current Windows agent is local-only, binds to loopback, and does not yet provide end-to-end phone-to-PC pairing or remote commands. Do not expose it to LAN/Internet until authenticated TLS pairing and tests are complete.

Mayra is split into two cooperating applications:

- Android: Mayra Mobile, the owner's control interface.
- Windows: Mayra PC Companion, the authenticated local control endpoint.

## Control model

Phone command -> authenticated Mayra session -> capability check -> allowlisted PC command -> Windows result -> phone.

Wi-Fi or hotspot reachability is never authorization.

First pairing requires the owner-approved pairing flow. A successfully paired device may reconnect without repeating the initial approval until the session/trust is revoked or expires.

## Initial PC command surface

The first companion command layer covers:

- connection/status check
- Notepad
- Calculator
- Windows Settings
- Network Settings
- Display Settings
- Sound Settings
- read-only PC security status
- session revocation

Future modules can add files, clipboard, media, browser automation, and other owner-approved capabilities through explicit command IDs. No arbitrary shell/PowerShell execution is exposed by the Mayra protocol.

## Device media

The architecture reserves explicit capabilities for:

- front camera
- rear camera
- microphone
- speaker/audio

These require the corresponding Android permissions and active user-visible sessions. They are not implemented merely by declaring a capability.

## Recovery

Lost-phone recovery is limited to legitimate owner-authenticated recovery operations. Mayra never bypasses Android or Windows passwords, biometrics, lock screens, or other security controls.

## Windows 10

Windows 10 is the primary target for the companion in this project. Actual compatibility and security status must be verified on the owner's physical Windows 10 machine before release.

## Release rule

The PC companion is not release-ready until:

1. Android unit tests pass.
2. Windows agent tests pass.
3. Phone-to-PC pairing works on Wi-Fi and hotspot.
4. Reconnect and revoke work.
5. Each exposed command is tested on the target Windows 10 machine.
6. Camera, microphone and speaker paths are tested on real hardware where enabled.
7. Final CI is green.
8. Real-device end-to-end validation is completed.
