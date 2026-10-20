param(
    [string]$TvHost = $env:TV_HOST,
    [string]$DownloadsDir = (Join-Path $HOME 'Downloads')
)

$ErrorActionPreference = 'Stop'

if (-not $TvHost) {
    throw 'Please specify your TV IP address: -TvHost <IP> or set $env:TV_HOST="192.168.x.x"'
}
$env:TV_HOST = $TvHost

Add-Type -AssemblyName System.Drawing

if (-not (Test-Path $DownloadsDir)) {
    throw "Downloads directory not found at: $DownloadsDir"
}

Write-Host "Scanning $DownloadsDir for newest image (.jpg, .jpeg, .png)..."
$file = Get-ChildItem -Path $DownloadsDir -File |
    Where-Object { $_.Extension -match '^\.(jpe?g|png)$' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $file) {
    throw "No .jpg or .png images found in $DownloadsDir"
}

Write-Host "Found newest image: $($file.Name) ($($file.Length) bytes, modified $($file.LastWriteTime))"

$src = [System.Drawing.Image]::FromFile($file.FullName)
Write-Host "Source dimensions: $($src.Width)x$($src.Height)"

$tw = 3840
$th = 2160
$dst = New-Object System.Drawing.Bitmap($tw, $th, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
$g = [System.Drawing.Graphics]::FromImage($dst)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

$srcAspect = $src.Width / $src.Height
$dstAspect = $tw / $th
if ($srcAspect -gt $dstAspect) {
    $cropW = [int]($src.Height * $dstAspect)
    $cropH = $src.Height
    $cropX = [int](($src.Width - $cropW) / 2)
    $cropY = 0
} else {
    $cropW = $src.Width
    $cropH = [int]($src.Width / $dstAspect)
    $cropX = 0
    $cropY = [int](($src.Height - $cropH) / 2)
}

$srcRect = New-Object System.Drawing.Rectangle($cropX, $cropY, $cropW, $cropH)
$dstRect = New-Object System.Drawing.Rectangle(0, 0, $tw, $th)
$g.DrawImage($src, $dstRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
$g.Dispose()
$src.Dispose()

$codec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { $_.MimeType -eq 'image/jpeg' }
$encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
$encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter([System.Drawing.Imaging.Encoder]::Quality, [long]95)

$outPath = Join-Path $env:TEMP 'cleanyourtv_wallpaper_4k.jpg'
$dst.Save($outPath, $codec, $encoderParams)
$dst.Dispose()
$encoderParams.Dispose()

Write-Host "Processed into 4K UHD: $outPath ($((Get-Item $outPath).Length) bytes)"

$repo = $PSScriptRoot
$py = Join-Path $repo 'tools\venv\Scripts\python.exe'
if (-not (Test-Path $py)) {
    $py = (Get-Command python -ErrorAction SilentlyContinue).Source
}
if (-not $py) {
    throw "Python not found. Please setup tools\venv or install python."
}

Set-Location $repo
Write-Host "Pushing wallpaper to TV at $TvHost..."
& $py -I (Join-Path $repo 'tools\tv.py') 'mkdir -p /sdcard/Android/data/com.gathin.tvhome/files'
& $py -I (Join-Path $repo 'tools\b64push.py') $outPath /sdcard/Android/data/com.gathin.tvhome/files/wallpaper.jpg

Write-Host "Reloading launcher on TV..."
& $py -I (Join-Path $repo 'tools\tv.py') 'am force-stop com.gathin.tvhome'
Start-Sleep -Seconds 1
& $py -I (Join-Path $repo 'tools\tv.py') 'input keyevent KEYCODE_HOME'
Write-Host "Wallpaper updated and applied successfully!"
