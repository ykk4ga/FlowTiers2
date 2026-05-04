# FlowTiers Multi-Version Builds

FlowTiers builds one Fabric jar per Minecraft target. The target is selected with the Gradle property `target_mc`.

## Requirements

- Java 21 is required for the `1.21.x` targets.
- Java 25 is required for the `26.1.x` targets.
- On Windows, run commands from the project root:

```powershell
cd C:\Users\thede\Desktop\FlowTiers
```

## Build Commands

Build the default target from `gradle.properties`:

```powershell
.\gradlew.bat build
```

Build one specific target:

```powershell
.\gradlew.bat build "-Ptarget_mc=1.21.11"
.\gradlew.bat build "-Ptarget_mc=26.1.2"
```

Build every configured target:

```powershell
.\gradlew.bat buildAllMcVersions
```

If you are using PowerShell, keep the `"-Ptarget_mc=..."` argument quoted.

## Output

Jars are written to `build/libs`.

Example:

```text
build/libs/flowtiers-1.0.0+mc1.21.11.jar
build/libs/flowtiers-1.0.0+mc26.1.2.jar
```

Use the jar whose `+mc...` suffix matches the Minecraft version you are launching.

## Supported Targets

Configured targets:

- `1.21`
- `1.21.1`
- `1.21.2`
- `1.21.3`
- `1.21.4`
- `1.21.5`
- `1.21.6`
- `1.21.7`
- `1.21.8`
- `1.21.9`
- `1.21.10`
- `1.21.11`
- `26.1`
- `26.1.1`
- `26.1.2`

## Current Feature Status

`1.21` through `1.21.11` are the main supported builds. They include the command, HUD, nametag, tab list, Mod Menu/Cloth Config, HUD placement, and leaderboard/player stats screens.

`26.1`, `26.1.1`, and `26.1.2` build from `src/client26/java`. The 26.x port currently includes:

- `/flowtiers` command
- FlowPvP API/cache/stats logic
- compact HUD
- `L` leaderboard keybind
- basic leaderboard screen with scroll loading and ladder switching

Still being ported for 26.x:

- polished leaderboard tabs/search/player profile screen
- Mod Menu and Cloth Config screen
- draggable HUD placement screen
- tab list stats mixin
- nametag stats mixin
- custom icon font styling

## How The Build Is Split

`1.21.x` targets use Yarn mappings and the `fabric-loom-remap` plugin.

`26.1.x` targets use the 26.x no-mappings/unobfuscated build path and the normal `fabric-loom` plugin. These targets use Java 25 and separate client source code in `src/client26/java`.

The version matrix and dependency pins live in:

```text
gradle/versions.gradle
```

## Useful Checks

Quick check for the newest 1.21 target:

```powershell
.\gradlew.bat build "-Ptarget_mc=1.21.11"
```

Quick check for the newest 26 target:

```powershell
.\gradlew.bat build "-Ptarget_mc=26.1.2"
```
