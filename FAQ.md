# ❓ שאלות נפוצות (FAQ)

## Installation & Setup

### Q: מה הדרישות מינימליות?
**A:** 
- Android 8.0 (API 26) minimum
- 2GB RAM (4GB recommended)
- Front-facing camera
- GPS (optional, for driving mode)

### Q: איך להתקין את הפרויקט?
**A:**
```bash
git clone <repo-url>
cd HandGestureVolumeControl
./gradlew installDebug
./gradlew runDebug
```

### Q: האם זה עובד עם emulator?
**A:** כן, אך emulator חייב לתמוך ב:
- Camera (virtual)
- GPU acceleration
- GPS simulation

### Q: איזו גרסת Android Studio?
**A:** Hedgehog (2023.1.1) ומעלה מומלץ

---

## Hand Detection Issues

### Q: לא מזהה את היד שלי
**A:** בדוק:
1. ✓ תאורה טובה (סביבו 300+ lux)
2. ✓ יד בתחום של 30-50cm מהמצלמה
3. ✓ יד בזווית טובה (כף היד פונה למצלמה)
4. ✓ בדוק ב-Logcat: `adb logcat | grep HandDetector`

### Q: מזהה יד אבל לא מזהה סיבוב
**A:** סיבות אפשריות:
- Confidence של hand detection נמוכה (< 0.5)
- סיבוב מהיר מדי או איטי מדי
- חלון buffer קטן מדי (bufferSize = 10)

**פתרון:**
```kotlin
// בַ RotationDetector.kt
private val bufferSize = 15  // תגדל ל-15-20
const val CONFIDENCE_THRESHOLD = 0.4f  // הנמך ל-0.4
```

### Q: יותר מדי false positives
**A:** הגדלת את הסף:
```kotlin
// בַ RotationDetector.kt
const val ANGLE_THRESHOLD = 5f  // תגדל מ-2 ל-5
const val AVERAGE_ROTATION_CONFIDENCE = 0.7f  // תגדל מ-0.6 ל-0.7
```

### Q: איך להסתכל על frame של היד?
**A:** הוסף debug code:
```kotlin
// ב-HandGestureService.kt, processImage()
android.graphics.Canvas.apply {
    bitmap?.let { 
        Log.d("DEBUG", "Frame size: ${it.width}x${it.height}")
        // save frame: FileOutputStream(...).write(...)
    }
}
```

---

## Volume Control Issues

### Q: הווליום משתנה כל הזמן (fluttering)
**A:** הגדלת את cooldown:
```kotlin
// ב-HandGestureService.kt
private val rotationCooldown = 1000L  // תגדל מ-500ms ל-1000ms
```

### Q: הווליום לא משתנה
**A:** בדוק:
1. ✓ MODIFY_AUDIO_SETTINGS permission
2. ✓ Ringer mode לא SILENT
3. ✓ Media stream לא MUTED
4. ✓ Log output: `adb logcat | grep VolumeController`

### Q: הווליום משתנה מדי לאט
**A:** הקטן את cooldown:
```kotlin
private val rotationCooldown = 300L  // הקטן מ-500ms ל-300ms
```

---

## Service & Background Issues

### Q: Service עוצר לאחר כמה דקות
**A:** בדוק:
1. ✓ FOREGROUND_SERVICE permission בـ manifest
2. ✓ startForeground() נקרא ב-onStartCommand()
3. ✓ Notification ID מייחד (לא 0)

### Q: Notification לא מופיעה
**A:** בדוק:
1. ✓ NotificationChannel נוצר
2. ✓ IMPORTANCE_LOW או IMPORTANCE_DEFAULT
3. ✓ android 8.0+ required for channels

### Q: App צורכת סוללה יותר מדי
**A:** בדוק:
1. ✓ GPS monitoring פועל כל הזמן
2. ✓ Hand detection paused כשלא בנהיגה
3. ✓ Reduce GPS update frequency

**פתרון GPS:**
```kotlin
// ב-DrivingModeDetector.kt
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    2000,  // תגדל מ-1000ms ל-2000ms
    50f,   // תגדל מ-10m ל-50m
    listener
)
```

---

## Permission Issues

### Q: App asks for permissions but don't work
**A:** בדוק runtime permissions:
```kotlin
// Android 6.0+ requires runtime permissions
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    PermissionManager.requestPermissions(activity)
}
```

### Q: Location permission denied
**A:** בדוק ב-Settings:
- Settings → Apps → Hand Gesture → Permissions
- Location must be "Allow only while using the app" or "Allow"

### Q: Camera permission denied
**A:** האפליקציה לא תעבוד ללא Camera permission

---

## Performance Issues

### Q: App frames drop (stuttering)
**A:** בדוק:
1. ✓ GPU enabled ב-MediaPipe
2. ✓ CameraX backpressure = KEEP_ONLY_LATEST
3. ✓ Single analysis thread

```kotlin
// ב-HandGestureService.kt
val imageAnalyzer = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
```

### Q: Memory leak (app crashes after 1 hour)
**A:** בדוק:
1. ✓ Bitmap.recycle() called
2. ✓ handDetector.release() called
3. ✓ No global references to Context

