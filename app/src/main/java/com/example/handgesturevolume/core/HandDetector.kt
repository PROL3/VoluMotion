package com.example.handgesturevolume.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult

/**
 * מנהל זיהוי היד באמצעות MediaPipe
 * זוהה נקודות חשובות בכף היד וחיבורים ביניהן
 */
class HandDetector(context: Context) {
    private val hands: Hands
    @Volatile
    private var latestResult: HandsResult? = null
    private var lastSentTimestampUs: Long = 0L

    init {
        val options = HandsOptions.builder()
            .setStaticImageMode(false)
            .setMaxNumHands(1)
            // Foreground service flow in this app does not maintain a GL context.
            // CPU mode is more stable here and avoids runtime crashes.
            .setRunOnGpu(false)
            .build()

        hands = Hands(context, options)
        hands.setErrorListener { message, e ->
            Log.e("HandDetector", "MediaPipe error: $message", e)
        }
        hands.setResultListener { result ->
            latestResult = result
        }
    }

    data class HandLandmarks(
        val handedness: String, // "Right" או "Left"
        val landmarks: List<PointF>, // 21 נקודות בכף היד
        val confidence: Float, // דרגת ביטחון בזיהוי
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * מזהה יד בתמונה
     */
    fun detectHand(bitmap: Bitmap, frameTimestampNs: Long): HandLandmarks? {
        return try {
            val requestedTimestampUs = frameTimestampNs / 1_000L
            // MediaPipe requires strictly increasing timestamps.
            val safeTimestampUs = if (requestedTimestampUs > lastSentTimestampUs) {
                requestedTimestampUs
            } else {
                lastSentTimestampUs + 1L
            }
            lastSentTimestampUs = safeTimestampUs
            hands.send(bitmap, safeTimestampUs)
            val result = latestResult ?: return null
            if (result.multiHandLandmarks().isEmpty()) return null
            if (result.multiHandedness().isEmpty()) return null

            val landmarkProtoList = result.multiHandLandmarks()[0].landmarkList
            if (landmarkProtoList.size < 21) return null

            val landmarks = landmarkProtoList.map { lm -> PointF(lm.x, lm.y) }
            val handedness = result.multiHandedness()[0].label
            val confidence = result.multiHandedness()[0].score

            HandLandmarks(
                handedness = handedness,
                landmarks = landmarks,
                confidence = confidence
            )
        } catch (e: Exception) {
            Log.e("HandDetector", "Error detecting hand: ${e.message}", e)
            null
        }
    }

    /**
     * מקבל מערך של HandLandmarks ו-timestamps
     * מחזיר את מהירות התנועה הממוצעת
     */
    fun calculateMotionVelocity(
        previousLandmarks: HandLandmarks?,
        currentLandmarks: HandLandmarks
    ): Float {
        if (previousLandmarks == null) return 0f

        val timeDiff = (currentLandmarks.timestamp - previousLandmarks.timestamp) / 1000f
        if (timeDiff == 0f) return 0f

        var totalDistance = 0f
        for (i in currentLandmarks.landmarks.indices) {
            val prev = previousLandmarks.landmarks[i]
            val current = currentLandmarks.landmarks[i]
            val distance = Math.sqrt(
                ((current.x - prev.x) * (current.x - prev.x) +
                        (current.y - prev.y) * (current.y - prev.y)).toDouble()
            ).toFloat()
            totalDistance += distance
        }

        return totalDistance / timeDiff
    }

    fun release() {
        hands.close()
    }

    companion object {
        // אינדקסים של נקודות חשובות בכף היד
        const val WRIST = 0
        const val THUMB_TIP = 4
        const val INDEX_FINGER_TIP = 8
        const val MIDDLE_FINGER_TIP = 12
        const val RING_FINGER_TIP = 16
        const val PINKY_TIP = 20

        // אינדקסים של קשרים בין נקודות
        const val PALM_CENTER = 9 // בין מרכז הכף ל-middle finger
    }
}
