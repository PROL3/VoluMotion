package com.example.handgesturevolume.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
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

class HandGestureService : LifecycleService() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var handDetector: HandDetector
    private lateinit var rotationDetector: RotationDetector
    private lateinit var volumeController: VolumeController
    private lateinit var drivingModeDetector: DrivingModeDetector

    private var isServiceRunning = false
    private var isServicePaused = false
    private var isDetectorReady = false
    private var lastRotationTime = 0L
    private val rotationCooldown = 500L

    override fun onCreate() {
        super.onCreate()
        Log.d("HandGestureService", "Service created")

        cameraExecutor = Executors.newSingleThreadExecutor()
        rotationDetector = RotationDetector()
        volumeController = VolumeController(this)
        drivingModeDetector = DrivingModeDetector(this)

        // מאזין למצב נהיגה - שולט על הפעלה/כיבוי של המצלמה
        drivingModeDetector.addListener(object : DrivingModeDetector.DrivingModeListener {
            override fun onDrivingModeChanged(isDriving: Boolean) {
                if (isDriving && isServicePaused) {
                    resumeService()
                } else if (!isDriving && !isServicePaused) {
                    pauseService()
                }
                updateNotification(isDriving)
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                if (!isServiceRunning) {
                    startForeground(NOTIFICATION_ID, createNotification())
                    startHandDetection()
                }
            }
            ACTION_STOP -> {
                stopHandDetection()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PAUSE -> pauseService()
            ACTION_RESUME -> resumeService()
        }
        return START_STICKY
    }

    private fun pauseService() {
        if (!isServicePaused) {
            isServicePaused = true
            releaseCameraOnly()
            showToast("ניטור הושהה")
        }
    }

    private fun resumeService() {
        if (isServicePaused) {
            isServicePaused = false
            startHandDetection() // הפעלה מחדש של המצלמה והאנלייזר
            showToast("ניטור חזר לפעולה")
        }
    }

    private fun startHandDetection() {
        if (!PermissionManager.hasPermission(this, android.Manifest.permission.CAMERA)) {
            showToast("חסרה הרשאת מצלמה")
            return
        }

        isServiceRunning = true
        
        // אתחול ה-Detector רק אם הוא לא מוכן
        if (!isDetectorReady) {
            handDetector = HandDetector(this)
            isDetectorReady = true
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isServicePaused) {
                            processImage(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalyzer)
                Log.d("HandGestureService", "Camera bound to lifecycle")
            } catch (e: Exception) {
                Log.e("HandGestureService", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))

        if (PermissionManager.hasPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            drivingModeDetector.startMonitoring()
        }
    }

    /**
     * משחרר רק את המצלמה בלי לסגור את השירות כולו
     */
    private fun releaseCameraOnly() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            Log.d("HandGestureService", "Camera unbinded (Paused)")
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopHandDetection() {
        isServiceRunning = false
        drivingModeDetector.stopMonitoring()
        releaseCameraOnly()
        if (isDetectorReady) {
            handDetector.release()
            isDetectorReady = false
        }
        cameraExecutor.shutdown()
    }

    private fun processImage(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            if (bitmap != null) {
                val handLandmarks = handDetector.detectHand(bitmap, imageProxy.imageInfo.timestamp)

                if (handLandmarks != null && handLandmarks.confidence > 0.5f) {
                    val rotation = rotationDetector.detectRotation(handLandmarks.landmarks, handLandmarks.confidence)
                    val averageRotation = rotationDetector.getAverageRotation()

                    if (averageRotation != null && averageRotation.confidence > 0.6f) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastRotationTime > rotationCooldown) {
                            handleRotationAction(averageRotation.direction)
                            lastRotationTime = currentTime
                        }
                    }
                }
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e("HandGestureService", "Process error", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun handleRotationAction(direction: RotationDetector.RotationDirection) {
        when (direction) {
            RotationDetector.RotationDirection.CLOCKWISE -> {
                volumeController.volumeUp()
                showToast("🔊 עוצמה ↑")
                rotationDetector.reset()
            }
            RotationDetector.RotationDirection.COUNTER_CLOCKWISE -> {
                volumeController.volumeDown()
                showToast("🔉 עוצמה ↓")
                rotationDetector.reset()
            }
            else -> {}
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(CHANNEL_ID, "Hand Gesture", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("שירות ווליום במחוות יד")
            .setContentText("ממתין לזיהוי נהיגה...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(isDriving: Boolean) {
        val text = if (isDriving) "בנהיגה - זיהוי פעיל" else "בעצירה - ניטור מושהה"
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hand Gesture Volume")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
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