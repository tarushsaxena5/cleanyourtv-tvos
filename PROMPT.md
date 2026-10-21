# The Master AI Assistant Prompt

Paste this into **Google Antigravity**, **Claude Code**, **OpenAI Codex**, **Cursor**, or **Gemini CLI** (or another coding assistant with shell access) on a computer connected to the same Wi-Fi / subnet as the TV.

Works universally on all **Android TV & Google TV** devices (Sony Bravia, TCL, Xiaomi, Hisense, Motorola, Skyworth, OnePlus, Realme, Philips, Chromecast with Google TV, Nvidia Shield TV, ONN 4K Streaming Box, and most others).

> [!NOTE]
> Since you cloned this repository (`tarushsaxena5/cleanyourtv-tvos`), you can reuse `tools/`, `launcher/`, `push_wallpaper_from_downloads.ps1`, and `Easy-Install.ps1` instead of writing them from scratch.

### Preparation on the TV:
1. Go to `Settings` ⚙️ $\rightarrow$ `System` (or `Device Preferences`) $\rightarrow$ `About`.
2. Scroll down to `Build` (or `Android TV OS build`) and press the **OK / Center button 7 times** until you see *"You are now a developer!"*.
3. Go back to `Developer options` $\rightarrow$ turn **USB debugging** (and **Network / ADB debugging** if shown) to **ON**.
4. Keep the TV on and note its IP address under `Settings` $\rightarrow$ `Network & Internet`.

---

```text
I want to debloat my Android TV / Google TV and install the clean Apple tvOS Edition launcher from this repository (tarushsaxena5/cleanyourtv-tvos).
My TV model is: <MODEL NAME HERE> (IP: <TV IP HERE>).

Please work directly on the device, step-by-step, verifying everything you do with screenshots pulled from the TV. Ask me before anything that depends on my preferences.

1. Connect to TV over ADB:
- Connect to my TV over ADB on port 5555 using tools/tv.py (or Wireless Debugging TLS pairing if port 5555 is not exposed).
- If on macOS and the stock adb binary fails with "No route to host" due to Local Network privacy blocks, use the Python adb-shell client in tools/venv.
- For APKs and wallpapers, always use tools/b64push.py to push in base64 chunks to avoid file transfer hangs.
- Warn me when the "Allow USB debugging?" authorization prompt will appear on the TV screen so I can accept it.

2. Back Up Device State:
- Before changing any package state, run tools/recovery.py to record an automated pre-change snapshot of all enabled and disabled packages into ~/.cleanyourtv/recovery-*.json.
- Verify that a rollback script (tools/restore.ps1 or tools/restore.sh) is verified and ready.
- Check if anything is plugged into the TV's USB ports, and warn me that some TVs cut 5V USB-OTG power while USB debugging is enabled.

3. Debloat Bloatware & Telemetry:
- Inspect installed packages and categorize them into:
  * Manufacturer analytics / telemetry (PatchWall, retail demo loops, duplicate app stores)
  * Preloaded bloatware & unused duplicate utilities
  * Essential hardware services (DO NOT TOUCH: TV tuner, HDMI input manager, Bluetooth remotes/audio, Wi-Fi, on-screen keyboard, Play Store, Play Services)
- Ask me which streaming services, voice assistants, or cast services I actually use before disabling any of them.
- Freeze selected bloatware non-destructively using `pm disable-user --user 0` (via tools/disable.ps1 or disable.sh). Never uninstall system packages.

4. Build & Deploy CleanYourTV Pro (Apple tvOS Edition):
- Compile and install the launcher using build_deploy.ps1 (or gradle assembleRelease followed by b64push.py to /data/local/tmp/tvhome.apk and `pm install -r`).
- Ensure all our flagship Apple tvOS upgrades are live and verified:
  * Apple tvOS Focus Engine: 1.10x card scale pop, 14dp Z-elevation lift, 220ms ease-out transitions (DecelerateInterpolator(1.8f)), and 18dp squircle corners.
  * 2-stop translucent specular glass highlight placed strictly behind icons with a 1dp rim light.
  * True borderless Floating Dock: no heavy container box shelf. Auto-populates 4 dock tiles from installed streaming apps (SmartTube, YouTube, Netflix, Disney+, Hotstar, Prime Video, Plex, Kodi).
  * Dual-Category Discovery: queries both LEANBACK and standard LAUNCHER categories with live BroadcastReceiver auto-refresh on Play Store installs.
  * Apple tvOS Clock & Date Lockup: 46sp bold digital time, frosted vertical divider, stacked full uppercase weekday (e.g. WEDNESDAY) and full date & year with zero text truncation.
  * Dynamic Bluetooth Control Center & Frosted Quick Menu: Live connection pill in top header; 1-click frosted glass popup to connect, disconnect, unpair, or pair new accessories.
  * Hardware Remote Settings Shortcut: Direct KEYCODE_SETTINGS (code 176) hardware key interception launching system settings in ~230ms.
  * On-Screen Remote Customization: long-press remote OK/Select opens menu to Pin/Unpin, Move, Hide/Unhide, and view App Info directly on TV.
  * Universal Crash-Proof Inputs: TvInputManager HDMI switcher with defensive OEM fallback intents and Toast handling.
  * Native 4K UHD Static Wallpaper Engine: 25 MP cap raised, zero color flickering, with dark obsidian radial gradient fallback.
- Set CleanYourTV as default Home via `cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity`.
- Safely disable the stock launcher (com.google.android.apps.tv.launcherx) and setupwraith if confirmed on Google TV.

5. Push Custom 4K Wallpaper:
- Ask me for my wallpaper preference, or run `push_wallpaper_from_downloads.ps1` to auto-crop the newest downloaded wallpaper in 16:9 4K UHD (3840x2160) and push directly to the TV.

6. Visual Verification:
- After applying changes, inject remote D-pad key presses, capture screenshots from the TV via `screencap -p /data/local/tmp/verify.png`, pull and inspect the visual layout.
- Confirm that focus scaling, the floating dock, the top bar (Wi-Fi SSID, Inputs, Settings gear), and app scrolling are crisp, responsive, and glitch-free.
- Remind me to switch USB debugging OFF in TV Developer options once finished to restore 5V USB-OTG power for external hard drives.
```
