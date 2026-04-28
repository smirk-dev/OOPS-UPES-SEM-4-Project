Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Determine base path of this script robustly
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$base = if ($PSScriptRoot) { $PSScriptRoot } else { $scriptDir }

$devScript = Join-Path $base 'scripts\dev.ps1'

if (-not (Test-Path $devScript)) {
    Write-Error "Could not find dev script at: $devScript"
    exit 1
}

Set-Location $base
Write-Host "Starting project using: $devScript" -ForegroundColor Cyan
try {
    & $devScript
}
catch {
    Write-Error "Failed to run dev script: $_"
    exit 1
}
