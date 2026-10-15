# MPS Re-modularized

Configurable powered armor, the Power Fist, installable modules, and the Tinker
Table for Minecraft 1.21.1. MPS Re-modularized continues MachineMuse's Modular
Powersuits and builds as one mod.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.243
- Java 21

JEI and supported intermod integrations are optional.

## Build

Windows:

```powershell
.\gradlew.bat build --no-configuration-cache
```

Linux or macOS:

```shell
./gradlew build --no-configuration-cache
```

The mod JAR is written to `build/libs`. Gradle downloads the required build
dependencies; a local JEI JAR is not required. Generated resources are committed.

## Attribution and license

Derived from [MachineMusePowersuits](https://github.com/flowtender/MachineMusePowersuits)
and the work of MachineMuse and later contributors. Existing copyright notices
are retained. See [LICENSE](LICENSE) for the Simplified BSD license.
