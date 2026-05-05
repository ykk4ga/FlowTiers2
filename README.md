# FlowTiers

FlowTiers is a client-side Fabric mod that shows FlowPvP ranked stats in-game. It can display a player's tier, ELO, leaderboard position, and ladder icon through a command, HUD overlay, nametags, the tab list, and an in-game leaderboard screen.

Stats are fetched from the public FlowPvP API at `https://flowpvp.gg/api`.

## Features

- `/flowtiers <player>` command with username to UUID lookup
- Compact configurable HUD for your own FlowPvP stats
- Nametag and tab list stat overlays
- FlowPvP leaderboard screen with ladder switching, search, and player profiles
- Highest-tier and global display modes
- Custom ladder icons
- Mod Menu + Cloth Config integration
- Draggable HUD placement screen
- Multi-version Fabric builds for Minecraft `1.21` through `1.21.11` and `26.1` through `26.1.2`

## Requirements

- Fabric Loader `0.17.2` or newer
- Fabric API
- Cloth Config
- Mod Menu

Java requirements depend on the target Minecraft version:

| Minecraft target | Java |
| --- | --- |
| `1.21.x` | Java 21 |
| `26.1.x` | Java 25 |

## Installation

1. Build or download the jar for your Minecraft version.
2. Put the matching FlowTiers jar in your `mods` folder.
3. Install Fabric API, Cloth Config, and Mod Menu for the same Minecraft version.
4. Launch Minecraft with Fabric.

Use the jar whose `+mc...` suffix matches the Minecraft version you are launching.

```text
flowtiers-1.0.0+mc1.21.11.jar
flowtiers-1.0.0+mc26.1.2.jar
```

## Building

Run commands from the project root.

Build the default target from `gradle.properties`:

```powershell
.\gradlew.bat build
```

Build one specific target:

```powershell
.\gradlew.bat build "-Ptarget_mc=1.21.11"
.\gradlew.bat build "-Ptarget_mc=26.1.2"
```

Build every configured Minecraft target:

```powershell
.\gradlew.bat buildAllMcVersions
```

PowerShell users should keep the `"-Ptarget_mc=..."` argument quoted.

Build outputs are written to:

```text
build/libs
```

## Supported Versions

Configured build targets:

| Minecraft | Status |
| --- | --- |
| `1.21` | Supported |
| `1.21.1` | Supported |
| `1.21.2` | Supported |
| `1.21.3` | Supported |
| `1.21.4` | Supported |
| `1.21.5` | Supported |
| `1.21.6` | Supported |
| `1.21.7` | Supported |
| `1.21.8` | Supported |
| `1.21.9` | Supported |
| `1.21.10` | Supported |
| `1.21.11` | Supported |
| `26.1` | Supported |
| `26.1.1` | Supported |
| `26.1.2` | Supported |

The full configured matrix is intended to build with `buildAllMcVersions`. Runtime testing is still recommended when updating Minecraft, Fabric Loader, or Fabric API versions because mixins depend on client internals.

## Project Structure

- `src/main` contains shared mod metadata and common entrypoint code.
- `src/client`, `src/clientLegacy`, and `src/clientModern` contain the 1.21 client implementation.
- `src/client26` contains the 26.x client implementation.
- `gradle/versions.gradle` contains the Minecraft version matrix and dependency pins.
