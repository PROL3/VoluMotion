package com.example.handgesturevolume.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.handgesturevolume.core.HandDetector
import com.example.handgesturevolume.core.RotationDetector
import com.example.handgesturevolume.ui.MainActivity
import com.example.handgesturevolume.utils.DrivingModeDetector
import com.example.handgesturevolume.utils.PermissionManager
import com.example.handgesturevolume.utils.VolumeController
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * שירות ל-Foreground שמטפל בזיהוי יד וסיבוב בעבודת רקע
 * פועל כל הזמן ומעדכן הודעה ברצועת ההודעות
 */
class HandGestureService : LifecycleService() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var handDetector: HandDetector
    private lateinit var rotationDetector: RotationDetector
    private lateinit var volumeController: VolumeController
    private lateinit var drivingModeDetector: DrivingModeDetector

    private var isServiceRunning = false
    private var isServicePaused = false
    private var lastRotationTime = 0L
    private val rotationCooldown = 500L // 500ms בין פעולות סיבוב

    private var previousHandLandmarks: HandDetector.HandLandmarks? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("HandGestureService", "Service created")

        // אתחול רכיבים
        cameraExecutor = Executors.newSingleThreadExecutor()
        handDetector = HandDetector(this)
        rotationDetector = RotationDetector()
        volumeController = VolumeController(this)
        drivingModeDetector = DrivingModeDetector(this)

        // רישום למעקב מצב נהיגה
        drivingModeDetector.addListener(object : DrivingModeDetector.DrivingModeListener {
            override fun onDrivingModeChanged(isDriving: Boolean) {
                isServicePaused = !isDriving
                updateNotification(isDriving)
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                Log.d("HandGestureService", "Starting hand gesture detection")
                startForeground(NOTIFICATION_ID, createNotification())
                startHandDetection()
            }
            ACTION_STOP -> {
                Log.d("HandGestureService", "Stopping hand gesture detection")
                stopHandDetection()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PAUSE -> {
                isServicePaused = true
                showToast("ניטור הושהה")
            }
            ACTION_RESUME -> {
                isServicePaused = false
                showToast("ניטור המשך")
            }
        }

        return START_STICKY
    }

    /**
     * התחל זיהוי יד מהמצלמה
     */
    private fun startHandDetection() {
        if (!PermissionManager.hasPermission(this, android.Manifest.permission.CAMERA)) {
            Log.e("HandGestureService", "Camera permission not granted")
            showToast("חסרה הרשאת מצלמה")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        isServiceRunning = true

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (!isServicePaused) {
                                processImage(imageProxy)
                            }
                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        imageAnalyzer
                    )
                    Log.d("HandGestureService", "Camera started")
                } catch (e: Exception) {
                    Log.e("HandGestureService", "Error binding camera: ${e.message}")
                    showToast("שגיאה באתחול מצלמה")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            },
            ContextCompat.getMainExecutor(this)
        )

        // התחל ניטור GPS
        if (PermissionManager.hasPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            drivingModeDetector.startMonitoring()
        }
    }

    /**
     * עצור זיהוי יד
     */
    private fun stopHandDetection() {
        isServiceRunning = false
        drivingModeDetector.stopMonitoring()
        handDetector.release()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
        Log.d("HandGestureService", "Hand detection stopped")
    }

    /**
     * עיבוד כל תמונה מהמצלמה
     */
    private fun processImage(imageProxy: ImageProxy) {
        try {
            val frameTimestampNs = imageProxy.imageInfo.timestamp
            val bitmap = imageProxyToBitmap(imageProxy)
            if (bitmap != null) {
                val handLandmarks = handDetector.detectHand(bitmap, frameTimestampNs)

                if (handLandmarks != null && handLandmarks.confidence > 0.5f) {
                    // זיהוי יד עם ביטחון גבוה

                    // בדוק סיבוב
                    val rotation = rotationDetector.detectRotation(
                        handLandmarks.landmarks,
                        handLandmarks.confidence
                    )

                    // קבל ממוצע סיבוב מסדרת מדידות
                    val averageRotation = rotationDetector.getAverageRotation()

                    if (averageRotation != null && averageRotation.confidence > 0.6f) {
                        val currentTime = System.currentTimeMillis()

                        // הגן מפני active triggers קצובים
                        if (currentTime - lastRotationTime > rotationCooldown) {
                            when (averageRotation.direction) {
                                RotationDetector.RotationDirection.CLOCKWISE -> {
                                    Log.d("HandGestureService", "Clockwise rotation detected")
                                    volumeController.volumeUp()
                                    showToast("🔊 עוצמה ↑")
                                    rotationDetector.reset()
                                }
                                RotationDetector.RotationDirection.COUNTER_CLOCKWISE -> {
                                    Log.d("HandGestureService", "Counter-clockwise rotation detected")
                                    volumeController.volumeDown()
                                    showToast("🔉 עוצמה ↓")
                                    rotationDetector.reset()
                                }
                                else -> {}
                            }

                            lastRotationTime = currentTime
                        }
                    }
                }

                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e("HandGestureService", "Error processing image: ${e.message}")
        }
    }

    /**
     * המרת ImageProxy ל-Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val image = imageProxy.image ?: return null

            val ySize = image.planes[0].buffer.remaining()
            val uSize = image.planes[1].buffer.remaining()
            val vSize = image.planes[2].buffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            image.planes[0].buffer.get(nv21, 0, ySize)
            image.planes[2].buffer.get(nv21, ySize, vSize)
            image.planes[1].buffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(
                nv21,
                ImageFormat.NV21,
                image.width,
                image.height,
                null
            )

            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
            val imageBytes = out.toByteArray()

            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("HandGestureService", "Error converting image: ${e.message}")
            null
        }
    }

    /**
     * יצור הודעה ברצועת ההודעות
     */
    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Hand Gesture Detection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitoring hand gestures for volume control"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hand Gesture Volume Control")
            .setContentText("זיהוי סיבוב יד פעיל")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * עדכן הודעה
     */
    private fun updateNotification(isDriving: Boolean) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val text = if (isDriving) "נוהג - זיהוי פעיל" else "לא נוהג - זיהוי מושהה"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hand Gesture Volume Control")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * הצג Toast קטן
     */
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHandDetection()
        Log.d("HandGestureService", "Service destroyed")
    }

    companion object {
        const val CHANNEL_ID = "hand_gesture_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.handgesturevolume.START"
        const val ACTION_STOP = "com.example.handgesturevolume.STOP"
        const val ACTION_PAUSE = "com.example.handgesturevolume.PAUSE"
        const val ACTION_RESUME = "com.example.handgesturevolume.RESUME"
    }
}
