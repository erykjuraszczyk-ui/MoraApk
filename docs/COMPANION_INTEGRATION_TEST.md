# Companion integration — device test procedure

## Purpose

Validate optional, user-initiated handoff from MORA AI to a factory Mora launcher app on REDMAGIC 7. The factory app is never modified.

## Implementation

- `CompanionAppDiscovery` searches only launcher activities that match the word "Mora".
- Android package visibility is declared using a MAIN/LAUNCHER intent query.
- No `QUERY_ALL_PACKAGES` permission is requested.
- The app does not assume a package ID or private vendor API.
- Launch is initiated only after an explicit user action in the app UI.

## Device validation

1. Install and open MORA AI.
2. Confirm that the original Mora app remains installed and operates normally.
3. Call `CompanionAppDiscovery(context).findMoraApps()`.
4. If the factory app appears, record its package name and display label.
5. Ask the user to select the intended Mora entry if more than one appears.
6. Call `launch(selected.packageName)` only after the user taps the launch control.
7. Verify that Android opens the expected factory app.
8. Repeat with the factory app disabled or absent; MORA AI must continue working.
9. Verify behavior after device restart and app update.

## Acceptance criteria

- No package ID is hard-coded.
- No background launch or hidden activity occurs.
- Missing/invisible factory app is a normal state, not a fatal error.
- Factory Mora data and APK signature remain unchanged.
- MORA AI remains independently usable.

## Limitation

This module provides discovery and launcher handoff only. It does not grant access to private app data, internal services, alarms, widgets, or vendor-specific APIs. Those require a documented public integration interface from the app vendor.
