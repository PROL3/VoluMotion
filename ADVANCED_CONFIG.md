# ⚙️ הגדרות מתקדמות

## 🎯 Tuning Parameters

### HandDetector Configuration
```kotlin
// ב-HandDetector.kt

// זה צריך להיות 1 (רק יד אחת בכל פעם)
const val MAX_HANDS = 1

// גבוה יותר = עיבוד מהיר אך פחות מדויק
// נמוך יותר = מדויק אך איטי יותר
STATIC_IMAGE_MODE = false

// השתמש GPU לעיבוד מהיר
RUN_ON_GPU = true
```

### RotationDetector Tuning
```kotlin
// גודל חלון לחישוב ממוצע
val bufferSize = 10  // נסה 5-20

// סף confidence מינימלי
const val CONFIDENCE_THRESHOLD = 0.6f  // 0.5-0.8

// סף זוויה מינימלי (מעלות)
const val ANGLE_THRESHOLD = 2f  // 1-5 מעלות

// סף ביטחון גדול יותר = פחות false positives
const val AVERAGE_ROTATION_CONFIDENCE = 0.6f
```

### Volume Control Timing
```kotlin
// זמן בין פעולות אפשרויות סיבוב
const val ROTATION_COOLDOWN = 500L  // 300-1000ms

// מנוע מ-"flickering" של ווליום
// יותר זמן = פחות תגובה מהירה
```

### Driving Mode Detection
```kotlin
// סף מהירות ל-"נהיגה"
const val DRIVING_SPEED_THRESHOLD = 5f  // km/h

// frequency של GPS updates
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    1000,      // 1000ms בין updates
    10f,       // 10 meters displacement
    listener
)

// מהר יותר updates = צריכת סוללה גדולה יותר
```

## 📊 Performance Metrics

### Expected Performance

```
Device: Pixel 5 (Snapdragon 765)
Screen: 6" FHD+

Hand Detection:    ~30 FPS (33ms per frame)
Rotation Detection: ~60 FPS (16ms per frame)
Total Latency:      50-100ms
CPU Usage:          15-25%
Memory:             80-120 MB
Battery Drain:      8-10% per hour (with GPS)
```

### Optimization Checklist

- ✅ Use GPU rendering ב-MediaPipe
- ✅ Limit buffer sizes (10 frames)
- ✅ Close unused resources
- ✅ Use back-pressure strategy בـ CameraX
- ✅ Stop GPS when not driving
- ✅ Reduce update frequency

## 🔐 Security Best Practices

### Camera Usage
```kotlin
// אל תשמור את הפריימים לדיסק
// אל תשלח למשרת ללא הצפנה
// מחק בעבור כל frame שלא צריך
```

### Location Privacy
```kotlin
// אל תקשר את הלוקציה למצלמה
// אל תשלח מידע לוקציה ל-3rd parties
// מחק עבור 1 שעה
```

### Permissions
```kotlin
// בדוק permissions בכל פעם
if (!PermissionManager.hasPermission(context, CAMERA)) {
    // עצור
}
```

## 🚀 Custom Configurations

### Light Driving Mode (Low Power)
```kotlin
// - הפעל GPS בלבד
// - אל תתחיל hand detection
// - שמור מידי על סוללה
```

### Aggressive Mode (High Sensitivity)
```kotlin
// - bufferSize = 5 (מהיר יותר)
// - confidence = 0.4 (רגיש יותר)
// - cooldown = 300ms (מהיר יותר)
// ⚠️ יותר false positives
```

### Conservative Mode (High Stability)
```kotlin
// - bufferSize = 20 (יציב יותר)
// - confidence = 0.8 (בטוח יותר)
// - cooldown = 1000ms (איטי יותר)
// ✅ פחות false positives
```

## 📈 Monitoring & Logging

### Log Levels
```kotlin
Log.v() // Verbose - detailed debug info
Log.d() // Debug - used extensively here
Log.i() // Info - important events
Log.w() // Warning - possible problems
Log.e() // Error - problems
```

### Metrics Collection
```kotlin
// זה יכול להיות מעניין להוסיף
data class PerformanceMetrics(
    val fps: Float,
    val latency: Long,
    val cpuUsage: Float,
    val memoryUsage: Long,
    val batteryDrain: Float
)
```

## 🔄 Continuous Integration

### Suggested Testing
```gradle
testImplementation 'junit:junit:4.13.2'
testImplementation 'io.mockk:mockk:1.13.5'
androidTestImplementation 'androidx.test:runner:1.5.2'
```

### Test Cases
- [ ] Hand detection with different lighting
- [ ] Rotation detection accuracy
- [ ] Volume control integration
- [ ] GPS driving mode detection
- [ ] Foreground service lifecycle
- [ ] Permission handling
- [ ] Memory leaks detection

---

**מעדכן: May 2026**
