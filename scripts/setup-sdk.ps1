$ErrorActionPreference = 'Stop'

# Determine Android SDK path from environment variables
$sdk = $Env:ANDROID_SDK_ROOT
if (-not $sdk) {
    $sdk = $Env:ANDROID_HOME
}
if (-not $sdk) {
    Write-Error 'Android SDK not found. Set ANDROID_SDK_ROOT or ANDROID_HOME.'
}

$sdkPath = [System.IO.Path]::GetFullPath($sdk)
Write-Host "Using Android SDK at $sdkPath"

$repoRoot = Split-Path -Parent $PSScriptRoot
$localProps = Join-Path $repoRoot 'local.properties'

if (-not (Test-Path $localProps)) {
    Set-Content -Path $localProps -Value "sdk.dir=$sdkPath"
    Write-Host 'Created local.properties'
} else {
    Write-Host 'local.properties already exists; no changes made.'
}
