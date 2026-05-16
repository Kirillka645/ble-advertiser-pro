# BLE Advertiser Pro

A Material 3 Android app for crafting and broadcasting Bluetooth Low Energy
advertisements that mimic popular consumer devices (Apple Continuity, Microsoft
Swift Pair, Samsung Easy Setup, Google Fast Pair, and more).

> **Research / educational use only.** Use only on devices you own or with the
> explicit permission of their owners. Broadcasting deceptive BLE advertisements
> to third-party devices may violate local laws, app-store policies and your
> network operator's terms of service. The first launch of the app forces you
> to acknowledge this disclaimer before any features are unlocked.

This project is a fork-and-rewrite of [Simon Dankelmann's Bluetooth-LE-Spam][upstream],
licensed under the GPLv3. See `LICENSE` for the full text and the upstream
project for the original implementation and contributors.

[upstream]: https://github.com/simondankelmann/Bluetooth-LE-Spam

## What is improved

- **Material 3 UI overhaul** — proper Material 3 colour tokens, dark theme
  parity and edge-to-edge layout.
- **Material You / dynamic colors** — on Android 12+ the app automatically
  picks up the user's wallpaper palette; can be disabled in Settings.
- **First-launch research disclaimer** — blocking dialog that has to be
  accepted before the app can be used.
- **Auto-stop timer** — configure a number of minutes after which advertising
  is automatically stopped to prevent accidentally leaving it running.
- **Burst mode preference** — UI toggle for cycling at minimum interval.
- **Expanded device catalogues** for every supported manufacturer:
  - **Apple Continuity** — added AirPods 4, AirPods 4 ANC, AirPods Pro 2
    (USB-C), AirPods Max (USB-C), Beats Solo 4 and Beats Pill across the
    "New Device" and "Not Your Device" prompts.
  - **Microsoft Swift Pair** — replaced the `Device 1`...`Device 10`
    placeholders with realistic Surface, Microsoft and Xbox device names.
  - **Samsung Easy Setup** — added Buds 2 Pro / Buds 3 / Buds 3 Pro shells
    and Galaxy Watch FE / Watch 7 / Watch Ultra entries.
  - **Google Fast Pair Phone Setup** — added Galaxy S24/Z Flip 6/Z Fold 6,
    Pixel 9 family, Nothing Phone (2a)/(3) and OnePlus 12/12R seeds.

## Build

```bash
./gradlew assembleDebug
```

Java 21 and the Android SDK with `compileSdk = 36` / `minSdk = 26` are required.
The CI workflow under `.github/workflows/build.yml` runs the same command on
push and uploads the unsigned APK.

## Permissions

The app requests `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`,
`POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` and (only on Android &le; 11)
location permissions, which Google requires for BLE scanning.

## License

GPL-3.0-or-later, identical to the upstream project. See `LICENSE`.
