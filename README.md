# 🎙️ Hand Gesture Volume Control

## 📋 תיאור הפרויקט

אפליקציה Android שמזהה תנועת סיבוב של היד (clockwise/counter-clockwise) דרך מצלמת הטלפון ומשתמשת בכך כדי לשלוט בעוצמת השמע של המערכת - כל זה ללא מגע וגם בזמן נהיגה.

## ✨ תכונות עיקריות

✅ **זיהוי סיבוב יד בזמן אמת**
- שימוש ב-MediaPipe לזיהוי יד מדויק
- זהוי סיבוב clockwise/counter-clockwise
- סינון noise ו-false positives

✅ **שליטה בווליום**
- עלייה/ירידה בעוצמת השמע
- תגובה מהירה ויציבה
- פעולה מהירה (cooldown של 500ms למנוע triggers מופרזות)

✅ **עבודה ברקע**
- Foreground Service עם Notification
- עדכון הודעה דינמי
- אפשרות Pause/Resume

✅ **זיהוי נהיגה**
- ניטור GPS בזמן אמת
- הפעלה אוטומטית כאשר מהירות > 5 קמ"ש
- השהיית ניטור כאשר לא בנסיעה

✅ **ממשק משתמש**
- UI פשוטה וברורה
- מידע מלא על הרשאות
- טוגל הפעלה/כיבוי
- כפתור Pause/Resume

## 🏗️ ארכיטקטורה

```
┌─────────────────────────────────────────┐
│         MainActivity (UI)               │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────┐   │
│  │   HandGestureService            │   │
│  │   (Foreground Service)          │   │
│  ├─────────────────────────────────┤   │
│  │ • CameraX (Image Analysis)      │   │
│  │ • HandDetector (MediaPipe)      │   │
│  │ • RotationDetector              │   │
│  │ • VolumeController              │   │
│  │ • DrivingModeDetector (GPS)     │   │
│  └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

## 📦 קובצי הפרויקט

### Core Components
- **HandDetector.kt** - זיהוי יד עם MediaPipe
- **RotationDetector.kt** - זיהוי סיבוב עם חישובי זוויות
- **VolumeController.kt** - שליטה בווליום של Android
- **DrivingModeDetector.kt** - זיהוי נהיגה דרך GPS

### UI & Service
- **MainActivity.kt** - ממשק משתמש ראשי
- **HandGestureService.kt** - Foreground Service לעבודה ברקע
- **activity_main.xml** - Layout של ה-Activity

### Configuration
- **AndroidManifest.xml** - הצהרות והרשאות
- **build.gradle** - תלויות והגדרות בנייה
- **strings.xml** - מחרוזות טקסט

## 🔧 דרישות טכניות

### Gradle & Build
```gradle
compileSdk 34
minSdk 26
targetSdk 34
```

### Libraries
```gradle
// CameraX - for camera access
androidx.camera:camera-core
androidx.camera:camera-lifecycle

// MediaPipe - for hand detection
com.google.mediapipe:solution-core
com.google.mediapipe:tasks-vision

