יצרתי עבורך את קובץ ה-`README.md` באנגלית, מתורגם ומעוצב בצורה מקצועית התואמת את הסטנדרטים של GitHub.

```markdown
# 🎙️ Hand Gesture Volume Control

## 📋 Project Description

An Android application that detects hand rotation movements (clockwise/counter-clockwise) via the phone's camera to control system volume. This allows for touch-free operation, specifically designed for safe use while driving.

## ✨ Key Features

✅ **Real-time Hand Rotation Recognition**
- Powered by **MediaPipe** for high-precision hand tracking.
- Detects clockwise and counter-clockwise rotation.
- Noise filtering and false-positive prevention logic.

✅ **Volume Control**
- Seamlessly increases/decreases system audio levels.
- Stable and responsive feedback.
- Built-in 500ms cooldown to prevent excessive triggers.

✅ **Background Operation**
- Runs as a **Foreground Service** with a persistent Notification.
- Dynamic notification updates.
- Pause/Resume functionality from the UI or notification.

✅ **Driving Detection**
- Real-time GPS monitoring.
- Automatically activates monitoring when speed exceeds 5 km/h.
- Pauses monitoring when stationary to save resources.

✅ **User Interface**
- Simple and intuitive UI.
- Comprehensive permission management guide.
- Master On/Off toggle and Pause/Resume controls.

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│           MainActivity (UI)             │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────┐    │
│  │      HandGestureService         │    │
│  │      (Foreground Service)       │    │
│  ├─────────────────────────────────┤    │
│  │ • CameraX (Image Analysis)      │    │
│  │ • HandDetector (MediaPipe)      │    │
│  │ • RotationDetector              │    │
│  │ • VolumeController              │    │
│  │ • DrivingModeDetector (GPS)     │    │
│  └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘
```

## 📦 Project Structure

### Core Components
- **HandDetector.kt**: Handles hand landmark detection using MediaPipe.
- **RotationDetector.kt**: Calculates rotation direction using geometric angle analysis.
- **VolumeController.kt**: Interfaces with Android's AudioManager.
- **DrivingModeDetector.kt**: Monitors speed and movement via GPS.

### UI & Service
- **MainActivity.kt**: The main entry point and user interface.
- **HandGestureService.kt**: Manages background lifecycle and camera analysis.
- **activity_main.xml**: Application layout definition.

### Configuration
- **AndroidManifest.xml**: App declarations, services, and permissions.
- **build.gradle**: Dependencies and build configurations.
- **strings.xml**: Localization and UI text.

## 🔧 Technical Requirements

### Gradle & Build
```gradle
compileSdk 34
minSdk 26
targetSdk 34
```

### Libraries
```gradle
// CameraX - for camera access
implementation "androidx.camera:camera-core:1.3.0"
implementation "androidx.camera:camera-lifecycle:1.3.0"

// MediaPipe - for hand detection
implementation "com.google.mediapipe:solution-core:latest_version"
implementation "com.google.mediapipe:tasks-vision:latest_version"

// Android & Lifecycle
implementation "androidx.lifecycle:lifecycle-service:2.6.1"
implementation "androidx.appcompat:appcompat:1.6.1"
```

## 📱 Required Permissions

| Permission | Purpose |
|------------|---------|
| `CAMERA` | Accessing the front camera for gesture recognition. |
| `ACCESS_FINE_LOCATION` | Accurate speed detection for Driving Mode. |
| `ACCESS_COARSE_LOCATION` | Fallback for network-based location. |
| `FOREGROUND_SERVICE` | Keeping the app active in the background. |
| `ACCESS_NOTIFICATION_POLICY` | System-level volume adjustment permissions. |

## 🚀 Getting Started

### 1. Clone the Project
```bash
git clone <repo-url>
cd HandGestureVolumeControl
```

### 2. Build and Install
```bash
./gradlew build
./gradlew installDebug
```

### 3. Grant Permissions
- 📷 Camera
- 📍 Location
- 🔔 Notifications

### 4. How to Use
1. Open the app and toggle the **Master Switch** to ON.
2. Position your hand in front of the front camera.
3. **Rotate Right (CW)**: Volume Up ↑
4. **Rotate Left (CCW)**: Volume Down ↓

## 🔍 How the Rotation Detection Works

### Step 1: Hand Tracking
`Camera Stream → MediaPipe → 21 Hand Landmarks`

### Step 2: Angle Calculation
`Landmarks → Calculate relative angles of fingers around the palm center.`

### Step 3: Direction Analysis
`Compare Previous Angles vs. Current Angles → Determine Direction & Speed.`

### Step 4: Filtering & Smoothing
- **Motion Buffer**: Analyzes a 10-frame window.
- **Confidence**: Requires a minimum threshold of 0.6.
- **Cooldown**: 500ms delay between actions to ensure stability.

## ⚙️ Configuration & Tuning

### Rotation Logic (`RotationDetector.kt`)
```kotlin
private val bufferSize = 10 // Window size for average calculation
private const val threshold = 2f // Minimum degrees to trigger rotation
```

### Action Cooldown (`HandGestureService.kt`)
```kotlin
private val rotationCooldown = 500L // Delay in milliseconds between volume steps
```

### GPS Monitoring (`DrivingModeDetector.kt`)
```kotlin
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    1000, // 1 second interval
    10f,  // 10 meters distance interval
    locationListener
)
```

## 🎯 Performance Optimization

- **CPU Efficiency**: Single `ImageAnalysis` pipeline with GPU acceleration via MediaPipe.
- **Battery Conservation**: Uses a `LOW` importance Foreground Service and smart GPS polling intervals.
- **Memory Management**: Optimized Bitmap recycling and limited landmark buffers.
- **CameraX Strategy**: Uses `KEEP_ONLY_LATEST` backpressure strategy to prevent lag.

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Hand not detected** | Ensure adequate lighting and distance (30-50 cm from camera). |
| **Too many false positives** | Increase `bufferSize` (e.g., to 20) or increase the `threshold`. |
| **Service stops in background** | Check battery optimization settings and ensure `FOREGROUND_SERVICE` permission is granted. |
| **High battery drain** | Ensure GPS is set to "While using app" or increase `minTime` in GPS updates. |

## 📊 Logging

Monitor the app's behavior via Logcat:
```bash
adb logcat | grep -E "HandDetector|RotationDetector|VolumeController|DrivingMode"
```

## 📈 Future Roadmap

- [ ] Add support for additional gestures (Swipe, Pinch).
- [ ] Implement haptic/vibration feedback.
- [ ] Multilingual support (Hebrew, Spanish, etc.).
- [ ] Simultaneous left and right-hand support.
- [ ] ML-based custom gesture training.
- [ ] Customizable keybindings (e.g., Skip Track).

## 📄 License

This project integrates Google's MediaPipe solutions. Please refer to the MediaPipe license for core vision task usage.

---
**Created with ❤️ for touchless volume control while driving.**
```