---
name: testing-banking-app
description: Test the Banking App Mock Compose Android app end-to-end. Use when verifying UI features, form flows, or navigation changes.
---

# Testing Banking App Mock Compose

## Prerequisites

- Android SDK and emulator configured
- APK built via `./gradlew assembleDebug`
- Output APK at `app/build/outputs/apk/debug/app-debug.apk`
- Debug package name: `by.alexandr7035.banking.debug`
- Main activity: `by.alexandr7035.banking.MainActivity`

## Devin Secrets Needed

No secrets required. The app uses hardcoded mock credentials.

## Emulator Setup

1. Start emulator (if not already running): check `adb devices`
2. Disable animations for reliable testing:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```
3. Install APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
4. Launch app:
   ```bash
   adb shell am start -n by.alexandr7035.banking.debug/by.alexandr7035.banking.MainActivity
   ```

## Login Flow

The app has a first-launch wizard:
1. **Onboarding screens** — look for "Skip This Step" button and tap it
2. **Login screen** — credentials are pre-filled (email: `example@mail.com`, password: `1234567Ab`). Tap "Sign In"
3. **OTP screen** — if shown, enter `1111`
4. **Create PIN** — enter any 4-digit PIN (e.g., 1-2-3-4)
5. **Confirm PIN** — re-enter the same PIN
6. **Enable Biometrics** — tap "Skip This Step"
7. You should now see the Home screen with "Welcome Back!" header

## Navigation

- **Bottom nav**: Home (1st), History (2nd), Profile (3rd)
- **Profile screen**: Shows profile card with edit icon, QR options, Help/Privacy, App Settings, Log out
- **Edit Profile**: Tap the small edit icon on the profile card avatar

## ADB Interaction Tips

The emulator might be slow due to nested virtualization. Use these strategies:

### Use UI Automator for exact coordinates
```bash
adb shell uiautomator dump /sdcard/ui.xml
adb shell cat /sdcard/ui.xml | grep -o 'text="TARGET_TEXT"[^/]*bounds="[^"]*"'
```
Extract center coordinates from bounds `[x1,y1][x2,y2]` → tap at `((x1+x2)/2, (y1+y2)/2)`

### Tap elements reliably
```bash
adb shell input tap X Y
```
Add `sleep 2-3` between taps on slow emulators.

### Text input in EditText fields
1. Tap the field to focus it
2. Move cursor to end: `adb shell input keyevent KEYCODE_MOVE_END`
3. Delete existing text character by character:
   ```bash
   for i in $(seq 1 20); do adb shell input keyevent 67; sleep 0.2; done
   ```
4. Type new text: `adb shell input text "new_value"`

**Important**: Triple-tap select-all and Ctrl+A might not work reliably in Compose EditText fields. The delete-character-by-character approach is more reliable.

### Dismiss keyboard
```bash
adb shell input keyevent 111  # KEYCODE_ESCAPE
```

### Dismiss ANR dialogs
```bash
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS
```

### Get screen resolution
```bash
adb shell wm size
```

## Mock Data

- Default profile: Alexander Michael, email test@example.com, nickname @alexandermichael
- Profile tier: BASIC
- Data persists in-memory only (lost on app restart)
- Profile picture URL uses a placeholder image

## Common Issues

- **ANR dialogs**: The emulator may show "App isn't responding" dialogs due to nested virtualization. Dismiss with the broadcast command above.
- **Lost taps**: If taps don't register, increase sleep duration between commands. Use UI Automator dump to verify exact coordinates.
- **Activity not found**: Use `by.alexandr7035.banking.MainActivity` (not `AppHostActivity`). Find activities with:
  ```bash
  adb shell dumpsys package by.alexandr7035.banking.debug | grep -A5 "android.intent.action.MAIN"
  ```
- **Build issues**: The app requires a `keystore.properties` file. If missing, the build.gradle.kts has a fallback that uses empty strings for signing config.
