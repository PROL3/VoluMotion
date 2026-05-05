# 🔧 גיד התקנה מלא Step-by-Step

## שלב 1: הורדה והגדרה

### 1.1 Clone הפרויקט
```bash
cd ~/Desktop
git clone https://github.com/your-repo/HandGestureVolumeControl.git
cd HandGestureVolumeControl
```

### 1.2 בדוק Android Studio
- Open Android Studio
- File → Open → בחר תיקיה

## שלב 2: Sync Gradle

Android Studio יבקש בך לסנכרן את Gradle.

```bash
# או ידנית
./gradlew sync
```

## שלב 3: בדוק Dependencies

```bash
# רא את dependency tree
./gradlew dependencies

# צריך לראות:
# ✓ MediaPipe (hand detection)
# ✓ CameraX (camera access)
# ✓ AndroidX (lifecycle, services)
# ✓ Kotlin (language)
```

## שלב 4: בחר Device/Emulator

### עם Device
```bash
# חבר via USB
adb devices

# אם צריך ADB
adb tcpip 5555
adb connect <device-ip>:5555
```

### עם Emulator
- הפעל emulator עם Android 8.0+ (API 26+)
- אפשר כמו "Google Play" system image

## שלב 5: בנה את הפרויקט

```bash
# Debug build
./gradlew buildDebug

# או מ-Android Studio
Build → Make Project
```

## שלב 6: התקן על Device

```bash
# Install
./gradlew installDebug

# או
adb install app/build/outputs/apk/debug/app-debug.apk
```

## שלב 7: הרץ את האפליקציה

```bash
# Launch
./gradlew runDebug

# או
adb shell am start -n com.example.handgesturevolume/.ui.MainActivity
```

## שלב 8: אשר Permissions

בטלפון:
1. Settings → Apps → Hand Gesture Volume Control
2. Permissions:
   - ✓ Camera: Allow
   - ✓ Location: Allow (for driving mode)
   - ✓ Notifications: Allow

## שלב 9: בדוק פעולה

```
1. Open app
2. Toggle switch ON
3. Point hand at camera
4. Rotate hand clockwise → Volume should UP
5. Rotate hand counter-clockwise → Volume should DOWN
```

## שלב 10: Debug עם Logcat

```bash
# ראה logs בזמן אמת
adb logcat | grep "HandGestureService\|HandDetector\|Rotation"

# או מ-Android Studio
View → Tool Windows → Logcat
```

---

## ✅ Checklist סיום

- [ ] Project builds with no errors
- [ ] All dependencies downloaded
- [ ] App installs successfully
- [ ] All permissions granted
- [ ] Hand detection works
- [ ] Rotation detection works
- [ ] Volume changes when rotating
- [ ] Foreground notification shows
- [ ] Logs appear in Logcat

---

## 🆘 Troubleshooting

### Build Fails
```
❌ ERROR: Unsupported class-file format
✅ Solution: Update Java version to 11+
```

### MediaPipe Won't Load
```
❌ ERROR: Library libmediapipe not found
✅ Solution: Download MediaPipe AAR manually
   Or use older version (0.9.x)
```

### App Crashes on Start
```
❌ CRASH: SecurityException - Camera permission
✅ Solution: Request runtime permissions
   Or allow in Settings
```

### Hand Not Detected
```
❌ Hand detection confidence too low
✅ Solution: 
   - Better lighting
   - Move hand closer to camera
   - Check front camera works
```

### Logcat Spam
```
❌ Too many logs, can't see useful info
✅ Solution: Filter by tag
   adb logcat | grep "HandGestureService"
```

---

**שלמת! 🎉 האפליקציה פועלת!**
