# Ramus

**Java-based IDEF0 & DFD modeller.** Build business-process models in the IDEF0
and DFD notations, keep them in git, and edit them by hand.

<img width="1792" alt="Screenshot 2019-11-18 at 11 14 26" src="https://user-images.githubusercontent.com/2261228/69039713-23c56d00-09f5-11ea-99c5-b6714efe3037.png">

<img width="1792" alt="Screenshot 2019-11-18 at 11 14 59" src="https://user-images.githubusercontent.com/2261228/69039723-27f18a80-09f5-11ea-9a8d-508069ce7bbd.png">

Original project by Vitaliy Yakovchuk and Oleksiy Chizhevskiy,
<http://ramussoftware.com/>. This repository is a **modified fork** — see
[Changes in this fork](#changes-in-this-fork) and [License](#license).

---

## Contents

- [Quick start](#quick-start)
- [Project format](#project-format)
- [Building](#building)
- [Documentation](#documentation)
- [Changes in this fork](#changes-in-this-fork)
- [Contributing](#contributing)
- [License](#license)

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
./gradlew :local-client:createMacApp     # dev bundle, local-client/build/mac-app/Ramus.app
./gradlew :local-client:macDmg           # standalone DMG in dest/macos/
```

`macDmg` runs the full pipeline: `.icns` from
`packaging/macos/AppIcon.appiconset` → optional `jlink` runtime → `jpackage`.
The resulting DMG bundles a Java runtime, so its users need no Java. If `jlink`
is unavailable the full JDK is bundled instead — larger, but it works.

Prebuilt DMGs are published under this repository's GitHub Releases.

Pointing packaging at a specific JDK, without touching your shell's
`JAVA_HOME`: create `gradle-local.properties` in the repository root (it is
gitignored).

```properties
packagingJavaHome=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
# packagingUseJlink=false          # bundle the full JDK instead of a jlink image
# packagingJmodsPath=…/jmods       # explicit module path for jlink
```

### Windows installer

```bash
./gradlew windowsInstallerDocker     # via the cdrx/nsis image
./gradlew windowsInstaller           # via a local wine + NSIS
```

Windows is not actively maintained in this fork and may not work properly.

---

## Documentation

The design documents under `docs/` are written in Ukrainian.

| Document                                                       | What it covers                                                                                                               |
| -------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| [docs/PROJECT_FORMAT.md](docs/PROJECT_FORMAT.md)               | Complete project-format reference: syntax, identifiers, attribute types, how IDEF0 maps onto the files, safe-editing recipes |
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

---

## License

**GNU General Public License, version 3.** The full text is in
[LICENSE](LICENSE).

Original copyright © 2005–2025 Vitaliy Yakovchuk, Oleksiy Chizhevskiy.
Modifications in this fork are released under the same license.

Ramus is free software: you may redistribute it and modify it under the terms
of the GPL. It comes with **no warranty** — see sections 15 and 16 of the
license.
