# 📑 File Index

## 🗂️ Project Structure

```
HandGestureVolumeControl/
│
├── 📄 BUILD & CONFIG FILES
│   ├── build.gradle                    [Root build configuration]
│   ├── settings.gradle                 [Project module configuration]
│   ├── gradle.properties               [Build optimization settings]
│   ├── gradlew                         [Unix Gradle wrapper script]
│   ├── gradlew.bat                     [Windows Gradle wrapper script]
│   ├── .gitignore                      [Git ignore rules]
│   └── gradle/libs.versions.toml       [Dependency version pins]
│
├── 📱 ANDROID MANIFEST
│   └── app/src/main/AndroidManifest.xml
│       - Permissions (Camera, Location, Foreground Service)
│       - Activities & Services declaration
│       - Feature requirements
│
├── 💻 KOTLIN SOURCE CODE
│   └── app/src/main/java/com/example/handgesturevolume/
│       │
│       ├── core/
│       │   ├── HandDetector.kt        [MediaPipe integration]
│       │   │   - Detects 21 hand landmarks
│       │   │   - Confidence scoring
│       │   │   - Motion velocity calculation
│       │   │
│       │   └── RotationDetector.kt    [Gesture recognition engine]
│       │       - Angle calculation
│       │       - Rotation direction detection
│       │       - Buffering & averaging
│       │       - CW/CCW determination
│       │
│       ├── service/
│       │   └── HandGestureService.kt  [Foreground service]
│       │       - CameraX integration
│       │       - Image processing loop
│       │       - Foreground notification
│       │       - GPS monitoring
│       │       - Volume control execution
│       │
│       ├── ui/
│       │   └── MainActivity.kt        [User interface]
│       │       - Service start/stop
│       │       - Pause/resume functionality
│       │       - Permission handling
│       │       - Status display
│       │
│       └── utils/
│           ├── VolumeController.kt    [Audio control]
│           │   - Volume up/down
│           │   - Mute/unmute
│           │   - Current volume getter
│           │
│           ├── DrivingModeDetector.kt [GPS-based driving detection]
│           │   - Location monitoring
│           │   - Speed calculation
│           │   - Listener callbacks
│           │
│           └── PermissionManager.kt   [Permission handling]
│               - Runtime permission checks
│               - Permission requests
│               - Required permissions list
│
├── 🎨 RESOURCES
│   └── app/src/main/res/
│       │
│       ├── layout/
│       │   └── activity_main.xml      [UI layout]
│       │       - Toggle switch
│       │       - Pause button
│       │       - Status text
│       │       - Permission info
│       │       - Tips section
│       │
│       ├── drawable/
│       │   ├── rounded_background.xml [Card style]
│       │   └── button_background.xml  [Button style]
│       │
│       └── values/
│           ├── strings.xml            [Text strings]
│           ├── colors.xml             [Color definitions]
│           └── themes.xml             [App theme]
│
├── 📦 BUILD
│   ├── app/build.gradle               [App-level configuration]
│   │   - Dependencies
│   │   - SDK versions
│   │   - Build options
│   │   - Feature flags
│   │
│   └── app/proguard-rules.pro         [ProGuard obfuscation rules]
│       - MediaPipe rules
│       - CameraX rules
│       - Keep annotations
│
└── 📚 DOCUMENTATION
    ├── README.md                      [Project overview & features]
    ├── SETUP_GUIDE.md                 [Step-by-step installation guide]
    ├── IMPLEMENTATION_GUIDE.md        [Technical implementation details]
    ├── CODE_EXAMPLES.md               [Code snippets & examples]
    ├── ADVANCED_CONFIG.md             [Tuning & optimization guide]
    ├── FAQ.md                         [Frequently asked questions]
    ├── PROJECT_OVERVIEW.md            [Architecture & design patterns]
    ├── COMPLETE_SUMMARY.md            [Full project summary]
    └── FILE_INDEX.md                  [This file]
```

---

## 📄 File Descriptions

### Configuration Files

| File | Purpose | Size |
|------|---------|------|
| `build.gradle` | Root Gradle config | 300B |
| `app/build.gradle` | App dependencies & config | 1.2K |
| `settings.gradle` | Module configuration | 400B |
| `gradle.properties` | Build properties | 200B |
| `gradle/libs.versions.toml` | Dependency versions | 600B |
| `.gitignore` | Git exclusions | 500B |

