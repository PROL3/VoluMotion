# ⚡ Quick Reference Card

## 🎯 5-Minute Quick Start

```bash
cd VoluMotion
./gradlew build         # Build
./gradlew runDebug      # Run
```

✅ **Done!** App should open on your device.

---

## 🔧 Quick Commands

### Build
```bash
./gradlew build                 # Build APK
./gradlew buildRelease          # Release build
./gradlew clean                 # Clean build files
```

### Run
```bash
./gradlew runDebug              # Build + Install + Run
./gradlew installDebug          # Install only
./gradlew installRelease        # Install release
```

### Debug
```bash
adb logcat                      # View all logs
adb logcat | grep Hand          # Filter by "Hand"
adb devices                     # List connected devices
adb shell "input keyevent 82"   # Open menu on device
```

---

## 🎨 UI Customization

### Change Colors
📁 `res/values/colors.xml`
```xml
<color name="purple_500">#FF6200EE</color>  <!-- Change this -->
```

### Change Text
📁 `res/values/strings.xml`
```xml
<string name="app_name">Hand Gesture Volume Control</string>
```

### Change Layout
📁 `res/layout/activity_main.xml`
- Toggle position
- Button text
- Colors

---

## 🔌 Adjust Detection

### Hand Detection Sensitivity
📁 `core/HandDetector.kt`
```kotlin
const val MAX_HANDS = 1              // Number of hands to detect
RUN_ON_GPU = true                   // GPU acceleration
```

### Rotation Sensitivity
📁 `core/RotationDetector.kt`
```kotlin
private val bufferSize = 10         // Increase = more stable
const val ANGLE_THRESHOLD = 2f      // Increase = less sensitive
const val CONFIDENCE_THRESHOLD = 0.6f  // Increase = more strict
```

### Volume Control Speed
📁 `service/HandGestureService.kt`
```kotlin
private val rotationCooldown = 500L  // Increase = slower
```

### GPS Detection Sensitivity
📁 `utils/DrivingModeDetector.kt`
```kotlin
const val DRIVING_SPEED_THRESHOLD = 5f  // km/h (increase = less sensitive)
locationManager.requestLocationUpdates(
    ...,
    1000,  // milliseconds between updates
    10f,   // meters displacement
    ...
)
```

---

## 🔐 Permissions (AndroidManifest.xml)

```xml
<!-- Required Permissions -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />

<!-- Optional Features -->
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.location.gps" android:required="false" />
```

---

## 📊 Architecture Overview

```
┌──────────────┐
│  MainActivity│◄─── User toggles
└──────┬───────┘
       │
       ▼
┌──────────────────────────────┐
│  HandGestureService          │
│  (Foreground Service)        │
├──────────────────────────────┤
│  ├─ CameraX (30 FPS)         │
│  ├─ HandDetector (MediaPipe) │
│  ├─ RotationDetector         │
│  ├─ VolumeController         │
│  └─ DrivingModeDetector      │
└──────────────────────────────┘
```

---

## 🧠 Main Components

| Component | Role | Key File |
|-----------|------|----------|
| **HandDetector** | Find 21 hand points | `core/HandDetector.kt` |
| **RotationDetector** | Detect rotation | `core/RotationDetector.kt` |
| **VolumeController** | Change volume | `utils/VolumeController.kt` |
| **DrivingModeDetector** | GPS tracking | `utils/DrivingModeDetector.kt` |
| **HandGestureService** | Main logic | `service/HandGestureService.kt` |
| **MainActivity** | UI | `ui/MainActivity.kt` |

---

## 📱 File Locations Cheat Sheet

```
Source Code:
  app/src/main/java/com/example/handgesturevolume/
  ├── core/HandDetector.kt
  ├── core/RotationDetector.kt
  ├── service/HandGestureService.kt
  ├── ui/MainActivity.kt
  └── utils/

Resources:
  app/src/main/res/
  ├── layout/activity_main.xml
  ├── drawable/*.xml
  └── values/strings.xml

Configuration:
  ├── app/build.gradle
  ├── AndroidManifest.xml
  └── gradle.properties
```

---

## 🚨 Common Issues & Fixes

