# MORA REDMAGIC 7 — Fan Update 1.1.0

## Integration decision

This release uses the **companion-app** model:

- Keep the factory REDMAGIC/Mora application installed and unchanged.
- Install MORA AI as a separate Android application.
- Do not replace, re-sign, patch, or modify the factory APK.
- Do not assume the factory application's package ID, exported activities, deep links, or private APIs.
- Integrate only through documented Android mechanisms and user-approved actions.

## Release scope

Version 1.1.0 is an incremental compatibility and reliability release, not a firmware update.

1. Identify the installed companion app and installed factory app by package name.
2. Read version and signing-certificate fingerprints for diagnostics.
3. Keep the factory app optional: MORA AI must remain usable if it is absent.
4. Do not launch external activities until the real package ID and exported entry point have been verified on-device.
5. Keep microphone use user-initiated; no hidden or continuous background listening.
6. Keep API credentials in Android Keystore-backed encrypted storage; never commit credentials.
7. Respect Android battery, thermal, notification, and background-execution policies.

## REDMAGIC 7 validation

Test on the target phone and record:

- Android version and REDMAGIC OS build;
- companion package ID, version, and signing-certificate SHA-256;
- factory Mora package ID, version, and signing-certificate SHA-256, if installed;
- microphone and notification permission behavior;
- voice recognition and TTS availability for Polish;
- screen rotation, process recreation, and app restart;
- battery use and thermal behavior during a 15-minute voice/animation session.

Do not infer that the device has a particular Android release or that an undocumented REDMAGIC API is available.

## Upgrade safety

An in-place Android update requires the same application ID and a compatible signing certificate. If either differs, Android will reject the update; do not uninstall the factory app to work around this. Preserve the app's data and use the companion installation instead.

## Build and verification

Use the repository's pinned Gradle wrapper and JDK version. Run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Install the debug build only for development testing. A distributable release must be signed with the project's own release key. Never use the factory application's signing key.

## Known unknowns

The repository README does not document the factory Mora package ID, exported components, or a supported integration API. These must be inspected on the physical device before implementing direct app-to-app navigation. Until then, the integration remains deliberately decoupled.
