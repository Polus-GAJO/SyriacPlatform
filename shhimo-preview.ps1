# Rebuild and install the Shhimo Android debug app from the latest Access CSV export.
# Run after the Access ExportShhimoPackageData button has completed successfully.
param(
    [switch]$BuildOnly
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$source = Join-Path $root "author-database\exports\book-1"
$previewRoot = Join-Path $root "author-database\preview"
$assets = Join-Path $root "platform\shhimoApp\src\debug\assets\shhimo"
$gradle = Join-Path $root "platform\gradlew.bat"

if (-not (Test-Path -LiteralPath (Join-Path $source "ExistsIn.csv") -PathType Leaf)) {
    throw "Missing Shhimo export: $source\ExistsIn.csv. Export Book 1 from Access first."
}
if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
    throw "Gradle wrapper not found: $gradle"
}

New-Item -ItemType Directory -Path $previewRoot -Force | Out-Null
# A fresh, never-reused directory avoids the package builder's nonempty-output guard.
$output = Join-Path $previewRoot ("shhimo-build-" + [guid]::NewGuid().ToString("N"))
$backup = $null
$installedAssets = $false
try {
    New-Item -ItemType Directory -Path $output | Out-Null
    Push-Location (Join-Path $root "platform")
    try {
        & $gradle :buildtools:buildShhimoDailyPackage "-PbookId=1" "-PsourceDir=$source" "-PoutputDir=$output" "-PincludeMedia=true" --rerun-tasks
        if ($LASTEXITCODE -ne 0) { throw "Shhimo package build failed; app assets unchanged." }
    } finally { Pop-Location }

    if (-not (Test-Path -LiteralPath (Join-Path $output "manifest.json") -PathType Leaf)) {
        throw "Shhimo package has no manifest.json; app assets unchanged."
    }

    # Keep old debug assets until the new package has been generated and checked.
    $assetsParent = Split-Path -Parent $assets
    New-Item -ItemType Directory -Path $assetsParent -Force | Out-Null
    if (Test-Path -LiteralPath $assets) {
        $backup = Join-Path $previewRoot ("shhimo-backup-" + [guid]::NewGuid().ToString("N"))
        Move-Item -LiteralPath $assets -Destination $backup
    }
    try {
        Copy-Item -LiteralPath $output -Destination $assets -Recurse
        $installedAssets = $true
    } catch {
        if (Test-Path -LiteralPath $assets) { Remove-Item -LiteralPath $assets -Recurse -Force }
        if ($backup) { Move-Item -LiteralPath $backup -Destination $assets; $backup = $null }
        throw
    }

    if (-not $BuildOnly) {
        Push-Location (Join-Path $root "platform")
        try {
            & $gradle :shhimoApp:installDebug
            if ($LASTEXITCODE -ne 0) { throw "Shhimo installDebug failed. Generated debug assets remain updated." }
        } finally { Pop-Location }
    }

    if ($backup -and (Test-Path -LiteralPath $backup)) {
        Remove-Item -LiteralPath $backup -Recurse -Force
        $backup = $null
    }
    Write-Host "Shhimo package refreshed successfully." -ForegroundColor Green
    if (-not $BuildOnly) { Write-Host "Shhimo debug app installed successfully." -ForegroundColor Green }
} finally {
    # Preserve the last successful debug assets even if installation fails.
    if ($backup -and (Test-Path -LiteralPath $backup)) {
        Write-Warning "Previous debug assets backup retained at: $backup"
    }
    if (Test-Path -LiteralPath $output) { Remove-Item -LiteralPath $output -Recurse -Force }
}