### Source Code Files

| File | Lines | Purpose |
|------|-------|---------|
| `HandDetector.kt` | 150 | MediaPipe integration |
| `RotationDetector.kt` | 200 | Gesture recognition |
| `HandGestureService.kt` | 350 | Main service logic |
| `MainActivity.kt` | 100 | UI Activity |
| `VolumeController.kt` | 80 | Audio control |
| `DrivingModeDetector.kt` | 100 | GPS tracking |
| `PermissionManager.kt` | 60 | Permissions handling |
| **TOTAL** | **1,040** | |

### Resource Files

| File | Type | Purpose |
|------|------|---------|
| `activity_main.xml` | Layout | Main UI |
| `rounded_background.xml` | Drawable | Card styling |
| `button_background.xml` | Drawable | Button styling |
| `strings.xml` | Values | Text strings |
| `colors.xml` | Values | Color palette |
| `themes.xml` | Values | App theme |

### Documentation Files

| File | Topics | Size |
|------|--------|------|
| README.md | Overview, features, architecture | 5KB |
| SETUP_GUIDE.md | Installation, checklist, troubleshooting | 8KB |
| IMPLEMENTATION_GUIDE.md | Technical details, usage examples | 12KB |
| CODE_EXAMPLES.md | Code snippets, patterns | 10KB |
| ADVANCED_CONFIG.md | Tuning, performance, security | 6KB |
| FAQ.md | Q&A, debugging, support | 7KB |
| PROJECT_OVERVIEW.md | Architecture, roadmap, metrics | 8KB |
| COMPLETE_SUMMARY.md | Full summary, checklist | 7KB |

---

## 🔍 Quick File Lookup

### By Purpose

#### Hand Detection
- `core/HandDetector.kt` - Main detection
- `IMPLEMENTATION_GUIDE.md` - How it works

#### Gesture Recognition
- `core/RotationDetector.kt` - Rotation logic
- `CODE_EXAMPLES.md` - Usage examples

#### Volume Control
- `utils/VolumeController.kt` - Volume API
- `CODE_EXAMPLES.md` - Integration examples

#### Background Service
- `service/HandGestureService.kt` - Service logic
- `README.md` - Architecture diagram

#### User Interface
- `ui/MainActivity.kt` - UI logic
- `res/layout/activity_main.xml` - UI layout

#### Configuration
- `app/build.gradle` - Dependencies
- `AndroidManifest.xml` - Permissions
- `ADVANCED_CONFIG.md` - Tuning

---

## 🎯 By Development Task

### Setting Up Project
1. Read: `README.md`
2. Follow: `SETUP_GUIDE.md`
3. Edit: `app/build.gradle` (if needed)
4. Check: `gradle.properties`

### Understanding Code
1. Start: `PROJECT_OVERVIEW.md`
2. Read: `IMPLEMENTATION_GUIDE.md`
3. Look: `CODE_EXAMPLES.md`
4. Study: `core/*.kt` files

### Customizing App
1. UI Changes: `res/layout/activity_main.xml`, `res/values/*`
2. Logic Changes: `core/*.kt`
3. Tuning: `ADVANCED_CONFIG.md`
4. Debugging: `FAQ.md`

### Debugging Issues
1. Check: `FAQ.md`
2. Look: `ADVANCED_CONFIG.md`
3. Trace: Logcat output
4. Study: Relevant `.kt` file

### Building/Publishing
1. Check: `app/build.gradle`
2. Review: `app/proguard-rules.pro`
3. Run: `./gradlew build`
4. Sign: APK before release

---

## 📊 File Statistics

### By Type
```
Kotlin Files:        7 files (1,040 lines)
XML Files:           6 files (200 lines)
Gradle Files:        4 files (1.5 KB)
Markdown Files:      9 files (5,000+ lines)
Other:               3 files
────────────────────────────────
Total:              29 files
```

### By Size
```
Documentation:      ~50% of total content
Source Code:        ~20% of total content
Configuration:      ~5% of total content
Resources:          ~25% of total content
```

