# 📋 Project Overview

## 🎯 Project Objective

**Hand Gesture Volume Control** is an Android application that enables volume control through natural hand gestures—eliminating the need to touch the phone. This is particularly beneficial for safe operation while driving.

### User Focus
- **Drivers:** Control volume without visual or physical distractions.
- **Hands-Free Environments:** Users in conditions where touching the device is impossible or inconvenient.

### Core Technologies
- **MediaPipe:** Real-time hand landmark detection.
- **CameraX:** Efficient front-facing camera access.
- **Android Location:** GPS-based driving detection.
- **AudioManager:** System-level volume orchestration.

---

## 📁 Project Structure

```
HandGestureVolumeControl/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/handgesturevolume/
│   │   │   ├── core/
│   │   │   │   ├── HandDetector.kt         📸 Hand detection logic
│   │   │   │   └── RotationDetector.kt     🔄 Rotation logic
│   │   │   ├── service/
│   │   │   │   └── HandGestureService.kt   🎛️ Foreground Service
│   │   │   ├── ui/
│   │   │   │   └── MainActivity.kt         📱 Main UI
│   │   │   └── utils/
│   │   │       ├── VolumeController.kt     🔊 Volume interface
│   │   │       ├── DrivingModeDetector.kt  🗺️ GPS driving logic
│   │   │       └── PermissionManager.kt    🔐 Permissions handler
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml       📋 Layout XML
│   │   │   ├── drawable/                   🎨 UI Assets
│   │   │   └── values/                     📄 Strings & Themes
│   │   └── AndroidManifest.xml             ⚙️ App Manifest
│   ├── build.gradle                        📦 Module Dependencies
│   └── proguard-rules.pro                  🔒 Obfuscation rules
├── README.md                               📖 General Overview
├── SETUP_GUIDE.md                          🚀 Installation Guide
├── IMPLEMENTATION_GUIDE.md                 📚 Technical Guide
├── CODE_EXAMPLES.md                        🧪 Code Snippets
├── ADVANCED_CONFIG.md                      ⚙️ Tuning & Parameters
└── FAQ.md                                  ❓ Common Questions
```

---

## 🔄 Data Flow

### Rotation Detection Process



```
┌─────────────────────────┐
│  Camera Frame (30 FPS)  │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  HandDetector           │
│  - 21 Landmarks         │
│  - Confidence Scored    │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  RotationDetector       │
│  - Calculate Angles     │
│  - Buffer (10 frames)   │
│  - Direction Logic      │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Cooldown Check (500ms) │
└────────────┬────────────┘
             │
        ┌────┴───────┐
        ▼            ▼
  ╔──────────╗  ╔─────────────╗
  │ Clockwise│  │ Counter-CW  │
  ║ Vol UP   │  │ Vol DOWN    │
  ╚──────────╝  ╚─────────────╝
```

### Driving Detection Flow

```
┌─────────────────────────┐
│  GPS Location Updates   │
│  (1 Second Interval)    │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Speed Calculation      │
│  Threshold: 5 km/h      │
└────────────┬────────────┘
             │
        ┌────┴──────────┐
        ▼               ▼
  ╔───────────╗   ╔────────────╗
  │  DRIVING  │   │ STATIONARY │
  ║ = ACTIVE  │   │ = PAUSED   │
  ╚───────────╝   ╚────────────╝
```

---

## ⚙️ Components & Responsibilities

| Component | Role | Input | Output |
|-----------|------|-------|--------|
| **HandDetector** | Landmark extraction | Bitmap | 21 Landmarks + Confidence |
| **RotationDetector**| Direction analysis | Landmarks Array | Direction + Angle |
| **VolumeController**| Audio interface | UP/DOWN Command | System volume change |
| **DrivingModeDetector**| State detection | GPS Location | isDriving: Boolean |
| **HandGestureService**| Core Coordinator | Camera frames | Orchestrates full pipeline |
| **MainActivity** | User Interface | User Toggles | Service state control |

---

## 📊 Performance Targets

| Metric | Target | Current |
|--------|--------|---------|
| Hand Detection FPS | 30+ | 30-35 FPS |
| Recognition Latency| Real-time | 16-33ms |
| Volume Response | <100ms | 50-100ms |
| Memory Usage | <150MB | 80-120MB |
| Battery (GPS ON) | <15%/hour | 8-12%/hour |

---

## 🔐 Permissions Overview

1. **CAMERA** (`android.permission.CAMERA`)
   - **Risk:** Dangerous | **Purpose:** Accessing the front camera for detection.
2. **LOCATION** (`ACCESS_FINE_LOCATION`)
   - **Risk:** Dangerous | **Purpose:** Calculating speed for auto-driving mode.
3. **FOREGROUND_SERVICE**
   - **Risk:** Normal | **Purpose:** Sustaining background operations with a notification.
4. **NOTIFICATION_POLICY**
   - **Risk:** Normal | **Purpose:** Permission to override/adjust system volume.

---

## 📈 Roadmap

### Phase 1 (Current)
- ✅ Hand rotation detection (CW/CCW).
- ✅ System volume integration.
- ✅ GPS driving mode detection.
- ✅ Reliable Foreground Service.

### Phase 2 (Planned)
- [ ] New Gestures: Swipe (Next/Prev track) and Pinch (Mute).
- [ ] Haptic (vibration) feedback on successful detection.
- [ ] Machine Learning model refinement for low-light conditions.

---

## ❓ Frequently Asked Questions

**Q: Why use MediaPipe instead of OpenCV?**
A: MediaPipe is native to mobile, utilizes GPU acceleration out of the box, and is significantly lighter on system resources for hand tracking.

**Q: Can I use the back camera?**
A: Technically yes, by changing the config to `BACK_CAMERA`, but it is less practical for driver-facing use cases.

**Q: How do I reduce battery consumption?**
A: You can decrease the GPS update frequency or switch to `NETWORK_PROVIDER` in the `ADVANCED_CONFIG.md` settings.

---

**Built with ❤️ for drivers who prioritize safety and control.**