# FlowTiers Multi-Version Builds

FlowTiers builds one Fabric jar per Minecraft release target.

The current release targets are:

- 1.21
- 1.21.1
- 1.21.2
- 1.21.3
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11
- 26.1
- 26.1.1
- 26.1.2

Build the default target from `gradle.properties`:

```powershell
.\gradlew.bat build
```

Build one target:

```powershell
.\gradlew.bat build "-Ptarget_mc=1.21.11"
.\gradlew.bat build "-Ptarget_mc=26.1.2"
```

Build every configured target:

```powershell
.\gradlew.bat buildAllMcVersions
```

Output jars are written to `build/libs` and include the Minecraft target in the version, for example:

```text
flowtiers-1.0.0+mc1.21.11.jar
```

The `1.21.x` targets use Fabric Yarn mappings. Fabric currently has no Yarn mappings for `26.1`, `26.1.1`, or `26.1.2`, so those targets use Mojang official mappings.

Each target also pins a matching Fabric API, Cloth Config, and optional Mod Menu API version in `gradle/versions.gradle`.
