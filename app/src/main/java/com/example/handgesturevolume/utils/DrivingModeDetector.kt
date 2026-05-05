package com.example.handgesturevolume.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

/**
 * זוהה אם המשתמש נוהג בעזרת GPS ומהירות
 * מפעיל את האפליקציה אוטומטית כאשר מזוהה נהיגה
 */
class DrivingModeDetector(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val listeners = mutableListOf<DrivingModeListener>()

    private var isMonitoring = false
    private var isDriving = false

    interface DrivingModeListener {
        fun onDrivingModeChanged(isDriving: Boolean)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // בדוק אם המהירות גדולה מ-5 קמ"ש (זיהוי בסיסי לנהיגה)
            val speed = location.speed // במטרים לשנייה
            val speedKmh = speed * 3.6f // המרה לקמ"ש

            val newDrivingState = speedKmh > 5f && location.hasSpeed()

            if (newDrivingState != isDriving) {
                isDriving = newDrivingState
                Log.d("DrivingMode", "Driving state changed: $isDriving (Speed: ${String.format("%.1f", speedKmh)} km/h)")
                notifyListeners(isDriving)
            }
        }

        override fun onProviderEnabled(provider: String) {
            Log.d("DrivingMode", "Provider enabled: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d("DrivingMode", "Provider disabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
    }

    /**
     * התחל ניטור GPS
     */
    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        if (isMonitoring) return

        try {
            // בקש עדכונים מ-GPS כל 1 שנייה או כל 10 מטרים
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000, // minTime: 1 second
                10f, // minDistance: 10 meters
                locationListener
            )

            // גם לזיהוי עם network provider לחיסכון סוללה
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000,
                50f,
                locationListener
            )

            isMonitoring = true
            Log.d("DrivingMode", "Location monitoring started")
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
            locationManager.removeUpdates(locationListener)
            isMonitoring = false
            Log.d("DrivingMode", "Location monitoring stopped")
        } catch (e: Exception) {
            Log.e("DrivingMode", "Error stopping monitoring: ${e.message}")
        }
    }

    /**
     * בדיקת מצב נהיגה נוכחי
     */
    fun isDrivingMode(): Boolean = isDriving

    /**
     * הרשמה לשינויים במצב נהיגה
     */
    fun addListener(listener: DrivingModeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DrivingModeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(isDriving: Boolean) {
        listeners.forEach { it.onDrivingModeChanged(isDriving) }
    }
}
