# 📚 Detailed Implementation Guide

## 1️⃣ Initial Setup

### Cloning the Project
```bash
# Clone or download the project
git clone https://github.com/PROL3/VoluMotion
cd VoluMotion

# Open in Android Studio
# File → Open → Select the project folder
```

### Development Environment Checklist
```
✓ Android Studio Hedgehog+
✓ SDK 34 installed
✓ NDK (Required for CameraX and MediaPipe)
✓ Latest Kotlin Plugin
✓ Physical device or emulator with Android 8.0+
```

## 2️⃣ Core Dependencies

### MediaPipe
MediaPipe is a Google framework providing optimized models for hand tracking.


**Current Stable Versions:**
```gradle
implementation 'com.google.mediapipe:solution-core:0.10.11'
implementation 'com.google.mediapipe:tasks-vision:0.10.11'
```

**Key Responsibilities:**
- Detects 21 hand landmarks.
- Processes each camera frame in real-time.
- Provides a confidence score for each detection.

### CameraX
Part of Android Jetpack, CameraX simplifies real-time camera access.
```gradle
implementation 'androidx.camera:camera-core:1.2.3'
implementation 'androidx.camera:camera-lifecycle:1.2.3'
```

### Lifecycle Service
Essential for background processing using a Foreground Service.
```gradle
implementation 'androidx.lifecycle:lifecycle-service:2.6.1'
```

---

## 3️⃣ Hand Tracking - HandDetector

### Workflow
```kotlin
// 1. Initialization (within Service onCreate)
val handDetector = HandDetector(context)

// 2. Process each frame
val handLandmarks = handDetector.detectHand(bitmap)

if (handLandmarks != null && handLandmarks.confidence > 0.5f) {
    // Hand detected in frame!
    println("Found hand with ${handLandmarks.landmarks.size} points")
}

// 3. Resource Cleanup (within onDestroy)
handDetector.release()
```

### HandDetector Output Data
- **Handedness**: "Right" or "Left".
- **Landmarks**: A list of 21 `PointF` objects representing coordinates.
- **Confidence**: A float value (e.g., 0.92f).

### The 21 Hand Landmarks

- **WRIST** (0)
- **THUMB**: TIP (4)
- **INDEX**: TIP (8)
- **MIDDLE**: TIP (12)
- **RING**: TIP (16)
- **PINKY**: TIP (20)
- *Includes 16 intermediate points for finger joints.*

---

## 4️⃣ Rotation Recognition - RotationDetector

### Mechanics
The core logic relies on geometric tracking:
1. **Calculate Angle**: Measure the angle of each finger relative to the palm center.
2. **Compare Frames**: Track angle changes between consecutive frames.
3. **Determine Direction**: Consistent change in one direction triggers a **Clockwise** or **Counter-Clockwise** event.

### Angle Calculation Algorithm
```
For each landmark point:
    1. Calculate (dx, dy) = point - palmCenter
    2. angle = atan2(dy, dx) * 180 / π
    3. Normalize to 0-360 degrees

Compare angles:
    angleDiff = currentAngle - previousAngle
    if angleDiff > 2°: Clockwise rotation
    if angleDiff < -2°: Counter-clockwise rotation
```

---

## 5️⃣ Volume Management - VolumeController

### Basic Usage
```kotlin
val volumeController = VolumeController(context)

volumeController.volumeUp()    // Increase
volumeController.volumeDown()  // Decrease
volumeController.toggleMute()  // Mute/Unmute

val currentLevel = volumeController.getCurrentVolume() // Returns 0-1
```

### Internal Implementation
```kotlin
audioManager.adjustStreamVolume(
    AudioManager.STREAM_MUSIC,
    AudioManager.ADJUST_RAISE,  // or ADJUST_LOWER
    AudioManager.FLAG_SHOW_UI   // Displays the system volume slider
)
```

---

## 6️⃣ Driving Mode - DrivingModeDetector

### Logic
Uses GPS to monitor speed. If speed > 5 km/h, the app assumes the user is driving.
```kotlin
val drivingDetector = DrivingModeDetector(context)

drivingDetector.addListener { isDriving ->
    if (isDriving) {
        // Resume rotation detection
    } else {
        // Pause detection to conserve battery
    }
}

drivingDetector.startMonitoring()
```

---

## 7️⃣ Foreground Service - HandGestureService

### Key Characteristics
- Runs in the background even if the UI is closed.
- Displays a persistent notification.
- Acts as the "Brain" connecting the Camera, MediaPipe, and Volume Controller.

### Service Lifecycle
1. **onCreate()**: Initialize components.
2. **onStartCommand()**: Begin detection logic.
3. **startForeground()**: Display the required persistent notification.
4. **processImage()**: Loop for frame analysis.
5. **onDestroy()**: Release camera and detector resources.

---

## 8️⃣ MainActivity - User Interface

| Element | Action |
|---------|-------|
| **Master Toggle** | Starts/Stops the `HandGestureService`. |
| **Pause Button** | Temporarily suspends monitoring. |
| **Status Text** | Displays "Driving," "Stationary," or "Hand Detected." |

---

## 9️⃣ Troubleshooting & Debugging

### Using Logcat
```bash
# Filter logs for specific components
adb logcat | grep "HandDetector\|RotationDetector\|VolumeController"
```

### Common Issues & Solutions
- **No Hand Detection**: Check lighting; ensure the hand is 30-50cm from the camera.
- **False Positives**: Increase the `confidence` threshold in settings.
- **Service Crashing**: Verify `CAMERA` and `FOREGROUND_SERVICE` permissions in the Manifest.
- **High Battery Drain**: Use `NETWORK_PROVIDER` for location or increase the GPS update interval.

---

## 🔜 Next Steps

1. **Build**: Run `./gradlew build`.
2. **Deploy**: Run `./gradlew installDebug`.
3. **Permissions**: Grant Camera, Location, and Notification access on the device.
4. **Test**: Rotate your hand in front of the front-facing camera.
5. **Tune**: Adjust the rotation `cooldown` and `ANGLE_THRESHOLD` for your specific hardware.

---

**Last Updated: May 2026**