### Dependency Graph
```
HandGestureService
    ├── HandDetector (MediaPipe)
    │   └── mediapipe:solution-core
    │   └── mediapipe:tasks-vision
    ├── RotationDetector
    ├── VolumeController (AudioManager)
    ├── DrivingModeDetector (LocationManager)
    │   └── android.location.*
    └── CameraX
        ├── camera:core
        └── camera:lifecycle
```

---

## 🔗 File Dependencies

```
AndroidManifest.xml
    ├── MainActivity
    └── HandGestureService

MainActivity
    ├── PermissionManager
    └── HandGestureService

HandGestureService
    ├── HandDetector
    │   └── MediaPipe
    ├── RotationDetector
    ├── VolumeController
    └── DrivingModeDetector

CameraX
    └── HandGestureService
```

---

## 📝 File Editing Guide

### Safe to Modify
- ✅ `app/build.gradle` - Add dependencies
- ✅ `app/src/main/res/` - UI customization
- ✅ `core/*.kt` - Logic adjustments
- ✅ `ADVANCED_CONFIG.md` - Tuning values

### Don't Touch Without Reason
- ⚠️ `AndroidManifest.xml` - Unless adding permissions
- ⚠️ `settings.gradle` - Unless adding modules
- ⚠️ `proguard-rules.pro` - Unless publishing

### Read Only (Documentation)
- 🔒 `README.md`
- 🔒 `SETUP_GUIDE.md`
- 🔒 `FAQ.md`
- 🔒 All other `.md` files

---

## 🚀 Quick Actions

### Build
```bash
./gradlew build
# Compiles all files in:
# - app/src/main/java/**
# - app/src/main/res/**
# Reads from: build.gradle, AndroidManifest.xml
```

### Run
```bash
./gradlew runDebug
# Uses: app/build.gradle
# Deploys: app/src/main/AndroidManifest.xml
```

### Install
```bash
./gradlew installDebug
# Reads: build.gradle, manifest
# Installs: APK to device
```

### Debug
```bash
adb logcat
# Outputs from: Log.d() calls in *.kt files
# Check: HandDetector, RotationDetector, VolumeController
```

---

## 💾 Version Control

### Files to Commit
- ✅ All `.kt` files
- ✅ All `gradle*` files
- ✅ `AndroidManifest.xml`
- ✅ All resource files
- ✅ All `.md` files

### Files to NOT Commit
- ❌ `build/` directory
- ❌ `.idea/` directory
- ❌ `*.iml` files
- ❌ `local.properties`
- ❌ `.gradle/` directory

### .gitignore
Included file handles all exclusions automatically

---

## 📚 Documentation Quick Links

### Getting Started
1. **README.md** - Start here!
2. **SETUP_GUIDE.md** - Installation steps
3. **CODE_EXAMPLES.md** - See it in action

### Deep Dive
1. **IMPLEMENTATION_GUIDE.md** - How it works
2. **PROJECT_OVERVIEW.md** - Architecture
3. **ADVANCED_CONFIG.md** - Fine tuning

### Reference
1. **FAQ.md** - Common questions
2. **COMPLETE_SUMMARY.md** - Full overview
3. **FILE_INDEX.md** - This file!

---

## 🆘 Finding What You Need

### I want to...

**...understand the project**
→ Start with: README.md → PROJECT_OVERVIEW.md

**...install it quickly**
→ Follow: SETUP_GUIDE.md

**...see code examples**
→ Look at: CODE_EXAMPLES.md

**...modify the UI**
→ Edit: `res/layout/activity_main.xml`

**...improve performance**
→ Read: ADVANCED_CONFIG.md

**...solve a problem**
→ Search: FAQ.md

**...understand hand detection**
→ Study: IMPLEMENTATION_GUIDE.md + core/HandDetector.kt

**...adjust rotation detection**
→ Tune: core/RotationDetector.kt parameters

---

## 📱 File Checklist

Before deployment, verify:
- [ ] All `.kt` files compile
- [ ] `AndroidManifest.xml` has all permissions
- [ ] `build.gradle` has latest dependencies
- [ ] `proguard-rules.pro` is complete
- [ ] All resources exist in `res/`
- [ ] No debug log statements left
- [ ] Documentation updated

---

## 🎓 Learn More

Each file contains:
1. **Comments in Hebrew** - Code documentation
2. **KDoc** - Function documentation
3. **Code examples** - Practical usage
4. **External links** - Reference materials

---

**Complete File Index | May 2026**

---