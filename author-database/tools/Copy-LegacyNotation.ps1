#requires -Version 5.1
<#
Copies legacy notation images into the shared media library without modifying Access.
The resulting manifest is for a separately reviewed Access migration step.
Use -WhatIf first. Existing files are never overwritten.
#>
[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'Medium')]
param(
    [Parameter(Mandatory = $true)][string]$LegacyRoot,
    [string]$MediaRoot = 'D:\SyriacPlatformMedia',
    [string]$ExportRoot = 'D:\SyriacPlatform\author-database\exports\media'
)
$ErrorActionPreference = 'Stop'
$notesCsv = Join-Path $ExportRoot 'MusicNotes.csv'
$linksCsv = Join-Path $ExportRoot 'MNMelody.csv'
foreach ($p in @($notesCsv, $linksCsv)) {
    if (-not (Test-Path -LiteralPath $p -PathType Leaf)) { throw "Missing inventory: $p" }
}
if (-not (Test-Path -LiteralPath $LegacyRoot -PathType Container)) { throw "Missing legacy root: $LegacyRoot" }
$notes = @(Import-Csv -LiteralPath $notesCsv)
$links = @(Import-Csv -LiteralPath $linksCsv)
$noteById = @{}
foreach ($n in $notes) {
    $id = [string]$n.MNN
    if (-not $id -or $noteById.ContainsKey($id)) { throw "Missing or duplicate MNN: $id" }
    $noteById[$id] = $n
}
foreach ($l in $links) {
    if (-not $noteById.ContainsKey([string]$l.MNN)) { throw "MNMelody references missing MNN $($l.MNN)" }
    if ([int]$l.Edition -lt 1) { throw "Invalid Edition for MNID $($l.MNID)" }
}
$legacyFull = [IO.Path]::GetFullPath($LegacyRoot).TrimEnd('\') + '\'
$targetRoot = Join-Path $MediaRoot 'notation'
$targetFull = [IO.Path]::GetFullPath($targetRoot).TrimEnd('\') + '\'
$hashToTarget = @{}
$manifest = New-Object 'System.Collections.Generic.List[object]'
foreach ($n in $notes) {
    $relative = ([string]$n.LinkAdres).Trim().Replace('/', '\')
    if (-not $relative -or [IO.Path]::IsPathRooted($relative) -or $relative -match '(^|\\)\.\.(\\|$)' -or $relative.Contains(':')) {
        throw "Unsafe LinkAdres for MNN $($n.MNN): $relative"
    }
    $source = [IO.Path]::GetFullPath((Join-Path $legacyFull $relative))
    if (-not $source.StartsWith($legacyFull, [StringComparison]::OrdinalIgnoreCase)) { throw "Path escapes root: $relative" }
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Missing source for MNN $($n.MNN): $source" }
    $ext = [IO.Path]::GetExtension($source).ToLowerInvariant()
    if ($ext -notin @('.jpg', '.jpeg', '.png')) { throw "Unsupported extension for MNN $($n.MNN): $ext" }
    $hash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
    if (-not $hashToTarget.ContainsKey($hash)) {
        # Content-based identity avoids duplicates even if legacy names differ.
        $name = "notation-$hash$ext"
        $hashToTarget[$hash] = "notation/$name"
    }
    $relativeTarget = $hashToTarget[$hash]
    $destination = Join-Path $MediaRoot ($relativeTarget.Replace('/', '\'))
    if (Test-Path -LiteralPath $destination -PathType Leaf) {
        $existingHash = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($existingHash -ne $hash) { throw "Destination collision: $destination" }
    }
    $manifest.Add([pscustomobject]@{
        MNN = $n.MNN
        OldLinkAdres = $relative
        SourceRelativePath = $relativeTarget
        ContentHash = $hash
    })
}
# Validate everything before any copy operation.
if (-not $WhatIfPreference) {
    if (-not (Test-Path -LiteralPath $targetRoot -PathType Container)) {
        if ($PSCmdlet.ShouldProcess($targetRoot, 'Create notation directory')) {
            New-Item -ItemType Directory -Path $targetRoot -Force | Out-Null
        }
    }
}
$copied = 0
foreach ($entry in $manifest) {
    $src = [IO.Path]::GetFullPath((Join-Path $legacyFull $entry.OldLinkAdres))
    $dst = Join-Path $MediaRoot ($entry.SourceRelativePath.Replace('/', '\'))
    if (-not (Test-Path -LiteralPath $dst -PathType Leaf)) {
        if ($PSCmdlet.ShouldProcess($dst, "Copy $src")) {
            # Copy only once for repeated image content; never overwrite.
            try { [IO.File]::Copy($src, $dst, $false); $copied++ }
            catch [IO.IOException] {
                if (-not (Test-Path -LiteralPath $dst -PathType Leaf) -or
                    (Get-FileHash -LiteralPath $dst -Algorithm SHA256).Hash.ToLowerInvariant() -ne $entry.ContentHash) { throw }
            }
        }
    }
}
if (-not $WhatIfPreference) {
    $manifestPath = Join-Path $ExportRoot 'NotationMigrationManifest.csv'
    $manifest | Export-Csv -LiteralPath $manifestPath -NoTypeInformation -Encoding UTF8
    Write-Host "Manifest: $manifestPath"
}
Write-Host "Notation records: $($notes.Count); melody links: $($links.Count); unique content: $($hashToTarget.Count); copied: $copied"
Write-Host 'No Access tables or old source images were modified.'
