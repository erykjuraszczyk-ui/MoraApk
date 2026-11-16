# REDMAGIC Performance Core 1.2.0

## Purpose

Provide an on-demand, local performance recommendation for MORA on Android phones, including REDMAGIC 7. This is an app-level policy only; it does not tune or override the phone's firmware.

## Inputs

- Android thermal status from `PowerManager` where supported;
- low-memory signal and available RAM from `ActivityManager`;
- system battery-saver state from `PowerManager`;
- battery percentage from the sticky battery status broadcast.

## Profiles

1. **Thermal protection** — selected when Android reports severe or higher thermal status.
2. **Low memory** — selected when Android reports low memory or available RAM is below the conservative 700 MB threshold.
3. **Battery saver** — selected when system battery saver is active or battery is at/below 20%.
4. **Balanced** — default when none of the above signals apply.

Priority is thermal, low-memory, battery saver, then balanced.

## User control

The user runs the analysis manually in Settings. MORA presents the profile and recommendation. The user must tap Apply before MORA changes its own reduced-motion preference. This preference affects MORA's hologram animation only; it does not change Android system settings.

## Safety boundaries

- No CPU/GPU frequency changes.
- No thermal service overrides.
- No force-stopping other apps.
- No background polling.
- No automatic changes to Android battery saver.
- No claim that battery temperature is CPU temperature.
- Data is read locally and is not uploaded by this module.
