# 📚 מדריך יישום מפורט

## 1️⃣ התקנה ראשונית

### קבלת הפרויקט
```bash
# Clone או download הפרויקט
git clone <repository-url>
cd HandGestureVolumeControl

# פתח ב-Android Studio
# File → Open → בחר את התיקייה
```

### בדוקות סביבת הפיתוח
```
✓ Android Studio Hedgehog+
✓ SDK 34 installed
✓ NDK (עבור CameraX ו-MediaPipe)
✓ Kotlin Plugin עדכני
✓ emulator או device עם Android 8.0+
```

## 2️⃣ תלויות חיוביות (Dependencies)

### MediaPipe
MediaPipe היא ספריה בתוך Google עם מודלים מיטביים לזיהוי יד.

**זה מה שעובד:**
```gradle
implementation 'com.google.mediapipe:solution-core:0.10.11'
implementation 'com.google.mediapipe:tasks-vision:0.10.11'
```

**מה היא עושה:**
- זוהה 21 נקודות בכף היד (landmarks)
- מתוך כל frame של המצלמה
- עם confidence score

### CameraX
חלק מ-Android Jetpack - עם CameraX אפשר שימוש קל למצלמה ברגע אמת.

```gradle
implementation 'androidx.camera:camera-core:1.2.3'
implementation 'androidx.camera:camera-lifecycle:1.2.3'
```

### Lifecycle Service
לעבודה ברקע עם Foreground Service.

```gradle
implementation 'androidx.lifecycle:lifecycle-service:2.6.1'
```

## 3️⃣ זיהוי היד - HandDetector

### איך זה עובד

```kotlin
// 1. אתחול (ב-onCreate של Service)
val handDetector = HandDetector(context)

// 2. עיבוד כל תמונה
val handLandmarks = handDetector.detectHand(bitmap)

if (handLandmarks != null && handLandmarks.confidence > 0.5f) {
    // יש יד בתמונה!
    println("Found hand with ${handLandmarks.landmarks.size} points")
}

// 3. שחרור (ב-onDestroy)
handDetector.release()
```

### Output של HandDetector

```
HandLandmarks(
    handedness = "Right",  // או "Left"
    landmarks = List<PointF>(21 items),  // 21 נקודות בכף יד
    confidence = 0.92f
)
```

### ה-21 Landmarks של יד

```
WRIST (0)
├─ THUMB: TIP (4)
├─ INDEX: TIP (8)
├─ MIDDLE: TIP (12)
├─ RING: TIP (16)
└─ PINKY: TIP (20)

+ 16 נקודות אמצע של כל אצבע
```

## 4️⃣ זיהוי סיבוב - RotationDetector

### מכניקה

**הרעיון הבסיסי:**
1. חשב זווית של כל אצבע סביב מרכז הכף
2. השווה זוויות בין frames
3. אם הוא סיבוב באותו כיוון → clockwise/counter-clockwise

### קוד מדוגמא

```kotlin
// יצור RotationDetector
val rotationDetector = RotationDetector()

// בכל frame, עדכן הסיבוב
val rotation = rotationDetector.detectRotation(landmarks, confidence)

// קבל ממוצע מהפריימים האחרונים
val stableRotation = rotationDetector.getAverageRotation()

if (stableRotation?.direction == RotationDirection.CLOCKWISE) {
    volumeController.volumeUp()
}
```

### אלגוריתם חישוב זוויות

```
For each landmark point:
    1. חשב (dx, dy) = point - palmCenter
    2. angle = atan2(dy, dx) * 180 / π
    3. normalize to 0-360 degrees

Compare angles:
    angleDiff = currentAngle - previousAngle
    if angleDiff > 2°: clockwise rotation
    if angleDiff < -2°: counter-clockwise rotation
```

## 5️⃣ שליטה בווליום - VolumeController

### שימוש בסיסי

```kotlin
val volumeController = VolumeController(context)

// הגדלה
volumeController.volumeUp()

// הנמכה
volumeController.volumeDown()

// השתקה
volumeController.toggleMute()

// קבל מידע
val currentLevel = volumeController.getCurrentVolume() // 0-1
val maxVolume = volumeController.getMaxVolume()
```

### מימוש פנימי

```kotlin
audioManager.adjustStreamVolume(
    AudioManager.STREAM_MUSIC,
    AudioManager.ADJUST_RAISE,  // או ADJUST_LOWER
    AudioManager.FLAG_SHOW_UI   // הצג ה-Volume Slider
)
```

## 6️⃣ זיהוי נהיגה - DrivingModeDetector

### איך זה עובד

GPS מודד מהירות. אם מהירות > 5 קמ"ש = נוהג.

