# 🧪 דוגמות קוד וקטעים שימושיים

## 1. דוגמה מלאה לשימוש ב-HandDetector

```kotlin
// משתמש רוצה לזהות יד מbitmap
fun exampleHandDetection() {
    // יצור detector
    val detector = HandDetector(context)
    
    // קבל bitmap מהמצלמה
    val imageBitmap = getBitmapFromCamera()
    
    // זהה יד
    val hand = detector.detectHand(imageBitmap)
    
    if (hand != null) {
        Log.d("Example", """
            Hand detected!
            Handedness: ${hand.handedness}
            Confidence: ${hand.confidence}
            Landmarks: ${hand.landmarks.size}
        """.trimIndent())
        
        // השתמש ב-landmarks
        for ((index, landmark) in hand.landmarks.withIndex()) {
            Log.d("Landmark", "Point $index: (${landmark.x}, ${landmark.y})")
        }
    } else {
        Log.d("Example", "No hand detected")
    }
    
    // שחרר משאבים
    detector.release()
}
```

## 2. דוגמה מלאה לזיהוי סיבוב

```kotlin
fun exampleRotationDetection() {
    val rotationDetector = RotationDetector()
    val handDetector = HandDetector(context)
    
    // סדרה של frames
    for (i in 1..30) { // 30 frames
        val bitmap = getFrameFromCamera(i)
        val hand = handDetector.detectHand(bitmap)
        
        if (hand != null) {
            // זהה סיבוב
            val rotation = rotationDetector.detectRotation(
                hand.landmarks,
                hand.confidence
            )
            
            if (rotation != null) {
                Log.d("Rotation", """
                    Direction: ${rotation.direction}
                    Angle: ${rotation.angle}°
                    Confidence: ${rotation.confidence}
                """.trimIndent())
            }
            
            // קבל ממוצע יציב
            val stable = rotationDetector.getAverageRotation()
            if (stable?.direction == RotationDirection.CLOCKWISE) {
                Log.d("Action", "CLOCKWISE - Volume UP!")
            }
        }
    }
}
```

## 3. שימוש ב-VolumeController

```kotlin
fun exampleVolumeControl() {
    val controller = VolumeController(context)
    
    // עלייה בווליום
    for (i in 1..5) {
        controller.volumeUp()
        Thread.sleep(200) // מנוע spam
    }
    
    // הנמכה בווליום
    for (i in 1..5) {
        controller.volumeDown()
        Thread.sleep(200)
    }
    
    // בדוק מצב נוכחי
    val currentLevel = controller.getCurrentVolume()
    val maxLevel = controller.getMaxVolume()
    Log.d("Volume", "Current: $currentLevel / $maxLevel")
    
    // תגובה מותנה
    when {
        controller.isMuted() -> {
            Log.d("Volume", "System is muted")
            controller.toggleMute()
        }
        currentLevel < 0.3f -> {
            Log.d("Volume", "Volume is low, increasing")
            controller.volumeUp()
        }
    }
}
```

## 4. זיהוי נהיגה עם DrivingModeDetector

```kotlin
fun exampleDrivingDetection() {
    val drivingDetector = DrivingModeDetector(context)
    
    // הרשם לשינויים
    drivingDetector.addListener { isDriving ->
        Log.d("Driving", "Driving mode: $isDriving")
        
        if (isDriving) {
            // התחל זיהוי יד
            startHandDetection()
        } else {
            // עצור זיהוי יד (חיסכון סוללה)
            stopHandDetection()
        }
    }
    
    // התחל GPS ניטור
    drivingDetector.startMonitoring()
    
    // כאשר לא צריך יותר
    drivingDetector.stopMonitoring()
}
```

## 5. הרשאות מלאות

```kotlin
fun examplePermissionHandling() {
    val activity = this
    
    // בדוק הרשאות
    if (!PermissionManager.hasAllPermissions(context)) {
        Log.d("Permissions", "Missing permissions")
        
        // קבל רשימה של חסרונות
        val missing = PermissionManager.getMissingPermissions(context)
        Log.d("Permissions", "Missing: ${missing.toList()}")
        
        // בקש הרשאות
        PermissionManager.requestPermissions(activity)
    } else {
        Log.d("Permissions", "All permissions granted!")
    }
    
    // בדוק הרשאה בודדת
    if (PermissionManager.hasPermission(context, Manifest.permission.CAMERA)) {
        Log.d("Camera", "Camera permission OK")
    }
}
```