### Q: CPU usage too high
**A:** בדוק:
1. ✓ Hand detection confidence > 0.5
2. ✓ Image analysis not processing every frame
3. ✓ Reduce buffer sizes

```kotlin
private val bufferSize = 5  // הקטן מ-10 ל-5
```

---

## Driving Mode Detection

### Q: GPS not working
**A:** בדוק:
1. ✓ ACCESS_FINE_LOCATION permission
2. ✓ Location Services enabled
3. ✓ Device has GPS (or emulator simulating it)

### Q: Driving mode detected even when stationary
**A:** ייתכן interference מ-Network Location. בדוק:
```kotlin
// ב-DrivingModeDetector.kt
// Remove or increase threshold for NETWORK_PROVIDER
// Keep only GPS_PROVIDER
```

### Q: Driving mode never detected
**A:** אתה צריך:
1. ✓ Actual speed > 5 km/h
2. ✓ GPS or Network location active
3. ✓ Device moving

---

## Debugging & Logs

### Q: איך לראות detailed logs?
**A:**
```bash
# All logs from app
adb logcat | grep "HandGesture"

# Specific component
adb logcat | grep "HandDetector"
adb logcat | grep "RotationDetector"
adb logcat | grep "VolumeController"

# Save to file
adb logcat > logcat_dump.txt
```

### Q: איפה ה-Logcat ב-Android Studio?
**A:** 
View → Tool Windows → Logcat

### Q: איך להפסיק Logcat spam?
**A:** בתחתית Logcat, בחר Log Level = "Error" או "Warn"

### Q: איך להגדיר breakpoint?
**A:**
1. Click on left margin של שורה בקוד
2. Dot יופיע
3. Run → Debug "app"
4. Code will pause at breakpoint

---

## Integration Questions

### Q: איך להשתמש ב-OpenCV instead של MediaPipe?
**A:** זה דורש שינויים משמעותיים:
1. Replace HandDetector.kt
2. Use OpenCV Hand Tracking
3. More CPU intensive
4. **Not recommended** - MediaPipe טוב יותר

### Q: איך להוסיף gesture נוסף?
**A:**
```kotlin
// 1. Extend RotationDetector
class CustomGestureDetector : RotationDetector() {
    fun detectSwipe(prev: List<PointF>, curr: List<PointF>): SwipeDirection {
        // implement swipe detection
    }
}

// 2. Use in HandGestureService
val gesture = customDetector.detectSwipe(landmarks, confidence)
when (gesture) {
    SwipeDirection.LEFT -> { /* handle */ }
    SwipeDirection.RIGHT -> { /* handle */ }
}
```

### Q: איך להשתמש עם Smartwatch?
**A:** זה דורש:
1. Wear OS app
2. שידור מידע דרך DataLayer API
3. WearOS specific permissions

---

## Security & Privacy

### Q: האפליקציה שומרת frames?
**A:** לא! כל frame מחוזק בעבורו אחרי עיבוד

### Q: האפליקציה שלחה נתונים?
**A:** לא! כל הנתונים מעובדים locally

### Q: מה עם הלוקציה?
**A:** זה משמש רק לזיהוי נהיגה, לא שמור או שלח

---

## Build & Compilation

### Q: Gradle build fails with "Unsupported class-file format"
**A:**
```bash
# Make sure Java 11+ is set
# In Android Studio:
File → Project Structure → SDK Location → JDK location
# Set to Java 11 or higher
```

### Q: Cannot find MediaPipe library
**A:**
```gradle
// Make sure repository is added in build.gradle
repositories {
    google()
    mavenCentral()
}

// And dependency is correct
implementation 'com.google.mediapipe:solution-core:0.10.11'
```

### Q: App won't run on device
**A:** בדוק:
1. ✓ Device connected: `adb devices`
2. ✓ USB debugging enabled
3. ✓ Minimum SDK >= 26

---

## Feature Requests

### Q: איך להוסיף Vibration feedback?
**A:**
```kotlin
val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
vibrator.vibrate(200) // 200ms vibration
```

### Q: איך להוסיף custom keybindings?
**A:** זה ידרוש AccessibilityService ו-complex permissions

### Q: איך להוסיף remote control?
**A:** זה ידרוש Bluetooth integration ו-pairing logic

---

## General Support

### Q: במה הפרויקט בנוי?
**A:**
- Language: Kotlin
- Framework: Android (API 26-34)
- Libraries: MediaPipe, CameraX, AndroidX
- Build: Gradle 8.0+

### Q: איך לעדכן את הספריות?
**A:**
```gradle
// ב-build.gradle
implementation 'com.google.mediapipe:solution-core:0.10.11'
// שנה את הגרסה בסוף
```

### Q: איך להנמיך את APK size?
**A:**
```gradle
android {
    buildTypes {
        release {
            minifyEnabled true  // Enable ProGuard
            shrinkResources true
        }
    }
}
```

---

## Still Have Questions?

1. ✓ Check README.md
2. ✓ Check IMPLEMENTATION_GUIDE.md
3. ✓ Check Logcat for error messages
4. ✓ Search GitHub Issues
5. ✓ Create new issue with:
   - Device info
   - Android version
   - Logcat output
   - Steps to reproduce

---

**Last Updated: May 2026**
