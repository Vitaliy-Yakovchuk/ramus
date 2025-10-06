# Ramus for macOS (Optimized Fork)

This is a macOS‑optimized fork of the Ramus IDEF0/DFD modeling tool. It focuses on native macOS integration and packaging. Windows support is not a goal here and may not work properly.

Download: the latest macOS DMG is available in this repository’s GitHub Releases section.

License: see LICENSE in this repository (unchanged from upstream).

## Requirements (macOS)

- macOS with developer tools (preinstalled utilities: `sips`, `iconutil`).
- JDK 21+ with `jdeps`, `jlink`, and `jpackage` (full JDK, not JRE). For best results, install a standard Temurin/Oracle JDK.
- Optional (icon conversion fallback): `dwebp` from the `webp` package (e.g., `brew install webp`).

Tip: This project supports local overrides without changing your shell’s `JAVA_HOME`.

## Quick Start

1) Build a macOS .app for quick testing

```
./gradlew :local-client:createMacApp
open local-client/build/mac-app/Ramus.app
```

2) Build a standalone DMG (recommended)

```
./gradlew :local-client:macDmg
open dest-macos
```

The DMG contains a standalone app that does not require users to install Java.

Alternatively, download the latest DMG from this repository’s GitHub Releases section.

## Configuration (Optional)

If you keep multiple JDKs, add a local file to point packaging to a specific JDK. These files are ignored by git.

- Create `gradle-local.properties` in the repo root (example):

```
# Full JDK used for packaging (jdeps, jlink, jpackage)
packagingJavaHome=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home

# Disable jlink and bundle a full JDK instead (bigger DMG but simpler)
# packagingUseJlink=false

# Optional explicit module path for jlink (if you want to use modules from a different JDK)
# packagingJmodsPath=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/jmods
```

Alternatively, you can set `org.gradle.java.home` in `gradle.properties` (also ignored by git) if you want Gradle itself to run on a particular JDK.

## Build Tasks Summary

- `:local-client:createMacApp`
  - Creates a dev `.app` under `local-client/build/mac-app/Ramus.app`.
  - Uses a generated `.icns` and sets Dock icon flags for a native look.

- `:local-client:macDmg`
  - Full packaging pipeline: generates `.icns` → optional `jlink` runtime → `jpackage` DMG.
  - Outputs to `dest/macos/`.
  - If `jlink` isn’t available, it automatically bundles the full JDK at `packagingJavaHome`.

- `:local-client:makeIcns`
  - Converts `packaging/macos/AppIcon.appiconset` into a `.icns` using `sips`/`iconutil`.

## Running From Source (Optional)

You can still run directly from sources:

```
./gradlew :local-client:runLocal
```

Note: the dev run uses your local Java installation; for the full native experience use the `.app` or DMG.

## Contributing

Contributions are very welcome—bug reports, macOS improvements, docs, and packaging tweaks. Please open issues or pull requests.

## What’s new in 2.0.2

- macOS app bundle and DMG packaging via Gradle + jpackage.
- Proper Dock icon and Info.plist; icons are sourced from `packaging/macos/AppIcon.appiconset` and converted to `.icns` during build (uses macOS `sips`/`iconutil`; falls back to `dwebp` if needed).
- Uses the macOS system menu bar (`apple.laf.useScreenMenuBar=true`).
- macOS keyboard shortcuts use the Command key (⌘) via the platform menu shortcut mask (e.g., ⌘S, ⌘O, ⌘Z, ⌘⇧S, etc.).
- Standalone distribution: bundles a Java runtime. Optionally uses `jlink` to create a minimized runtime; falls back to bundling the full JDK if `jlink` isn’t available.
- Modernized build/toolchain: project compiles for Java 17 (upstream used Java 8) and uses a recent Gradle (8.x). Packaging targets JDK 21 for the bundled runtime.
