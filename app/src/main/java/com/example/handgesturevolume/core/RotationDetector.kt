package com.example.handgesturevolume.core

import android.graphics.PointF
import android.util.Log
import kotlin.math.*

/**
 * מגלה סיבוב של כף היד (עם כיוון השעון / נגד כיוון השעון)
 * משתמש בנקודות ממוקד החישוב ויחסי זוויות
 */
class RotationDetector {
    private var previousFingerPositions: Map<String, PointF>? = null
    private var rotationBuffer: MutableList<RotationEvent> = mutableListOf()
    private val bufferSize = 12 // גודל חלון לחישוב ממוצע

    data class RotationEvent(
        val direction: RotationDirection,
        val angle: Float, // מעלות של סיבוב
        val confidence: Float,
        val timestamp: Long = System.currentTimeMillis()
    )

    enum class RotationDirection {
        CLOCKWISE,
        COUNTER_CLOCKWISE,
        NONE
    }

    /**
     * מגלה סיבוב בהתבסס על נקודות היד
     */
    fun detectRotation(landmarks: List<PointF>, confidence: Float): RotationEvent? {
        if (landmarks.size < 21) return null

        val currentFingerPositions = extractFingerPositions(landmarks)
        
        val rotation = if (previousFingerPositions != null) {
            calculateRotation(previousFingerPositions!!, currentFingerPositions, confidence)
        } else {
            null
        }

        previousFingerPositions = currentFingerPositions

        if (rotation != null) {
            rotationBuffer.add(rotation)
            if (rotationBuffer.size > bufferSize) {
                rotationBuffer.removeAt(0)
            }
        }

        return rotation
    }

    /**
     * מחזיר את כיוון הסיבוב הממוצע מתוך מספר מדידות אחרונות
     */
    fun getAverageRotation(): RotationEvent? {
        if (rotationBuffer.isEmpty()) return null

        // סנן אירועים עם confidence נמוכה
        val validEvents = rotationBuffer.filter { it.confidence > 0.5f }
        if (validEvents.isEmpty()) return null

        // בדוק אם יש עדויות מובהקות של סיבוב בכיוון אחד
        val clockwiseCount = validEvents.count { it.direction == RotationDirection.CLOCKWISE }
        val counterClockwiseCount = validEvents.count { it.direction == RotationDirection.COUNTER_CLOCKWISE }

        val need = (validEvents.size * MAJORITY_FRACTION).toInt().coerceAtLeast(4)

        return when {
            clockwiseCount > counterClockwiseCount && clockwiseCount >= need -> {
                validEvents.filter { it.direction == RotationDirection.CLOCKWISE }.maxByOrNull { it.confidence }
            }
            counterClockwiseCount > clockwiseCount && counterClockwiseCount >= need -> {
                validEvents.filter { it.direction == RotationDirection.COUNTER_CLOCKWISE }.maxByOrNull { it.confidence }
            }
            else -> null
        }
    }

    /**
     * מחלץ מיקומים של אצבעות חשובות
     */
    private fun extractFingerPositions(landmarks: List<PointF>): Map<String, PointF> {
        return mapOf(
            "thumb" to landmarks[HandDetector.THUMB_TIP],
            "index" to landmarks[HandDetector.INDEX_FINGER_TIP],
            "middle" to landmarks[HandDetector.MIDDLE_FINGER_TIP],
            "ring" to landmarks[HandDetector.RING_FINGER_TIP],
            "pinky" to landmarks[HandDetector.PINKY_TIP],
            "wrist" to landmarks[HandDetector.WRIST],
            "palm" to landmarks[HandDetector.PALM_CENTER]
        )
    }

    /**
     * חישוב כיוון סיבוב באמצעות וקטורים
     * משווה בין כיוונים קודמים לנוכחיים
     */
    private fun calculateRotation(
        previous: Map<String, PointF>,
        current: Map<String, PointF>,
        confidence: Float
    ): RotationEvent {
        // חשב את הזווית של כל אצבע סביב מרכז הכף
        val prevAngles = calculateAngles(previous)
        val currentAngles = calculateAngles(current)

        // חשב את ההבדל בזוויות
        val angleDiffs = mutableListOf<Float>()
        for ((key, prevAngle) in prevAngles) {
            val currentAngle = currentAngles[key] ?: continue
            var diff = currentAngle - prevAngle

            // התאם ל-360 מעלות
            while (diff > 180) diff -= 360
            while (diff < -180) diff += 360

            angleDiffs.add(diff)
        }

        val averageDiff = if (angleDiffs.isNotEmpty()) {
            angleDiffs.average().toFloat()
        } else {
            0f
        }

        // סף זווית גבוה יותר — פחות רגישות לרעידות מצלמה / רקע
        val threshold = MIN_ANGLE_DEGREES

        return when {
            averageDiff > threshold -> {
                RotationEvent(
                    direction = RotationDirection.CLOCKWISE,
                    angle = averageDiff,
                    confidence = minOf(abs(averageDiff) / 30f, 1f) * confidence
                )
            }
            averageDiff < -threshold -> {
                RotationEvent(
                    direction = RotationDirection.COUNTER_CLOCKWISE,
                    angle = abs(averageDiff),
                    confidence = minOf(abs(averageDiff) / 30f, 1f) * confidence
                )
            }
            else -> {
                RotationEvent(
                    direction = RotationDirection.NONE,
                    angle = 0f,
                    confidence = 0f
                )
            }
        }
    }

    /**
     * חשב זוויות של אצבעות סביב מרכז הכף
     */
    private fun calculateAngles(positions: Map<String, PointF>): Map<String, Float> {
        val palmCenter = positions["palm"] ?: positions["wrist"] ?: PointF(0.5f, 0.5f)

        return positions.mapValues { (_, point) ->
            val dx = point.x - palmCenter.x
            val dy = point.y - palmCenter.y
            
            // חשב זווית בעזרת atan2
            var angle = atan2(dy.toDouble(), dx.toDouble()).toFloat() * 180 / PI.toFloat()
            if (angle < 0) angle += 360
            angle
        }
    }

    /**
     * הגדרת סף מינימלי לזיהוי סיבוב
     * ערכים גבוהים יותר = פחות רגישות, פחות false positives
     */
    fun setRotationThreshold(threshold: Float) {
        // ניתן להשתמש בזה להתאמה דינמית
        Log.d("RotationDetector", "Rotation threshold set to $threshold")
    }

    fun reset() {
        previousFingerPositions = null
        rotationBuffer.clear()
    }

    companion object {
        private const val MIN_ANGLE_DEGREES = 7f
        private const val MAJORITY_FRACTION = 0.68f
    }
}