// Android & Lifecycle
androidx.lifecycle:lifecycle-service
androidx.appcompat:appcompat
```

## 📱 הרשאות נדרשות

| הרשאה | מטרה |
|------|------|
| `CAMERA` | גישה למצלמה הקדמית |
| `ACCESS_FINE_LOCATION` | זיהוי נהיגה דרך GPS |
| `ACCESS_COARSE_LOCATION` | fallback ל-network location |
| `FOREGROUND_SERVICE` | עבודה ברקע עם Notification |
| `ACCESS_NOTIFICATION_POLICY` | שליטה בווליום |

## 🚀 איך להשתמש

### 1. התקן את הפרויקט
```bash
git clone <repo-url>
cd HandGestureVolumeControl
```

### 2. בנה וקום את האפליקציה
```bash
./gradlew build
./gradlew installDebug
```

### 3. אשר הרשאות בטלפון
- 📷 Camera
- 📍 Location
- 🔔 Notification

### 4. הפעל את האפליקציה
- לחץ על Switch כדי להפעיל
- התחל לסובב את יד לפני המצלמה
- סיבוב ימינה (CW) = עוצמה ↑
- סיבוב שמאלה (CCW) = עוצמה ↓

## 🔍 איך פועל זיהוי הסיבוב

### שלב 1: זיהוי יד
```
Image from Camera → MediaPipe → 21 Landmarks
```

### שלב 2: חישוב זוויות
```
Landmarks → Calculate angle of each finger around palm center
```

### שלב 3: זיהוי סיבוב
```
Previous Angles - Current Angles = Rotation Direction & Speed
```

### שלב 4: סינון
```
Apply motion buffer (10 frames)
Check confidence > 0.6
Apply cooldown (500ms)
```

## ⚙️ הגדרות וטיונים

### זיהוי Rotation
```kotlin
// ב-RotationDetector.kt
private val bufferSize = 10 // גודל חלון לחישוב ממוצע
private const val threshold = 2f // מינימום מעלות לזיהוי סיבוב
```

### Cooldown בין פעולות
```kotlin
// ב-HandGestureService.kt
private val rotationCooldown = 500L // 500ms בין פעולות
```

### GPS Monitoring
```kotlin
// ב-DrivingModeDetector.kt
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    1000, // 1 שנייה
    10f, // 10 מטרים
    locationListener
)
```

## 🎯 אופטימיזציה לביצועים

### צריכת CPU
- ✅ ImageAnalysis יחיד (לא יותר)
- ✅ GPU processing ב-MediaPipe
- ✅ Cooldown למנוע continuous processing

### צריכת סוללה
- ✅ Foreground Service עם LOW importance
- ✅ GPS location updates כל 1-2 שניות
- ✅ CameraX backpressure strategy = KEEP_ONLY_LATEST

### זיכרון
- ✅ Bitmap recycling
- ✅ Limited hand landmarks buffer
- ✅ Single CameraX analyzer thread

## 🐛 Troubleshooting

### בעיה: לא זיהוי יד
**פתרון:**
- בדוק תאורה (צריך אור טוב)
- הצב את הטלפון במרחק 30-50 ס"מ
- בדוק שהמצלמה זה קדמית (FRONT_CAMERA)

### בעיה: false positives רבים
**פתרון:**
- הגבר את `bufferSize` ל-15-20
- הגבר את `confidence` threshold
- הגדל את `rotationCooldown` ל-800-1000ms

### בעיה: Service לא עובד ברקע
**פתרון:**
- בדוק הרשאות (בעיקר CAMERA ו-FOREGROUND_SERVICE)
- בדוק שה-device SDK >= 26
- בדוק logs ב-Logcat

### בעיה: Battery drain גבוה
**פתרון:**
- כבה את GPS כאשר לא בנהיגה
- הגדל את `minTime` ב-GPS requestLocationUpdates
- השתמש בNETWORK_PROVIDER במקום GPS

## 📊 Logging

כל ה-components משתמשים בـ Log.d:
```kotlin
Log.d("HandDetector", "Hand detected with confidence: $confidence")
Log.d("RotationDetector", "Rotation detected: $direction")
Log.d("VolumeController", "Volume changed")
Log.d("DrivingMode", "Driving state: $isDriving")
```

**לראות logs:**
```bash
adb logcat | grep -E "HandDetector|RotationDetector|VolumeController|DrivingMode"
```

## 📈 אפשרויות הרחבה עתידיות

- [ ] זיהוי ג'סצ'ר יד נוסף (swipe, pinch)
- [ ] הוספת vibration feedback
- [ ] תרגום עברית של טקסטים
- [ ] תמיכה ביד שמאל וימין באותו זמן
- [ ] הוספת ML-based gesture recognition
- [ ] אפשרות custom keybindings
- [ ] ניתוח ביצועים בזמן אמת

## 📄 License

This project is based on the Hand Tracking Using OpenCV repository and includes MediaPipe integration.

## 📝 הערות חשובות

1. **כל ההקוד בעברית** - קוד, קוראים, לוגים
2. **No OpenCV** - משתמש ב-MediaPipe לביצועים טובים יותר
3. **Android 8.0+** - חובה עבור Foreground Services
4. **Front Camera** - משתמש רק במצלמה הקדמית

---

**נוצר עם ❤️ לשליטה ידנית בווליום בזמן נהיגה**
