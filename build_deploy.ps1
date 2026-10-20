param(
    [string]$TvHost = $env:TV_HOST,
    [switch]$InputKeyService,
    [switch]$HomeKeyService
)

$ErrorActionPreference = 'Stop'

if (-not $TvHost) {
    throw 'Please specify your TV IP address: -TvHost <IP> or set $env:TV_HOST="192.168.x.x"'
}
$env:TV_HOST = $TvHost

Set-Location $PSScriptRoot

Write-Host "Connecting to TV at $env:TV_HOST..."
$launcherScript = Join-Path $PSScriptRoot 'launcher\build-and-install.ps1'

$params = @('-TvHost', $env:TV_HOST)
if ($InputKeyService) { $params += '-InputKeyService' }
if ($HomeKeyService) { $params += '-HomeKeyService' }

& powershell.exe -ExecutionPolicy Bypass -File $launcherScript @params
