# Phone-only validation — REDMAGIC 7

MORA can be built in GitHub Actions and installed/tested directly on the Android phone. A computer, USB cable, ADB, and Android Studio are not required for normal user validation.

## On-device diagnostics

Settings → Diagnostyka REDMAGIC 7 → Skanuj moje urządzenie.

The report reads public Android APIs and displays:
- manufacturer and device model;
- Android release and SDK API level;
- supported CPU ABIs;
- total and currently available RAM;
- Android low-memory state;
- battery percentage and charging state;
- battery temperature (not CPU temperature);
- Android thermal status (a qualitative system level, not a numeric SoC temperature);
- default display refresh rate.

The report is generated locally and is not uploaded automatically. It deliberately excludes serial numbers, account IDs, precise location, and a full installed-app inventory.

## Build and download on the phone

1. Open the repository's Actions page in the GitHub mobile app or browser.
2. Open the newest successful **Android CI** run for branch `feature/redmagic7-fan-update-1.1.0`.
3. Open Artifacts and download the debug APK artifact.
4. Extract the ZIP using the phone's file manager.
5. Install the APK. Debug builds use the application ID suffix `.debug`, so they install alongside the release/factory application and do not replace it.
6. Open MORA → Ustawienia → Diagnostyka REDMAGIC 7 → Skanuj moje urządzenie.

## Safety and interpretation

- Do not uninstall or alter the factory Mora app.
- Debug APK is for testing and is not a signed public release.
- Battery temperature is not processor temperature.
- Android thermal status is categorical; the device may not expose exact component temperatures.
- Refresh rate reports the default display mode and does not prove that the app is rendering at that rate.
- The report is not a hardware benchmark or a thermal stress test.