```kotlin
val drivingDetector = DrivingModeDetector(context)

// הרשם למעקב
drivingDetector.addListener { isDriving ->
    if (isDriving) {
        // התחל זיהוי סיבוב
    } else {
        // הפסק זיהוי סיבוב (חיסכון סוללה)
    }
}

// התחל GPS monitoring
drivingDetector.startMonitoring()

// כאשר סיימנו
drivingDetector.stopMonitoring()
```

## 7️⃣ Foreground Service - HandGestureService

### מאפייניו

- רץ בעבודת רקע, גם כאשר ה-App סגור
- מציג Notification כל הזמן
- אפשר לעצור ממנו או מה-App

### Lifecycle

```
1. onCreate() - אתחל componnts
2. onStartCommand() - התחל זיהוי
3. startForeground() - הצג notification
4. processImage() - כל frame
5. onDestroy() - שחרור וסגירה
```

### טיפול ב-Permissions

```kotlin
// בדוק הרשאות
if (!PermissionManager.hasAllPermissions(this)) {
    PermissionManager.requestPermissions(activity)
}

// אם לא יש CAMERA permission
handDetector.release() // עצור עיבוד
```

## 8️⃣ MainActivity - ממשק המשתמש

### כפתורים ופעולות

| Element | פעולה |
|---------|-------|
| Toggle Switch | הפעל/כבה שירות |
| Pause Button | השהה/חזור ניטור |
| Status Text | הצג סטטוס נוכחי |

### Flow

```
Activity Created
    ↓
Check & Request Permissions
    ↓
User toggles Switch
    ↓
startForegroundService(HandGestureService)
    ↓
Service: startHandDetection()
    ↓
Service: procesImage() בלולאה
    ↓
User presses Pause
    ↓
isServicePaused = true
```

## 9️⃣ Notification & UI Updates

### Foreground Notification

```kotlin
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle("Hand Gesture Volume Control")
    .setContentText("זיהוי סיבוב יד פעיל")
    .setSmallIcon(...)
    .setOngoing(true)  // Persistent
    .build()

startForeground(NOTIFICATION_ID, notification)
```

### Toast Messages

```kotlin
// מוצג קצר לכל פעולה
Toast.makeText(this, "🔊 עוצמה ↑", Toast.LENGTH_SHORT).show()
```

## 🔟 בדיקה וניפוי שגיאות

### Logcat

```bash
# הצג רק Logs של האפליקציה
adb logcat | grep "HandDetector\|RotationDetector\|VolumeController"

# מלא לוגים
adb logcat
```

### Common Issues

| בעיה | סיבה | פתרון |
|-----|------|------|
| אין זיהוי יד | תאורה גרועה | תוסף אור |
| False positives | סף confidence נמוך | הגדל confidence |
| Service עוצר | Permissions | בדוק manifest + runtime |
| Battery drain | GPS תמידי | השתמש network provider |

### Test Points

```kotlin
// 1. זיהוי יד
fun testHandDetection() {
    val hand = handDetector.detectHand(testBitmap)
    assert(hand != null && hand.confidence > 0.5f)
}

// 2. חישוב סיבוב
fun testRotation() {
    val rotation = rotationDetector.detectRotation(landmarks, 0.9f)
    assert(rotation.direction != RotationDirection.NONE)
}

// 3. שליטה בווליום
fun testVolume() {
    val before = volumeController.getCurrentVolume()
    volumeController.volumeUp()
    val after = volumeController.getCurrentVolume()
    assert(after > before)
}
```

## 🔜 צעדים הבאים

1. **בנה את הפרויקט**
   ```bash
   ./gradlew build
   ```

2. **הרץ על device/emulator**
   ```bash
   ./gradlew installDebug
   adb shell am start -n com.example.handgesturevolume/.ui.MainActivity
   ```

3. **אשר הרשאות** בטלפון

4. **בדוק פעולה**
   - סובב את יד קדימה במצלמה
   - בדוק שהווליום משתנה

5. **טון תכונות**
   - הגדל/הקטן confidence threshold
   - שנה cooldown בין פעולות
   - בדוק driving mode detection

---

## 🎓 הסברים מתקדמים

### איך MediaPipe מתאמן

MediaPipe זה מודל Neural Network שאומן על מיליונים של תמונות יד. הוא זוהה 21 נקודות בדיוק של ~95%.

### איך עובד זיהוי סיבוב

זה לא magic! זה just טריגונומטריה:
```
angle = atan2(dy, dx)
```
אם הזוויה משתנה בעקביות, זה סיבוב!

### מדוע חזנו CameraX?

- ✅ Simple API
- ✅ Lifecycle-aware
- ✅ טוב עם Kotlin Coroutines
- ✅ Built-in back-pressure handling

---

**עדכון אחרון: מאי 2026**
