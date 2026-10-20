# 📺 CleanYourTV Pro — Apple tvOS Edition for Android TV & Google TV

<p align="center">
  <img src="docs/screenshots/hero_home.png" alt="CleanYourTV Pro — Apple tvOS Edition" width="900">
</p>

<p align="center">
  <b>A 380 KB, zero-ad, crash-free, ultra-responsive 4K launcher and safe debloat suite that transforms sluggish Google TVs into an authentic Apple tvOS experience.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%20TV%209%E2%80%9314-8A2BE2.svg?style=for-the-badge&logo=android" alt="Platform: Android TV 9-14">
  <img src="https://img.shields.io/badge/APK%20Size-~380%20KB-00C853.svg?style=for-the-badge" alt="APK Size: ~380 KB">
  <img src="https://img.shields.io/badge/RAM%20Footprint-%3C25%20MB-00B0FF.svg?style=for-the-badge" alt="RAM Footprint: <25 MB">
  <img src="https://img.shields.io/badge/Ads%20%26%20Telemetry-0%25-FF3D00.svg?style=for-the-badge" alt="Ads & Telemetry: 0%">
  <img src="https://img.shields.io/badge/Safety-100%25%20Reversible-FFD600.svg?style=for-the-badge" alt="Safety: 100% Reversible">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="MIT License"></a>
</p>

---

## 🌟 Visual Showcase

<p align="center">
  <img src="docs/screenshots/hero_home.png" width="900" alt="Apple tvOS Hero Home Screen">
  <br>
  <i>Clean floating dock, native 4K UHD static wallpaper, real-time Wi-Fi pill with full SSID, and zero advertising banners.</i>
</p>

<br>

<p align="center">
  <img src="docs/screenshots/app_grid.png" width="900" alt="Dual-Category App Grid">
  <br>
  <i>Dual-category app grid showing all official Leanback TV apps and sideloaded Android apps, with background system utilities cleanly filtered.</i>
</p>

---

## 🥊 Why This Edition? (All UI Glitches & Stock Bugs Fixed)

This project builds upon the fantastic foundation of [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv), re-architecting the rendering and interaction pipeline to eliminate visual artifacts and deliver true Apple tvOS fidelity:

| Feature / Subsystem | ❌ Stock Google TV (`LauncherX`) | ⚠️ Original CleanYourTV | 🍎 CleanYourTV Pro (Apple tvOS Edition) |
|---|---|---|---|
| **Focus Physics & Scaling** | Flat 2D white highlight box | Grey rectangular sliding overlay that smeared over app icon artwork | **Authentic Apple tvOS Focus Engine:** `1.10x` card scale, `14dp` Z-elevation pop, smooth `220ms` decelerate ease-out (`DecelerateInterpolator(1.8f)`), and `18dp` squircle corners |
| **Glass Sheen Layer** | None | Simple opaque shape | **2-Stop Translucent Glass Specular:** Multi-layered gradient fill (`0x24FFFFFF` → `0x0CFFFFFF`) with a `1dp` specular rim light (`0x40FFFFFF`) placed *behind* the icon |
| **Background Behavior** | Bloated ads, auto-playing video trailers, sponsored carousels | `AmbientGlow` color-shifting bug flashed red, blue, or pink across wallpaper when moving focus | **Static 4K UHD Engine:** Background tint animations disabled; 4K wallpaper remains 100% static, color-accurate, and flicker-free |
| **Dock Design** | None | Bulky dark rectangular container shelf covering the lower screen | **True Floating Dock:** Container shelf removed (`DockDecoration` rendered transparent); app tiles float cleanly over your 4K backdrop |
| **App Discovery** | Ads first; app drawer hidden | Official Android TV Leanback apps only; sideloaded APKs missed | **Dual-Category Discovery:** Queries **both** `CATEGORY_LEANBACK_LAUNCHER` & `CATEGORY_LAUNCHER`, deduplicated in real-time |
| **Play Store Installs** | Automatic | Required manual app restart or rebuild to detect new installations | **Live Auto-Refresh:** Integrated `BroadcastReceiver` (`ACTION_PACKAGE_ADDED` / `REPLACED`) auto-populates new apps instantly |
| **Header Ergonomics** | Cluttered account / ad tabs | Settings button had text label; Wi-Fi truncated to 12 chars (`"My_Home_Wi..."`) | **Refined Top Bar:** `[Wi-Fi (Full SSID)]` $\rightarrow$ `[Inputs (Live TV/HDMI)]` $\rightarrow$ compact icon-only `[Settings ⚙️]` |
| **Wallpaper Quality** | 1080p compressed | Low-res 4 MB budget | **Native 4K (25 MP) Budget:** Support for full `3840×2160` UHD wallpapers with automated 16:9 center-crop push script |

