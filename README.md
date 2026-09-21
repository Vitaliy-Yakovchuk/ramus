# Ramus

**Java-based IDEF0 & DFD modeller.** Build business-process models in the IDEF0
and DFD notations, keep them in git, and edit them by hand.

<img width="1792" alt="The Ramus application window" src="https://user-images.githubusercontent.com/2261228/69039713-23c56d00-09f5-11ea-99c5-b6714efe3037.png">

<img width="1792" alt="Another view of the Ramus application window" src="https://user-images.githubusercontent.com/2261228/69039723-27f18a80-09f5-11ea-9a8d-508069ce7bbd.png">

By Vitaliy Yakovchuk. Free software under the GNU GPL, version 3 — see
[License](#license).

---

## Contents

- [Download](#download)
- [Quick start](#quick-start)
- [Project format](#project-format)
- [Building](#building)
  - [macOS application and DMG](#macos-application-and-dmg)
  - [Windows installer](#windows-installer)
- [Contributing](#contributing)
- [License](#license)

---

## Download

Packaged builds are on the
[Releases page](https://github.com/Vitaliy-Yakovchuk/ramus/releases): the macOS
DMG and the Windows installer are built by CI and attached to the release of a
`v*` tag. To build them yourself, see [Building](#building); to run the
application from source, see [Quick start](#quick-start).

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

The application reads and writes **`.rsf`** — a ZIP of XML table dumps. It is
the only format it opens and the only one it saves.

### The YAML project tree

Beside it the tree carries a text format: a project as a **directory of YAML
files**, made to be read, diffed and edited by hand.

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

Consequences worth knowing:

**It goes into git as it is.** Writing it is deterministic, so converting an
unchanged model rewrites no file and `git status` stays quiet until the model
itself actually changes. There are no timestamps in the files, deliberately —
they would make every write look like a change.

**It is meant to be edited by hand.** The YAML dialect is restricted on
purpose: block style, sorted keys, quoted strings, no anchors or aliases, no
line wrapping. One line in the file is one line of the value, so `sed` works
and a diff points at the change rather than the paragraph around it. Editing
values is safe; inventing identifiers is not — add new elements in the
application, then edit them in the files.

**`project.ramus` is the entry point.** It carries the schema version, the
`application-version`, `minimum-version` and the required `plugins`, and its
presence is what makes a directory a project.

**The application neither opens nor saves it.** A project handed to it — the
directory or that file — is refused with a message naming it. The tree is
reached through the converters, which read and write it with the same code the
application uses for the model itself:

```bash
./gradlew :ramus-core-demo:rsfToYaml -Prsf=<file.rsf> -Pout=<directory>
./gradlew :ramus-core-demo:yamlToRsf -Pin=<directory> -Prsf=<file.rsf>
```

So the way to edit a model as text is a round trip: convert it out, edit the
files, convert it back, open the `.rsf`.

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

Running the GUI from the container needs an X11 socket and an auth cookie.
Mounting `/tmp/.X11-unix` alone is not enough — the X server rejects the
connection unless the cookie's family field is relaxed so the container
hostname matches:

```bash
XAUTH=$(mktemp)
xauth nlist "$DISPLAY" | sed -e 's/^..../ffff/' | xauth -f "$XAUTH" nmerge -
chmod 644 "$XAUTH"

docker run --rm \
  -e DISPLAY="$DISPLAY" \
  -e XAUTHORITY=/tmp/.xauth \
  -v /tmp/.X11-unix:/tmp/.X11-unix:ro \
  -v "$XAUTH:/tmp/.xauth:ro" \
  -v "$PWD:/work" \
  ramus
```

Projects are taken from `/work`, so the host's current directory is mounted
there. The container user has UID 1000, so files created in that mount belong
to you. Under Wayland this goes through XWayland — `DISPLAY` already points
at it.

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

Pointing packaging at a specific JDK, without touching your shell's
`JAVA_HOME`: create `gradle-local.properties` in the repository root (it is
gitignored).

```properties
packagingJavaHome=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
# packagingUseJlink=false          # bundle the full JDK instead of a jlink image
# packagingJmodsPath=…/jmods       # explicit module path for jlink
# packagingLocales=en,uk,ru,pl     # locale data kept in the jlink runtime
```

The bundled runtime keeps locale data for English, Ukrainian and Russian only.
Without it dates and Cyrillic sorting fall back to the root locale, and with
all of it the image grows by about ten megabytes; `packagingLocales` is the
dial between the two.

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
- a Start menu shortcut appears, and `.rsf` files open on double-click;
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

## Contributing

Bug reports, fixes, documentation and packaging improvements are all welcome —
open an issue or a pull request.

---

## License

**GNU General Public License, version 3.** The full text is in
[LICENSE](https://github.com/Vitaliy-Yakovchuk/ramus/blob/master/LICENSE).

Copyright © 2005–2026 Vitaliy Yakovchuk.

Ramus is free software: you may redistribute it and modify it under the terms
of the GPL. It comes with **no warranty** — see sections 15 and 16 of the
license.
