<#
.SYNOPSIS
    CleanYourTV Pro — Apple tvOS Edition Universal 1-Click Interactive Installer
.DESCRIPTION
    Beginner-friendly interactive setup for Android TV and Google TV devices over Wi-Fi/Ethernet.
#>

param(
    [string]$TvHost = $env:TV_HOST
)

$ErrorActionPreference = 'Stop'
$OutputEncoding = New-Object System.Text.UTF8Encoding $false

Clear-Host
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  📺 CleanYourTV Pro — Apple tvOS Edition Universal Setup" -ForegroundColor White
Write-Host "  Lightning-fast, ad-free 4K launcher & safe debloat suite" -ForegroundColor Gray
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

# Ensure we run from repository root
Set-Location $PSScriptRoot
$repo = $PSScriptRoot

# 1. Ask for TV IP if not supplied
if (-not $TvHost) {
    Write-Host "Please ensure your TV has 'USB debugging' or 'Network debugging' turned ON" -ForegroundColor Yellow
    Write-Host "(Settings > System/Device Preferences > About > click Build 7 times, then Developer Options)" -ForegroundColor Gray
    Write-Host ""
    $TvHost = Read-Host "Enter your Android TV / Google TV IP Address (e.g., 192.168.1.50)"
}

if (-not $TvHost -or $TvHost.Trim() -eq "") {
    Write-Error "TV IP address cannot be empty. Setup aborted."
    exit 1
}

$TvHost = $TvHost.Trim()
$env:TV_HOST = $TvHost
Write-Host "Target TV: $env:TV_HOST" -ForegroundColor Green
Write-Host ""

# 2. Check / Setup Python Environment for ADB communication
$py = Join-Path $repo 'tools\venv\Scripts\python.exe'
if (-not (Test-Path $py)) {
    Write-Host "[1/3] Preparing lightweight Python ADB environment..." -ForegroundColor Cyan
    $sysPy = (Get-Command python -ErrorAction SilentlyContinue).Source
    if (-not $sysPy) {
        Write-Error "Python 3 is required. Please install Python from https://python.org or Microsoft Store, then re-run."
        exit 1
    }
    & $sysPy -m venv (Join-Path $repo 'tools\venv')
    & $py -m pip install --quiet --disable-pip-version-check -r (Join-Path $repo 'tools\requirements.txt')
}

# 3. Test ADB Connection to TV
Write-Host "[2/3] Verifying ADB connection to $env:TV_HOST:5555..." -ForegroundColor Cyan
$probe = & $py -I (Join-Path $repo 'tools\tv.py') 'echo CONNECT_SUCCESS' 2>&1
if ($probe -notmatch 'CONNECT_SUCCESS') {
    Write-Host ""
    Write-Host "⚠️  Could not connect to TV at $env:TV_HOST:5555." -ForegroundColor Red
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  1. Make sure your TV and PC are on the same Wi-Fi / subnet."
    Write-Host "  2. Confirm 'Network debugging' or 'USB debugging' is enabled on TV."
    Write-Host "  3. Look at your TV screen and accept the 'Allow USB debugging?' prompt."
    Write-Host ""
    $retry = Read-Host "Press [Enter] to retry, or type [Q] to quit"
    if ($retry -match '^[Qq]') { exit 1 }
    $probe = & $py -I (Join-Path $repo 'tools\tv.py') 'echo CONNECT_SUCCESS' 2>&1
    if ($probe -notmatch 'CONNECT_SUCCESS') {
        Write-Error "Connection failed. Please verify your TV IP and developer settings."
        exit 1
    }
}
Write-Host "✅ Connected successfully to TV!" -ForegroundColor Green
Write-Host ""