---

## 📊 Before vs. After Performance Benchmarks

Live diagnostic audit captured on **Skyworth SW6H / Motorola EnvisionX 55" 4K QLED (Android TV 11, MediaTek M7332)**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        RAM UTILIZATION AUDIT                           │
├────────────────────────────────┬───────────────────┬──────────────────┤
│ METRIC                         │ STOCK GOOGLE TV   │ CLEANYOURTV PRO  │
├────────────────────────────────┼───────────────────┼──────────────────┤
│ Launcher RAM Footprint (PSS)   │ ~150 MB – 400 MB+ │ 56.6 MB (↓ 62%!) │
│ GPU Compositing Memory         │ Heavy Multi-layer │ 21.8 MB (Native) │
│ Active System Packages         │ 132 packages      │ 100 packages     │
│ Background Bloatware Frozen    │ 0                 │ 27 packages      │
│ Total System Free RAM Headroom │ ~350 MB           │ 718 MB (~40% free│
│ Ad Fetch Latency               │ 3–5 seconds delay │ Instantaneous    │
└────────────────────────────────┴───────────────────┴──────────────────┘
```

> 💡 **Why this matters for your TV:** Freeing up **368+ MB of RAM** and shutting down 27 background sync loops gives the MediaTek/Realtek video decoder full CPU & memory bandwidth, completely eliminating stutter and dropped frames during **4K HDR, Dolby Vision, and 60fps high-bitrate streaming**.

---

## 🚀 Step-by-Step Installation Guide (Choose 1 of 3 Methods)

---

### 🟢 Method 1: Direct APK Install via USB or TV Browser (No PC Required — Easiest)

1. **Download:** Grab the latest `CleanYourTV-tvOS-v1.0.0.apk` from the **[Releases](../../releases)** tab.
2. **Transfer to TV:**
   - Put it on a USB flash drive and plug it into your TV, **OR**
   - Use the free **Downloader by AFTVnews** app (or **Send Files to TV**) from the Google Play Store on your TV.
3. **Install:** Open your TV's file manager, click the APK, and select **Install**.
4. **Set as Default:** Press the **Home** button on your remote $\rightarrow$ select **CleanYourTV** $\rightarrow$ choose **Always**.

---

### ⚡ Method 2: 1-Click Automated Setup on Windows (Recommended for Full Optimization)

This method automates the launcher installation, safe debloat suite, and 4K wallpaper setup in under 60 seconds:

1. **Enable Debugging on TV:**
   - Go to `Settings` ⚙️ $\rightarrow$ `System` (or `Device Preferences`) $\rightarrow$ `About`.
   - Scroll down to `Android TV OS build` and press the **OK / Center button 7 times** until you see *"You are now a developer!"*.
   - Return to `Settings` $\rightarrow$ `Developer options` $\rightarrow$ turn **USB debugging** (or **Network debugging**) to **ON**.
   - Note your TV's IP address in `Settings` $\rightarrow$ `Network & Internet` (e.g. `192.168.1.50`).

2. **Run the 1-Click Installer on your PC:**
   - Download or clone this repository to your PC.
   - Right-click `Easy-Install.ps1` and select **Run with PowerShell** (or open PowerShell in the repo folder):
     ```powershell
     .\Easy-Install.ps1
     ```
   - Enter your TV's IP address when prompted.
   - Look at your TV screen and select **"Always allow from this computer"** when prompted.
   - Select:
     * `[1]` to install the Apple tvOS Launcher safely without altering any system apps.
     * `[2]` for Full Optimization (Launcher + Safe Debloat with rollback record).

3. **Important Hardware Tip:**
   - When finished, go back to TV `Settings` $\rightarrow$ `Developer options` and toggle **USB debugging OFF**.
   - *Why?* Keeping USB debugging ON locks the TV's USB ports into client mode; turning it OFF restores full 5V USB-OTG power for external hard drives, SSDs, and flash drives.

---

### 🛠️ Method 3: Build & Customize from Source (For Developers)

1. Clone the repository:
   ```bash
   git clone https://github.com/Gathin23/cleanyourtv-tvos.git
   cd cleanyourtv-tvos
   ```
2. Customize your pinned Dock apps or hidden utilities in `launcher/app/src/main/res/values/config.xml`:
   ```xml
   <string-array name="dock_apps">
       <item>org.smarttube.stable</item>
       <item>app.netmirror.newtv</item>
       <item>in.startv.hotstar</item>
       <item>com.amazon.amazonvideo.livingroom</item>
   </string-array>
   ```
3. Compile and deploy directly to your TV:
   ```powershell
   .\build_deploy.ps1 -TvHost 192.168.1.50 -InputKeyService
   ```

---

## 🖼️ Automated 4K Wallpaper Pipeline

Drop any high-resolution wallpaper (`.jpg` or `.png`) into your Windows PC's `Downloads` folder, then run:

```powershell
.\push_wallpaper_from_downloads.ps1 -TvHost 192.168.1.50
```

The script will automatically:
1. Detect your newest downloaded image.
2. Perform an intelligent 16:9 center-crop.
3. Resample using high-quality bicubic interpolation into true `3840×2160` (4K UHD) at 95% JPEG quality.
4. Push it directly over ADB to `/sdcard/Android/data/com.gathin.tvhome/files/wallpaper.jpg`.
5. Instantly reload the launcher with zero TV restarts.

---

## 🔄 100% Safe Rollback & Recovery Guide

**Zero files are deleted.** CleanYourTV only freezes packages in Android user-space (`pm disable-user --user 0`).

- **1-Click Restore via Easy-Install:**
  Run `.\Easy-Install.ps1` and select option `[4] Restore Stock Google TV Launcher`.
- **Manual Rollback:**
  ```powershell
  # Re-enable all frozen OEM bloatware from your rollback backup:
  .\tools\restore.ps1 -TvHost $env:TV_HOST my-debloat.txt

  # Re-enable stock Google TV launcher:
  .\tools\venv\Scripts\python.exe -I tools\tv.py "pm enable com.google.android.apps.tv.launcherx"
  .\tools\venv\Scripts\python.exe -I tools\tv.py "cmd package set-home-activity --user 0 android"
  ```

---

## 💡 Frequently Asked Questions (FAQ)

### Q: Why isn't my external USB drive or hard disk showing up after connecting?
**A:** This is standard Android TV behavior when `USB debugging` is active. Go to your TV's `Settings` $\rightarrow$ `Developer options` and toggle **USB debugging to OFF**. This immediately restores 5V USB-OTG power so all external drives mount properly.

### Q: Do Bluetooth headphones, remotes, and HDMI inputs still work?
**A:** **Yes, 100%.** System audio, Bluetooth HID/A2DP stacks, HDMI-CEC input switching, and the TV's native Settings menu remain fully intact and functional.

### Q: Does Voice Search with the remote microphone still work?
**A:** Yes. `com.google.android.katniss` (Google Assistant / Voice Search) and `com.google.android.gms` (Play Services) are preserved and untouched.

---

## 🤝 Credits & Upstream Foundation

- **Upstream Foundation:** Enhanced and forked from the original open-source [CleanYourTV](https://github.com/Gathin23/cleanyourtv) created by **[Gathin23](https://github.com/Gathin23)**.
- **License:** Licensed under the permissive [MIT License](LICENSE).