## 6. Foreground Service התחלה

```kotlin
fun exampleServiceStart() {
    val context = this
    
    // יצור intent
    val serviceIntent = Intent(context, HandGestureService::class.java).apply {
        action = HandGestureService.ACTION_START
    }
    
    // התחל foreground service
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
    } else {
        context.startService(serviceIntent)
    }
    
    Log.d("Service", "Service started")
}
```

## 7. Pause/Resume Service

```kotlin
fun exampleServicePauseResume() {
    val context = this
    
    // הפסק
    val pauseIntent = Intent(context, HandGestureService::class.java).apply {
        action = HandGestureService.ACTION_PAUSE
    }
    context.startService(pauseIntent)
    Log.d("Service", "Service paused")
    
    // חכה קצת
    Thread.sleep(5000)
    
    // חזור
    val resumeIntent = Intent(context, HandGestureService::class.java).apply {
        action = HandGestureService.ACTION_RESUME
    }
    context.startService(resumeIntent)
    Log.d("Service", "Service resumed")
}
```

## 8. Bitmap from Camera Frame

```kotlin
fun exampleImageConversion(imageProxy: ImageProxy): Bitmap? {
    return try {
        val image = imageProxy.image ?: return null
        
        // קבל מידע של תמונה
        val width = image.width
        val height = image.height
        val format = image.format
        
        Log.d("Image", "$width x $height, format=$format")
        
        // המרה מ-NV21 ל-Bitmap (like done in HandGestureService)
        val ySize = image.planes[0].buffer.remaining()
        val nv21 = ByteArray(ySize + (ySize / 2))
        
        image.planes[0].buffer.get(nv21, 0, ySize)
        image.planes[2].buffer.get(nv21, ySize, ySize / 4)
        image.planes[1].buffer.get(nv21, ySize + ySize / 4, ySize / 4)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        
        BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    } catch (e: Exception) {
        Log.e("ImageConversion", "Error: ${e.message}")
        null
    }
}
```

## 9. Custom Gesture Detection Extension

```kotlin
// הרחבה: זיהוי ג'סטורים נוספים בעתיד
class CustomGestureDetector {
    // זיהוי swipe
    fun detectSwipe(previous: List<PointF>, current: List<PointF>): SwipeDirection? {
        // השווה מיקום הכף בين frames
        val prevPalm = previous[9]
        val currPalm = current[9]
        
        val dx = currPalm.x - prevPalm.x
        val dy = currPalm.y - prevPalm.y
        
        return when {
            dx > 0.1f -> SwipeDirection.RIGHT
            dx < -0.1f -> SwipeDirection.LEFT
            dy > 0.1f -> SwipeDirection.DOWN
            dy < -0.1f -> SwipeDirection.UP
            else -> null
        }
    }
    
    enum class SwipeDirection { LEFT, RIGHT, UP, DOWN }
}
```

## 10. Performance Monitoring

```kotlin
fun examplePerformanceMonitoring() {
    val startTime = System.currentTimeMillis()
    val startMemory = Runtime.getRuntime().totalMemory()
    
    // עשה כמה עיבודים
    val detector = HandDetector(context)
    val bitmap = getTestBitmap()
    val hand = detector.detectHand(bitmap)
    
    val endTime = System.currentTimeMillis()
    val endMemory = Runtime.getRuntime().totalMemory()
    
    val duration = endTime - startTime
    val memoryUsed = (endMemory - startMemory) / 1024 / 1024 // MB
    
    Log.d("Performance", """
        Duration: ${duration}ms
        Memory: ${memoryUsed}MB
        FPS: ${1000 / duration}
    """.trimIndent())
}
```

---

## 📝 שימוש בקטעים

כל קטע יכול להיות מעתיק ישירות לפרויקט שלך. פשוט:
1. Copy את הקוד
2. Paste לתוך Activity/Service
3. התאם את context/parameter
4. בדוק שנ אין שגיאות

---

**עדכון: May 2026**
