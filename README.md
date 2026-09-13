# Ramus

**Java-based IDEF0 & DFD modeller.** Build business-process models in the IDEF0
and DFD notations, keep them in git, and edit them by hand.

<img width="1792" alt="The Ramus application window" src="https://user-images.githubusercontent.com/2261228/69039713-23c56d00-09f5-11ea-99c5-b6714efe3037.png">

<img width="1792" alt="Another view of the Ramus application window" src="https://user-images.githubusercontent.com/2261228/69039723-27f18a80-09f5-11ea-9a8d-508069ce7bbd.png">

Original project by Vitaliy Yakovchuk and Oleksiy Chizhevskiy. This repository
is a **modified fork** of it, and the source of the site at
<https://ramussoftware.com/> — see
[Changes in this fork](#changes-in-this-fork) and [License](#license).

---

## Contents

- [Download](#download)
- [Quick start](#quick-start)
- [Project format](#project-format)
- [Building](#building)
- [Documentation](#documentation)
- [Changes in this fork](#changes-in-this-fork)
- [Contributing](#contributing)
- [License](#license)

---

## Download

Packaged builds are on the
[Releases page](https://github.com/Vitaliy-Yakovchuk/ramus/releases). The newest
one is 2.0.2, from 2023: a single Windows installer, `ramus-2.0.2-setup.exe`,
produced by the old NSIS path. It predates the text project format and looks
for a system Java 6 while installing.

Nothing is published for macOS yet. Both the DMG and the current Windows
installer are built by CI and attached to the release of the `v*` tag that
triggered it, so until the next such tag they have to be built from source —
one command each, see [macOS application and DMG](#macos-application-and-dmg)
and [Windows installer](#windows-installer). To run the application rather than
package it, start at [Quick start](#quick-start).

---

## Quick start

You need a JDK and a desktop session. Gradle itself is not required — the
wrapper downloads it.

```bash
git clone https://github.com/Vitaliy-Yakovchuk/ramus.git
cd ramus
./gradlew runLocal
```

The first run fetches Gradle and roughly 180 MB of dependencies; later runs
start in seconds.

Open a model straight away instead of going through the dialog:

```bash
./gradlew runLocal -Popen="dest/doc/en/Enterprise activity.rsf"
```

Sample models live in `dest/doc/en/` and `dest/doc/ru/`.

**JDK version.** Tested on Temurin 21; JDK 17 or newer is expected. The build
pins no toolchain, so sources compile against whatever JDK runs Gradle.
Packaging a macOS DMG additionally needs a full JDK 21+ with `jdeps`, `jlink`
and `jpackage`.

**No JDK on the machine?** See [Docker](#docker) below — it builds and runs
without installing anything but Docker itself.

---

## Project format

A Ramus project is a **directory of YAML files**, not a binary blob:

```
Model.ramus/
├── project.ramus          entry point: schema version, plugins, counters
├── attributes.yaml        every attribute of the model
├── qualifiers/            one file per qualifier
├── properties/            model settings
├── attachments/           user attachments (reports, files)
├── .gitignore
└── .local/                UI state; excluded from version control
```

Three consequences worth knowing:

**It goes into git as it is.** Saving an unchanged model rewrites no file, so
`git status` stays quiet until the model itself actually changes. There are no
save timestamps in the files, deliberately — they would make every save look
like a change.

**It is meant to be edited by hand.** The YAML dialect is restricted on
purpose: block style, sorted keys, quoted strings, no anchors or aliases, no
line wrapping. One line in the file is one line of the value, so `sed` works
and a diff points at the change rather than the paragraph around it. Editing
values is safe; inventing identifiers is not — add new elements in the
application, then edit them in the files.

**`project.ramus` is the entry point.** A desktop can associate a file with an
application but not a directory, so that file carries the
`application-version`, `minimum-version` and required `plugins`. The
application accepts either the directory or this file anywhere a project path
is expected.

### The older `.rsf` format

`.rsf` — a ZIP of XML table dumps — **opens but is never written back**.
Saving an opened `.rsf` prompts for a new project name, so the migration is
explicit and visible.

Batch conversion without launching the application:

```bash
./gradlew :ramus-core-demo:rsfToYaml -Prsf=<file.rsf> -Pout=<directory>
./gradlew :ramus-core-demo:yamlToRsf -Pin=<directory> -Prsf=<file.rsf>
```

The reverse converter exists only for exchanging models with older builds.

Full reference: **[docs/PROJECT_FORMAT.md](docs/PROJECT_FORMAT.md)**.

---

## Building

### Runnable JAR

```bash
./gradlew :local-client:shadowJar
java -jar local-client/build/libs/ramus.jar
```

### Docker

Builds in a container, so no JDK on the host:

```bash
docker build --target artifacts --output out .   # out/ramus.jar
docker build --target test .                     # run the test suite
docker build -t ramus .                          # image that runs the GUI
```

Running the GUI from the container needs an X11 socket and an auth cookie —
the recipe, the CI notes and the reasoning behind the Dockerfile are in
[docs/DOCKER.md](docs/DOCKER.md).

### macOS application and DMG

```bash
./packaging/macos/build-dmg.sh          # one command: checks, builds, reports
```

The script picks a JDK (21 or newer, `--jdk PATH` overrides it), runs the
Gradle pipeline, and prints where the image landed and how to install it. It
refuses to run anywhere but macOS, because `jpackage`, `iconutil` and `sips`
exist only there.

Under the hood it is still Gradle, and the tasks can be called directly:

```bash
./gradlew :local-client:createMacApp     # dev bundle, local-client/build/mac-app/Ramus.app
./gradlew :local-client:macDmg           # standalone DMG in dest/macos/
```

`macDmg` runs the full pipeline: `.icns` from
`packaging/macos/AppIcon.appiconset` → `jlink` runtime → `jpackage`. The
resulting DMG bundles a Java runtime, so its users need no Java. If `jlink` is
unavailable the full JDK is bundled instead — larger, but it works.

**The DMG is tied to one architecture**, because the runtime inside it is: a
build made on Apple silicon does not run on an Intel Mac. The architecture is
part of the file name (`Ramus-2.0.2-arm64.dmg`), and
[`.github/workflows/macos-dmg.yml`](https://github.com/Vitaliy-Yakovchuk/ramus/blob/master/.github/workflows/macos-dmg.yml)
builds both on GitHub's runners — on a tag push it attaches them to the
release. No Mac needed to publish a build.

**Installing.** Open the DMG, drag Ramus into Applications. Nothing else — the
Java runtime is inside the bundle.

The application is not signed with an Apple Developer ID, so a Mac that
downloaded it from the internet quarantines it and reports that "Ramus is
damaged". It is not; macOS says that about everything unsigned. Either open it
once through the context menu (right-click → Open → Open), or clear the flag:

```bash
xattr -dr com.apple.quarantine /Applications/Ramus.app
```

See [Download](#download) for what is published today.

Pointing packaging at a specific JDK, without touching your shell's
`JAVA_HOME`: create `gradle-local.properties` in the repository root (it is
gitignored).

```properties
packagingJavaHome=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
# packagingUseJlink=false          # bundle the full JDK instead of a jlink image
# packagingJmodsPath=…/jmods       # explicit module path for jlink
# packagingLocales=en,uk,ru,pl     # locale data kept in the jlink runtime
```

The bundled runtime keeps locale data for English, Ukrainian and Russian only —
the three the interface is translated into in full. A German translation is
almost complete and an Albanian one covers a couple of bundles; add `de` or
`sq` to `packagingLocales` when packaging for them. Without the locale data,
dates and Cyrillic sorting fall back to the root locale, and with all of it the
image grows by about ten megabytes; `packagingLocales` is the dial between the
two.

### Windows installer

```powershell
.\packaging\windows\build-installer.ps1     # one command: checks, builds, reports
```

The script checks the environment (Windows, a full JDK 21+, WiX Toolset 3) and
runs the Gradle pipeline, which can also be called directly:

```powershell
.\gradlew.bat :local-client:winInstaller                        # MSI in dest\windows\
.\gradlew.bat :local-client:winInstaller -PpackagingWinType=exe # EXE instead
```

Building needs [WiX Toolset 3](https://github.com/wixtoolset/wix3/releases)
(`choco install wixtoolset`): jpackage from JDK 21 drives WiX 3, support for
WiX 4 and 5 arrived only in JDK 24. Like the DMG, the installer can only be
built on the system it targets —
[`.github/workflows/windows-installer.yml`](https://github.com/Vitaliy-Yakovchuk/ramus/blob/master/.github/workflows/windows-installer.yml)
does it on GitHub's runners, and on a tag push attaches the result to the
release.

**Installing.** Double-click, and that is the whole procedure:

- the Java runtime is inside the package, nothing else to install;
- it installs into the user profile, so no administrator rights are needed;
- a Start menu shortcut appears, and `.ramus` and `.rsf` files open on
  double-click;
- the next version replaces this one instead of installing beside it — that is
  what the fixed `winUpgradeUuid` in `local-client/build.gradle` is for, and it
  must never change;
- uninstall goes through the normal Apps & features list.

The package is not signed, so SmartScreen greets a freshly downloaded
installer with "Windows protected your PC" → *More info* → *Run anyway*. Only
a code signing certificate removes that.

The old NSIS and IzPack path (`./gradlew windowsInstaller`) is still in the
tree but superseded: it hunts for a system JRE 1.6 and, failing to find one,
downloads it from a Sun URL that has not existed for over a decade.

---

## Documentation

The design documents under `docs/` are written in Ukrainian.

| Document                                                       | What it covers                                                                                                               |
| -------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| [docs/PROJECT_FORMAT.md](docs/PROJECT_FORMAT.md)               | Complete project-format reference: syntax, identifiers, attribute types, how IDEF0 maps onto the files, safe-editing recipes |
| [docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md)                     | The procedure an agent follows to edit a model in the files, and where it must stop                                          |
| [docs/FORMAT_MIGRATION_PLAN.md](docs/FORMAT_MIGRATION_PLAN.md) | Why the format changed and how the migration was carried out, stage by stage                                                 |
| [docs/START_REVIEW.md](docs/START_REVIEW.md)                   | The storage subsystem as it stood before that work — the baseline for comparison                                             |
| [docs/DOCKER.md](docs/DOCKER.md)                               | Containerised builds, running the GUI from a container, CI notes                                                             |

---

## Changes in this fork

Modifications to the original Ramus, as required by section 5 of the GPL:

**2026 — text project format.** Projects are saved as directories of YAML
instead of the binary `.rsf`; `.rsf` became read-only. Rendering is
deterministic, identifiers are stable, and diagram layout was separated from
the visual blob. See
[docs/FORMAT_MIGRATION_PLAN.md](docs/FORMAT_MIGRATION_PLAN.md).

**2026 — containerised build.** Multi-stage `Dockerfile` for building,
testing and running without a host JDK.

**2.0.2 — macOS support** (by [Vladislav Pavlik](https://github.com/Inv1x/)).
App bundle and DMG packaging through Gradle and `jpackage`; native Dock icon
and `Info.plist`; the macOS system menu bar; ⌘-based keyboard shortcuts; a
bundled Java runtime, minimised with `jlink` where available.

**Modernised toolchain.** Gradle 8.x and a current JDK, where upstream targeted
Java 8.

---

## Contributing

Bug reports, fixes, documentation and packaging improvements are all welcome —
open an issue or a pull request.

Commit messages and pull requests are in English. The rest of what an AI agent
working in this repository is expected to follow is in
[AGENTS.md](https://github.com/Vitaliy-Yakovchuk/ramus/blob/master/AGENTS.md).

---

## License

**GNU General Public License, version 3.** The full text is in
[LICENSE](https://github.com/Vitaliy-Yakovchuk/ramus/blob/master/LICENSE).

Original copyright © 2005–2025 Vitaliy Yakovchuk, Oleksiy Chizhevskiy.
Modifications in this fork are released under the same license.

Ramus is free software: you may redistribute it and modify it under the terms
of the GPL. It comes with **no warranty** — see sections 15 and 16 of the
license.
