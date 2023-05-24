# MahoMaps v1

![logo](res/splash.png)

Yandex.Maps client for MIDP2-capable devices.

## Features

- Geolocation detection
- Map caching
- Search
- Organization cards
- Building a route

## System requirements

- CLDC 1.1
- MIDP 2.0
- JSR-75
- A lot of RAM
- **A lot** of space in persistent memory
- JSR-179 (optional)
- HTTPS support (optional)

### Recommended system tweaks

On symbian 9.x, it's recommended to install a patch to allow access to file system without confirmation popups.

### How to make geolocation work?

- On Symbian 9.x / ^3 disable **all location sources except unassisted GPS**.
- **Disable A-GPS**. On non-symbian devices it may be called as "geolocation server", "internet support", "supplement server" or something like that.
- Let your device perform a cold start: request geolocation in any app (MahoMaps, Google Maps, Ovi Maps, Sports tracker, anything else) and keep the device under clear sky. It is desirable that the horizon is visible. Wait until it catches sattelites. May take a while.
- Once you are sure that GPS receiver works okay, you may try to enable A-GPS to increase accuracy and startup time. We recommend using `supl.google.com` as supplement server. `supl.nokia.com` is dead!
- If you experience issues, try disabling A-GPS.
- If you device can't catch sattelites even in good weather, check that its GPS receiver is operable.

## Tested systems

- Symbian ^3/Anna/Belle/Refresh/FP1/FP2
- Symbian 9.2-9.4
- Bada 1.1

## Supported emulators

- [KEmulator nnmod](https://nnp.nnchan.ru/kem/)
- [J2ME Loader](https://github.com/nikita36078/J2ME-Loader)

## Contact us

[Telegram chat](https://t.me/nnmidletschat)