### "Hand not detected"
→ Check lighting (300+ lux)
→ Position hand 30-50cm away
→ Try rotating hand slowly

### "Volume changes too fast"
→ Increase `rotationCooldown` to 1000ms

### "Volume doesn't respond"
→ Check Volume is not muted
→ Verify MODIFY_AUDIO_SETTINGS permission

### "Battery drains fast"
→ Disable GPS when not driving
→ Reduce update frequency

### "App crashes"
→ Check Logcat: `adb logcat | grep Error`
→ Ensure all permissions granted

---

## 📝 Key Parameters Tuning

```kotlin
// CONSERVATIVE (Most stable, slower response)
bufferSize = 20
ANGLE_THRESHOLD = 5f
CONFIDENCE_THRESHOLD = 0.8f
rotationCooldown = 1000L

// BALANCED (Default)
bufferSize = 10
ANGLE_THRESHOLD = 2f
CONFIDENCE_THRESHOLD = 0.6f
rotationCooldown = 500L

// AGGRESSIVE (Most responsive, may have false positives)
bufferSize = 5
ANGLE_THRESHOLD = 1f
CONFIDENCE_THRESHOLD = 0.4f
rotationCooldown = 300L
```

---

## 🎯 Testing Checklist

- [ ] Hand detection works
- [ ] Volume UP on clockwise rotation
- [ ] Volume DOWN on counter-clockwise rotation
- [ ] Foreground notification visible
- [ ] Pause/Resume buttons work
- [ ] GPS driving mode detects
- [ ] No crashes in Logcat
- [ ] Battery acceptable after 1 hour
- [ ] All permissions work
- [ ] Different lighting conditions tested

---

## 📚 Documentation Files

| File | When to Read |
|------|---|
| README.md | Quick overview |
| SETUP_GUIDE.md | Installation help |
| IMPLEMENTATION_GUIDE.md | How it works |
| CODE_EXAMPLES.md | Code snippets |
| ADVANCED_CONFIG.md | Tuning help |
| FAQ.md | Problem solving |
| PROJECT_OVERVIEW.md | Architecture |

---

## 🔗 Important Classes

```kotlin
// Hand Detection
HandDetector.detectHand(bitmap): HandLandmarks

// Rotation Detection
RotationDetector.detectRotation(landmarks, confidence): RotationEvent
RotationDetector.getAverageRotation(): RotationEvent?

// Volume Control
VolumeController.volumeUp()
VolumeController.volumeDown()

// Driving Detection
DrivingModeDetector.addListener(listener)
DrivingModeDetector.isDrivingMode(): Boolean
```

---

## 🎨 UI Elements

```xml
<!-- Main Switch -->
<androidx.appcompat.widget.SwitchCompat 
    android:id="@+id/toggleSwitch" />

<!-- Pause Button -->
<Button android:id="@+id/pauseButton" />

<!-- Status Text -->
<TextView android:id="@+id/statusText" />
```

---

## ⏱️ Performance Targets

```
Hand Detection:     30-35 FPS (33ms per frame)
Rotation Detection: Real-time (1-2ms)
Volume Response:    <100ms
Memory:             80-120 MB
Battery (GPS):      8-12% per hour
```

---

## 🆘 Debug Commands

```bash
# View logs
adb logcat

# Filter logs
adb logcat | grep "HandDetector"
adb logcat | grep "Error"

# Save logs
adb logcat > debug.log

# Clear logs
adb logcat -c

# List packages
adb shell pm list packages | grep hand

# View app crash
adb logcat | grep "FATAL"
```

---

## 🚀 Deploy Steps

```
1. ./gradlew clean          # Clean
2. ./gradlew build          # Build
3. Check APK in app/build/outputs/apk/
4. Sign APK (if release)
5. Upload to Play Store
```

---

## 📞 Quick Help

**Stuck?** Check these in order:
1. README.md (overview)
2. SETUP_GUIDE.md (installation)
3. FAQ.md (common issues)
4. Logcat (errors)
5. CODE_EXAMPLES.md (how to use)

---

**Version: 1.0 | May 2026**

**Save this card! You'll need it. 💾**
