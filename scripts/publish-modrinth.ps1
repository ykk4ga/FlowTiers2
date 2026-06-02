param(
	[string]$ProjectId = "flowtiers",
	[string]$VersionType = "release",
	[string]$Changelog = "See the project description for details.",
	[switch]$AllowPartial,
	[switch]$AllowStale,
	[switch]$SyncDescription,
	[switch]$SyncDescriptionOnly,
	[switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-ModrinthCurl {
	param([string[]]$Arguments)

	if ($DryRun) {
		Write-Host "Dry run: curl.exe $((($Arguments -join ' ') -replace 'Authorization: [^ ]+', 'Authorization: [hidden]'))"
		return
	}

	& curl.exe @Arguments
	if ($LASTEXITCODE -ne 0) {
		throw "Modrinth request failed with exit code $LASTEXITCODE."
	}
}

if (-not $env:MODRINTH_TOKEN) {
	throw "MODRINTH_TOKEN is not set. Load your Modrinth personal access token into the environment first."
}

if (-not (Get-Command curl.exe -ErrorAction SilentlyContinue)) {
	throw "curl.exe was not found. Install curl or use a current Windows installation."
}

$root = Split-Path -Parent $PSScriptRoot
$descriptionPath = Join-Path $root "desc.md"
$gradlePropertiesPath = Join-Path $root "gradle.properties"
$versionsPath = Join-Path $root "gradle\versions.gradle"
$libsPath = Join-Path $root "build\libs"
$tempFiles = [System.Collections.Generic.List[string]]::new()
$headers = @(
	"-H", "Authorization: $env:MODRINTH_TOKEN",
	"-H", "User-Agent: Fecl/FlowTiers-Modrinth-Publisher"
)

function New-TemporaryJsonFile {
        param([object]$Data)

        $path = [System.IO.Path]::GetTempFileName()
	$tempFiles.Add($path)
        $json = $Data | ConvertTo-Json -Depth 8
        [System.IO.File]::WriteAllText($path, $json, [System.Text.UTF8Encoding]::new($false))
        return $path
}

function Resolve-ModrinthProjectId {
        $project = Invoke-RestMethod `
                -Uri "https://api.modrinth.com/v2/project/$ProjectId" `
                -Headers @{ "User-Agent" = "Fecl/FlowTiers-Modrinth-Publisher" }
        if (-not $project.id) {
                throw "Could not resolve Modrinth project '$ProjectId'."
        }
        return $project.id
}

function Sync-ModrinthDescription {
	if (-not (Test-Path -LiteralPath $descriptionPath)) {
		throw "Could not find $descriptionPath."
	}

	$payloadPath = New-TemporaryJsonFile @{ body = (Get-Content -LiteralPath $descriptionPath -Raw) }
	Write-Host "Syncing desc.md to Modrinth project '$ProjectId'..."
	$arguments = @(
		"--fail-with-body", "--silent", "--show-error",
		"-X", "PATCH",
		"-H", "Content-Type: application/json",
		"--data-binary", "@$payloadPath",
		"https://api.modrinth.com/v2/project/$ProjectId"
	)
	$arguments = $arguments[0..4] + $headers + $arguments[5..($arguments.Count - 1)]
	Invoke-ModrinthCurl $arguments
	Write-Host "Description synced."
}

try {
	if ($SyncDescriptionOnly) {
		Sync-ModrinthDescription
		exit 0
	}

	$gradleProperties = Get-Content -LiteralPath $gradlePropertiesPath -Raw
	if ($gradleProperties -notmatch "(?m)^mod_version=(.+)$") {
		throw "Could not read mod_version from gradle.properties."
	}
	$modVersion = $Matches[1].Trim()

	$expectedMinecraftVersions = Get-Content -LiteralPath $versionsPath |
			ForEach-Object { if ($_ -match '^\s*"([^"]+)"\s*:\s*\[') { $Matches[1] } }

	if (-not $expectedMinecraftVersions) {
		throw "Could not read Minecraft versions from gradle/versions.gradle."
	}

	$artifacts = @()
	$latestSourceWrite = Get-ChildItem -LiteralPath (Join-Path $root "src") -Recurse -File |
			Sort-Object LastWriteTimeUtc -Descending |
			Select-Object -First 1 -ExpandProperty LastWriteTimeUtc
	foreach ($minecraftVersion in $expectedMinecraftVersions) {
		$jarPath = Join-Path $libsPath "flowtiers-$modVersion+mc$minecraftVersion.jar"
		if (Test-Path -LiteralPath $jarPath) {
			if (-not $AllowStale -and (Get-Item -LiteralPath $jarPath).LastWriteTimeUtc -lt $latestSourceWrite) {
				throw "$jarPath is older than the source code. Run .\gradlew buildAllMcVersions first, or pass -AllowStale intentionally."
			}
			$artifacts += [PSCustomObject]@{ MinecraftVersion = $minecraftVersion; JarPath = $jarPath }
		} elseif (-not $AllowPartial) {
			throw "Missing $jarPath. Run .\gradlew buildAllMcVersions first, or pass -AllowPartial intentionally."
		}
	}

        if (-not $artifacts) {
                throw "No release JARs were found in $libsPath."
        }

        $resolvedProjectId = if ($DryRun) { $ProjectId } else { Resolve-ModrinthProjectId }
        Write-Host "Publishing FlowTiers $modVersion for $($artifacts.Count) Minecraft versions..."
        foreach ($artifact in $artifacts) {
                $metadata = @{
                        project_id = $resolvedProjectId
			name = "Flowtiers $modVersion for $($artifact.MinecraftVersion)"
			version_number = "$modVersion+mc$($artifact.MinecraftVersion)"
			changelog = $Changelog
                        dependencies = @(
                                @{ project_id = "9s6osm5g"; dependency_type = "required" }
                                @{ project_id = "mOgUt4GM"; dependency_type = "required" }
                        )
			game_versions = @($artifact.MinecraftVersion)
			version_type = $VersionType
			loaders = @("fabric")
			featured = $false
			status = "listed"
			file_parts = @("file")
			primary_file = "file"
		}
                $metadataPath = New-TemporaryJsonFile $metadata
		Write-Host "Uploading Minecraft $($artifact.MinecraftVersion)..."
		$arguments = @(
			"--fail-with-body", "--silent", "--show-error",
			"-X", "POST",
                        "-F", "data=<$metadataPath",
			"-F", "file=@$($artifact.JarPath)",
			"https://api.modrinth.com/v2/version"
		)
		$arguments = $arguments[0..4] + $headers + $arguments[5..($arguments.Count - 1)]
		Invoke-ModrinthCurl $arguments
		Write-Host ""
	}

	if ($SyncDescription) {
		Sync-ModrinthDescription
	}

	Write-Host "Published FlowTiers $modVersion."
} finally {
	foreach ($tempFile in $tempFiles) {
		Remove-Item -LiteralPath $tempFile -Force -ErrorAction SilentlyContinue
	}
}
