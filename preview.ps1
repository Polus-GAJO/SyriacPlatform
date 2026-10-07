param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateRange(1, [int]::MaxValue)]
    [int]$OccasionId
)

$ErrorActionPreference = "Stop"

$repoRoot = $PSScriptRoot
$configPath = Join-Path $repoRoot "development-content.properties"
$gradleWrapper = Join-Path $repoRoot "platform\gradlew.bat"

if (-not (Test-Path -LiteralPath $configPath -PathType Leaf)) {
    throw "Development content configuration was not found: $configPath"
}

if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf)) {
    throw "Gradle wrapper was not found: $gradleWrapper"
}

$configLines = Get-Content -LiteralPath $configPath
$occasionPattern = '^\s*occasionId\s*='

if (-not ($configLines | Where-Object { $_ -match $occasionPattern })) {
    throw "development-content.properties does not contain an occasionId entry."
}

$updatedLines =
    $configLines | ForEach-Object {
        if ($_ -match $occasionPattern) {
            "occasionId=$OccasionId"
        } else {
            $_
        }
    }

Set-Content -LiteralPath $configPath -Value $updatedLines -Encoding ASCII

Write-Host "Development occasion set to $OccasionId."
Write-Host "Cleaning generated app resources from the previous occasion..."

& $gradleWrapper -p (Join-Path $repoRoot "platform") :shared:clean :androidApp:clean

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Building occasion preview..."

& $gradleWrapper -p (Join-Path $repoRoot "platform") :buildtools:buildOccasionPreview

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
