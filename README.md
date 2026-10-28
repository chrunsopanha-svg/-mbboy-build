# MBBOY MLBB Smooth Tool (v0.1.0)

Floating orange MBBOY button + dark menu with troubleshooting guides for Mobile Legends.
Java, no libraries, no Internet permission, no ads, no analytics.

**Status: source code only. It has NOT been compiled or run on a device by the author of this file.**
Treat the first build as the first test.

## What it does / does not do
- Does: draw a draggable button, open guides, open Android settings screens, show a real one-time device snapshot.
- Does not: raise FPS, read other apps' FPS/RAM/CPU, measure ping, kill apps, change protected settings, touch MLBB.

## Project structure
```
MBBOYSmoothTool/
  settings.gradle, build.gradle, gradle.properties, .gitignore
  .github/workflows/build.yml          (free cloud build via GitHub Actions)
  app/build.gradle
  app/src/main/AndroidManifest.xml
  app/src/main/res/values/strings.xml
  app/src/main/res/drawable/ic_launcher.xml
  app/src/main/java/com/mbboy/smoothtool/
    MainActivity.java      start/stop/hide, overlay permission flow
    OverlayService.java    bubble, drag, menu, notification
    ToolboxActivity.java   renders guides, checklist, snapshot, settings shortcuts
    Guides.java            all guide text
    DeviceInfo.java        real readable metrics only
    SettingsLauncher.java  opens Android settings safely
    Ui.java                view helpers
```
No Gradle wrapper is included (binary file). The GitHub workflow installs Gradle 8.7 itself. In Android Studio the wrapper is generated for you.

## Build option A: phone only (free): GitHub Actions
1. Install Termux from F-Droid (not the old Play Store build) and a free GitHub account.
2. Create an empty repository on GitHub (github.com in your browser, "New repository").
3. Create a personal access token: GitHub > Settings > Developer settings > Fine-grained token, repo access to that repo, permission "Contents: Read and write".
4. In Termux:
   ```
   pkg install git unzip
   termux-setup-storage
   cp ~/storage/downloads/MBBOYSmoothTool.zip ~ && unzip MBBOYSmoothTool.zip && cd MBBOYSmoothTool
   git init && git branch -M main
   git add -A && git commit -m "first build"
   git remote add origin https://github.com/YOUR_NAME/YOUR_REPO.git
   git push -u origin main
   ```
   Use your username and the token as the password.
5. On GitHub open the Actions tab > "Build debug APK". Wait a few minutes.
6. Open the finished run > Artifacts > MBBOY-debug-apk, download the zip, extract app-debug.apk.
   (Alternative to Termux: upload files one by one with GitHub's "Add file > Create new file", typing the path with slashes. Tedious.)

## Build option B: Android Studio (needs a PC; free)
1. Install Android Studio. File > Open > MBBOYSmoothTool. Accept SDK downloads.
2. Build > Build Bundle(s) / APK(s) > Build APK(s).
3. APK: app/build/outputs/apk/debug/app-debug.apk.
Command line alternative: `gradle assembleDebug` (JDK 17 + Android SDK 34 required).

## Install on the OPPO A7
1. Copy app-debug.apk to the phone.
2. Open it in Files. Allow "Install unknown apps" for that file manager/browser when asked.
3. If ColorOS shows a security/virus-check screen, continue the install for a self-built debug APK.

## First run
1. Open MBBOY MLBB Smooth Tool, tap "Start floating button".
2. Read the explanation, tap Continue, switch on "Display over other apps" for this app, press Back.
3. Orange MBBOY button appears. Tap it for the menu; drag to move.
4. ColorOS may stop background apps. Settings > Battery (or App management) > this app > allow background activity / auto-launch if the button disappears.

## Testing checklist (OPPO A7)
- [ ] App installs and opens; no crash on launch.
- [ ] Guides open from the main screen; checklist boxes tick and Reset works.
- [ ] Tap Start with permission off: explanation dialog appears; "Not now" leaves overlay off with a message and no crash.
- [ ] Grant permission, return: overlay starts automatically.
- [ ] Deny permission (leave switch off, press Back): message shown, app still usable.
- [ ] Button is orange, circular, labelled MBBOY.
- [ ] Drag moves it smoothly and it stays on screen; a tap without moving opens the menu.
- [ ] Menu is dark, readable, and fits on screen; it closes with x, when dragging, or by tapping the button again.
- [ ] Each menu item opens its guide on top of the game; Back returns to the game.
- [ ] Hide removes the button; notification (or main screen) Show brings it back.
- [ ] "Close overlay" removes everything and the notification disappears.
- [ ] Rotate to landscape (in a game that supports it): button/menu remain on screen.
- [ ] Launch MLBB with the button visible: no crash, no lag you can attribute to the overlay, touches outside the button reach the game.
- [ ] Device snapshot values look plausible against Settings (battery %, storage).
- [ ] Settings shortcuts open the right screens, or fall back to main Settings with a message.
- [ ] After 10+ minutes in the game, the overlay is still alive.
- [ ] Revoke "Display over other apps" while running: overlay stops without crash.

## Troubleshooting build errors
| Symptom | Fix |
|---|---|
| "Unsupported class file major version" / JDK errors | Use JDK 17 for Gradle (Android Studio bundled JDK is fine). |
| "SDK location not found" | Create local.properties with `sdk.dir=/path/to/Android/Sdk` or open the project in Android Studio. |
| "Failed to find target with hash string 'android-34'" / license errors | SDK Manager > install Android SDK Platform 34 and Build-Tools 34.0.0; accept licenses (`sdkmanager --licenses`). |
| "Minimum supported Gradle version is 8.7" | Use Gradle 8.7+ (workflow already does) or let Android Studio upgrade the wrapper. |
| Could not resolve com.android.tools.build:gradle | Needs internet access to google(); retry or check proxy/VPN. |
| `Namespace not specified` | Ensure app/build.gradle has `namespace 'com.mbboy.smoothtool'`. |
| `cannot find symbol R` | Run Build > Rebuild; check res/ files are in the right folders and XML is valid. |
| `startForeground` / `ForegroundServiceStartNotAllowedException` / missing type on Android 14 | Keep FOREGROUND_SERVICE_SPECIAL_USE permission, foregroundServiceType="specialUse" and the <property> in the manifest. |
| Workflow fails at upload step | Check the build step above it; the APK path is app/build/outputs/apk/debug/app-debug.apk. |
| "App not installed" on phone | Uninstall any older copy signed with a different key; free some storage; re-download the APK. |
| Button never appears | Overlay permission off, or ColorOS killed the service: re-check both. |

If a build fails, copy the first "error:" or "What went wrong" lines from the log; they identify the cause.
