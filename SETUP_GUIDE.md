# 🔧 Full Installation Guide: Step-by-Step

## Step 1: Download and Setup

### 1.1 Clone the Project
Open your terminal and run the following commands:
```bash
cd ~/Desktop
git clone https://github.com/PROL3/VoluMotion.git
cd VoluMotion
```

### 1.2 Android Studio Configuration
1.  Open **Android Studio**.
2.  Go to **File** → **Open**.
3.  Select the `VoluMotion` folder.

---

## Step 2: Build & Sync (Manual Workflow)

**Note:** Standard IDE "Sync" or "Run" buttons may fail in this environment. You must use the terminal for the initial build and deployment.

### 2.1 Build the Project
Run the Gradle wrapper from the project root:
```powershell
.\gradlew.bat :app:assembleDebug
```

---

## Step 3: Verify Dependencies

To ensure all libraries are correctly resolved, check the dependency tree:
```bash
./gradlew dependencies
```

**Required components should include:**
*   ✓ **MediaPipe:** Hand detection and tracking.
*   ✓ **CameraX:** Camera access and preview.
*   ✓ **AndroidX:** Lifecycle management and background services.
*   ✓ **Kotlin:** Core programming language.

---

## Step 4: Device/Emulator Setup

### Using a Physical Device
1.  Connect your device via USB.
2.  Enable **USB Debugging** in Developer Options.
3.  Verify connection:
```bash
adb devices
```

### Using an Emulator
1.  Launch an emulator with **Android 8.0+ (API 26+)**.
2.  Ensure you are using a **Google Play** system image.

---

## Step 5: Install to Device

Install the generated APK to your target device (Serial: `R5CW612ZHAX`):

```powershell
& "C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s R5CW612ZHAX install -r "app\build\outputs\apk\debug\app-debug.apk"
```

---

## Step 6: Launch the Application

Use the following commands to force-stop any existing instance and launch the `MainActivity`:

```powershell
& "C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s R5CW612ZHAX shell am force-stop com.example.handgesturevolume; 
& "C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s R5CW612ZHAX shell am start -n com.example.handgesturevolume/.ui.MainActivity
```

---

## Step 7: Grant Permissions

On the target device, ensure the following permissions are granted:
1.  Navigate to **Settings** → **Apps** → **Hand Gesture Volume Control**.
2.  Enable **Permissions**:
    *   ✓ **Camera:** Required for gesture tracking.
    *   ✓ **Location:** Required for driving mode context.
    *   ✓ **Notifications:** Required for the background service listener.

---

## Step 8: Operation & Testing

1.  Open the application.
2.  Toggle the main switch to **ON**.
3.  Position your hand in front of the camera.
4.  **Rotate hand clockwise:** Volume should increase (**UP**).
5.  **Rotate hand counter-clockwise:** Volume should decrease (**DOWN**).

---

## Step 9: Debugging with Logcat

Monitor real-time logs to verify detection and service status:
```bash
adb logcat | grep "HandGestureService\|HandDetector\|Rotation"
```
*Alternatively, in Android Studio: **View** → **Tool Windows** → **Logcat**.*

---

## ✅ Final Checklist

- [ ] Project builds without errors.
- [ ] All dependencies are successfully downloaded.
- [ ] Application installs on the device.
- [ ] All system permissions are granted.
- [ ] Hand detection is active.
- [ ] Rotation logic triggers volume changes.
- [ ] Foreground notification is visible.
- [ ] Logs are appearing correctly in Logcat.

---

## 🆘 Troubleshooting

### Build Failures
*   **Error:** `Unsupported class-file format`
*   **Solution:** Update your Java version to **JDK 17** or higher.

### MediaPipe Loading Issues
*   **Error:** `Library libmediapipe not found`
*   **Solution:** Ensure you have an active internet connection to download the AAR, or manually verify the MediaPipe version in `build.gradle`.

### App Crashes on Startup
*   **Error:** `SecurityException - Camera permission`
*   **Solution:** Manually enable Camera permissions in the device settings as described in Step 7.

### Detection Issues
*   **Issue:** Hand not detected or low confidence.
*   **Solution:** 
    *   Improve lighting conditions.
    *   Ensure the hand is within 0.5m – 1.5m of the camera.
    *   Clean the front camera lens.

---

**Setup Complete! 🎉 Your VoluMotion environment is now ready.**