package com.example.handgesturevolume.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.ArrayDeque

/**
 * זיהוי נהיגה לפי GPS עם החלקת מהירות, היסטרזיס, ודיליי יציאה (לרמזורים וכדומה).
 */
class DrivingModeDetector(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val listeners = mutableListOf<DrivingModeListener>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isMonitoring = false
    private var isDriving = false

    private val speedWindow = ArrayDeque<Float>()
    private var lastLocationForSpeed: Location? = null
    private var enterStreak = 0

    private var pendingStopRunnable: Runnable? = null
    private var staleLocationRunnable: Runnable? = null

    interface DrivingModeListener {
        fun onDrivingModeChanged(isDriving: Boolean)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            scheduleStaleLocationGuard()
            val speedKmh = estimateSpeedKmh(location)
            pushSpeedSample(speedKmh)
            val smoothed = smoothedSpeedKmh()
            applyDrivingStateMachine(smoothed)
        }

        override fun onProviderEnabled(provider: String) {
            Log.d("DrivingMode", "Provider enabled: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d("DrivingMode", "Provider disabled: $provider")
            if (provider == LocationManager.GPS_PROVIDER) {
                forceDrivingOff("GPS provider disabled")
            }
        }

        @Deprecated("Deprecated in Android API")
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
    }

    /** מהירות משוערת: מהשקוע או לפי מרחק/זמן בין דגימות */
    private fun estimateSpeedKmh(location: Location): Float {
        val fromGpsMps = if (location.hasSpeed()) location.speed else null
        val prev = lastLocationForSpeed
        val gpsKmh = fromGpsMps?.times(3.6f)

        if (prev == null) {
            lastLocationForSpeed = Location(location)
            return gpsKmh ?: 0f
        }

        val dtNanos = location.elapsedRealtimeNanos - prev.elapsedRealtimeNanos
        val dtSec = dtNanos / 1_000_000_000.0
        val distM = prev.distanceTo(location).toDouble()
        val computedKmh = if (dtSec >= 0.25) {
            ((distM / dtSec) * 3.6).toFloat()
        } else {
            gpsKmh ?: 0f
        }

        lastLocationForSpeed = Location(location)

        return when {
            gpsKmh != null -> 0.65f * gpsKmh + 0.35f * computedKmh
            dtSec >= 0.25 -> computedKmh
            else -> gpsKmh ?: 0f
        }
    }

    private fun pushSpeedSample(speedKmh: Float) {
        val clamped = speedKmh.coerceIn(0f, 200f)
        speedWindow.addLast(clamped)
        while (speedWindow.size > SPEED_WINDOW_SIZE) {
            speedWindow.removeFirst()
        }
    }

    private fun smoothedSpeedKmh(): Float {
        if (speedWindow.isEmpty()) return 0f
        return speedWindow.average().toFloat()
    }

    private fun applyDrivingStateMachine(smoothedKmh: Float) {
        when {
            smoothedKmh >= DRIVING_ENTER_KMH -> {
                enterStreak++
                cancelPendingStop()
                if (!isDriving && enterStreak >= ENTER_CONFIRM_SAMPLES) {
                    isDriving = true
                    Log.d(
                        "DrivingMode",
                        "Driving ON (smoothed ${String.format("%.1f", smoothedKmh)} km/h)"
                    )
                    notifyListeners(true)
                }
            }

            smoothedKmh <= DRIVING_EXIT_KMH -> {
                enterStreak = 0
                if (isDriving) {
                    scheduleStopAfterGracePeriod(smoothedKmh)
                }
            }

            else -> {
                // אזור היסטרזיס: לא משנים מצב מדווח; לא מאפסים enter streak כדי לא לרפרף בכניסה
            }
        }
    }

    private fun cancelPendingStop() {
        pendingStopRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingStopRunnable = null
    }

    private fun scheduleStopAfterGracePeriod(smoothedKmh: Float) {
        if (pendingStopRunnable != null) return
        pendingStopRunnable = Runnable {
            pendingStopRunnable = null
            if (!isDriving) return@Runnable
            isDriving = false
            Log.d(
                "DrivingMode",
                "Driving OFF after grace (last smoothed ~${String.format("%.1f", smoothedKmh)} km/h)"
            )
            notifyListeners(false)
        }
        mainHandler.postDelayed(pendingStopRunnable!!, STOP_GRACE_PERIOD_MS)
        Log.d(
            "DrivingMode",
            "Stop grace started (${STOP_GRACE_PERIOD_MS / 1000}s) — smoothed ${String.format("%.1f", smoothedKmh)} km/h"
        )
    }

    private fun scheduleStaleLocationGuard() {
        staleLocationRunnable?.let { mainHandler.removeCallbacks(it) }
        staleLocationRunnable = Runnable {
            forceDrivingOff("No GPS update for ${NO_GPS_UPDATE_TIMEOUT_MS / 1000}s")
        }
        mainHandler.postDelayed(staleLocationRunnable!!, NO_GPS_UPDATE_TIMEOUT_MS)
    }

    private fun cancelStaleLocationGuard() {
        staleLocationRunnable?.let { mainHandler.removeCallbacks(it) }
        staleLocationRunnable = null
    }

    private fun forceDrivingOff(reason: String) {
        cancelPendingStop()
        enterStreak = 0
        if (isDriving) {
            isDriving = false
            Log.d("DrivingMode", "Driving OFF (forced): $reason")
            notifyListeners(false)
        } else {
            Log.d("DrivingMode", "Driving remains OFF: $reason")
            notifyListeners(false)
        }
    }

    /**
     * התחל ניטור GPS (רק GPS פיזי כדי להימנע מקונפליקטים עם רשת)
     */
    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        if (isMonitoring) return

        try {
            speedWindow.clear()
            lastLocationForSpeed = null
            enterStreak = 0

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GPS_MIN_TIME_MS,
                GPS_MIN_DISTANCE_M,
                locationListener
            )

            isMonitoring = true
            Log.d("DrivingMode", "Location monitoring started (smoothed / hysteresis / ${STOP_GRACE_PERIOD_MS / 1000}s stop grace)")
            scheduleStaleLocationGuard()
            // סנכרון ראשוני לשירות (כבוי נהיגה = השהיה עד שמזוהה מהירות)
            notifyListeners(isDriving)
        } catch (e: SecurityException) {
            Log.e("DrivingMode", "Location permission denied: ${e.message}")
        }
    }

    /**
     * עצור ניטור GPS
     */
    fun stopMonitoring() {
        if (!isMonitoring) return

        try {
            cancelPendingStop()
            cancelStaleLocationGuard()
            locationManager.removeUpdates(locationListener)
            isMonitoring = false
            isDriving = false
            speedWindow.clear()
            lastLocationForSpeed = null
            enterStreak = 0
            Log.d("DrivingMode", "Location monitoring stopped")
        } catch (e: Exception) {
            Log.e("DrivingMode", "Error stopping monitoring: ${e.message}")
        }
    }

    fun isDrivingMode(): Boolean = isDriving

    fun addListener(listener: DrivingModeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DrivingModeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(driving: Boolean) {
        listeners.forEach { it.onDrivingModeChanged(driving) }
    }

    companion object {
        private const val SPEED_WINDOW_SIZE = 6
        /** מעל סף זה — נחשב נהיגה (אחרי אישור כמה דגימות) */
        private const val DRIVING_ENTER_KMH = 8f
        /** מתחת לסף זה מתחיל טיימר יציאה (עם חצי דקה־ודקה grace) */
        private const val DRIVING_EXIT_KMH = 5f
        private const val ENTER_CONFIRM_SAMPLES = 3
        private const val STOP_GRACE_PERIOD_MS = 90_000L
        private const val NO_GPS_UPDATE_TIMEOUT_MS = 15_000L
        private const val GPS_MIN_TIME_MS = 2_000L
        private const val GPS_MIN_DISTANCE_M = 12f
    }
}