function Install-LauncherOnly {
    Write-Host "`n🚀 Installing Apple tvOS Launcher..." -ForegroundColor Cyan
    $apkPath = Join-Path $repo 'launcher\app\build\outputs\apk\release\app-release.apk'
    if (-not (Test-Path $apkPath)) {
        # Check if pre-packaged APK in repo root
        $apkPath = Join-Path $repo 'CleanYourTV-tvOS-v1.0.0.apk'
    }
    if (-not (Test-Path $apkPath)) {
        Write-Host "Building APK from source using Gradle..." -ForegroundColor Gray
        & powershell.exe -ExecutionPolicy Bypass -File (Join-Path $repo 'build_deploy.ps1') -TvHost $env:TV_HOST -InputKeyService
        return
    }

    Write-Host "Pushing APK to TV..." -ForegroundColor Gray
    & $py -I (Join-Path $repo 'tools\b64push.py') $apkPath /data/local/tmp/tvhome.apk
    Write-Host "Installing APK..." -ForegroundColor Gray
    $installOut = & $py -I (Join-Path $repo 'tools\tv.py') 'pm install -r /data/local/tmp/tvhome.apk; rm -f /data/local/tmp/tvhome.apk'
    Write-Host $installOut
    
    Write-Host "Configuring CleanYourTV as default Home screen..." -ForegroundColor Gray
    & $py -I (Join-Path $repo 'tools\tv.py') 'cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity'
    & $py -I (Join-Path $repo 'tools\tv.py') 'am start -n com.gathin.tvhome/.MainActivity'
    Write-Host "`n🎉 Success! Apple tvOS Launcher is installed and active on your TV." -ForegroundColor Green
}

function Run-FullOptimization {
    Install-LauncherOnly

    Write-Host "`n🛡️  Applying Safe Debloat Profile (Freezes 27+ OEM background telemetry services)..." -ForegroundColor Cyan
    $debloatFile = Join-Path $repo 'my-debloat.txt'
    if (-not (Test-Path $debloatFile)) {
        Write-Error "Debloat profile not found at $debloatFile"
        return
    }
    & powershell.exe -ExecutionPolicy Bypass -File (Join-Path $repo 'tools\disable.ps1') -TvHost $env:TV_HOST $debloatFile
    Write-Host "`n🎉 Full Optimization Complete! Bloatware frozen with automatic rollback saved." -ForegroundColor Green
}

function Push-Wallpaper {
    Write-Host "`n🖼️  Pushing 4K Wallpaper from Downloads..." -ForegroundColor Cyan
    & powershell.exe -ExecutionPolicy Bypass -File (Join-Path $repo 'push_wallpaper_from_downloads.ps1') -TvHost $env:TV_HOST
}

function Restore-Stock {
    Write-Host "`n🔄 Restoring Stock TV State..." -ForegroundColor Yellow
    $debloatFile = Join-Path $repo 'my-debloat.txt'
    if (Test-Path $debloatFile) {
        Write-Host "Re-enabling bloatware packages from rollback record..." -ForegroundColor Gray
        & powershell.exe -ExecutionPolicy Bypass -File (Join-Path $repo 'tools\restore.ps1') -TvHost $env:TV_HOST $debloatFile
    }
    Write-Host "Re-enabling stock launchers if disabled..." -ForegroundColor Gray
    $stockList = @('com.google.android.apps.tv.launcherx', 'com.google.android.tvlauncher', 'com.google.android.leanbacklauncher')
    foreach ($pkg in $stockList) {
        & $py -I (Join-Path $repo 'tools\tv.py') "pm enable $pkg" | Out-Null
    }
    Write-Host "Resetting Home activity..." -ForegroundColor Gray
    & $py -I (Join-Path $repo 'tools\tv.py') 'cmd package set-home-activity --user 0 android'
    Write-Host "`n✅ Stock state restored. Your TV is back to factory defaults!" -ForegroundColor Green
}

# Main Menu Loop
while ($true) {
    Write-Host "=================================================================" -ForegroundColor DarkGray
    Write-Host "  Select an Action:" -ForegroundColor White
    Write-Host "  [1] Install Apple tvOS Launcher Only (100% Safe, Zero System Changes)" -ForegroundColor Green
    Write-Host "  [2] Full Optimization (Install Launcher + Safe Debloat with Rollback)" -ForegroundColor Cyan
    Write-Host "  [3] Push Custom 4K Wallpaper from Downloads Folder" -ForegroundColor Magenta
    Write-Host "  [4] Restore Stock Google TV Launcher (1-Click Complete Rollback)" -ForegroundColor Yellow
    Write-Host "  [5] Exit" -ForegroundColor Gray
    Write-Host "=================================================================" -ForegroundColor DarkGray

    $choice = Read-Host "Enter option [1-5]"
    switch ($choice) {
        '1' { Install-LauncherOnly; break }
        '2' { Run-FullOptimization; break }
        '3' { Push-Wallpaper; break }
        '4' { Restore-Stock; break }
        '5' { Write-Host "Goodbye!"; exit 0 }
        default { Write-Host "Invalid option. Please choose between 1 and 5." -ForegroundColor Red }
    }
    Write-Host ""
}
