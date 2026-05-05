package com.example.handgesturevolume.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.handgesturevolume.R
import com.example.handgesturevolume.service.HandGestureService
import com.example.handgesturevolume.utils.PermissionManager

/**
 * Activity ראשי - UI לשליטה בשירות
 */
class MainActivity : AppCompatActivity() {
    private lateinit var toggleSwitch: SwitchCompat
    private lateinit var statusText: TextView
    private lateinit var pauseButton: Button
    private var isServiceRunning = false
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // בדוק ובקש הרשאות
        if (!PermissionManager.hasAllPermissions(this)) {
            PermissionManager.requestPermissions(this)
        }

        // קשירת UI
        setupUI()
    }

    private fun setupUI() {
        toggleSwitch = findViewById(R.id.toggleSwitch)
        statusText = findViewById(R.id.statusText)
        pauseButton = findViewById(R.id.pauseButton)

        // מטפל לטוגל ההפעלה
        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!PermissionManager.hasAllPermissions(this)) {
                    Toast.makeText(this, "אנא אשר את כל ההרשאות", Toast.LENGTH_SHORT).show()
                    toggleSwitch.isChecked = false
                    return@setOnCheckedChangeListener
                }
                startGestureService()
            } else {
                stopGestureService()
            }
        }

        // מטפל ל-Pause/Resume
        pauseButton.setOnClickListener {
            if (isServiceRunning) {
                togglePauseService()
            }
        }

        updateStatus()
    }

    /**
     * התחל שירות זיהוי יד
     */
    private fun startGestureService() {
        try {
            val serviceIntent = Intent(this, HandGestureService::class.java).apply {
                action = HandGestureService.ACTION_START
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            isServiceRunning = true
            isPaused = false
            updateStatus()
            Log.d("MainActivity", "Hand Gesture Service started")
            Toast.makeText(this, "שירות זיהוי יד הופעל", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting service: ${e.message}")
            toggleSwitch.isChecked = false
            Toast.makeText(this, "שגיאה בהפעלת השירות", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * עצור שירות זיהוי יד
     */
    private fun stopGestureService() {
        try {
            val serviceIntent = Intent(this, HandGestureService::class.java).apply {
                action = HandGestureService.ACTION_STOP
            }
            stopService(serviceIntent)

            isServiceRunning = false
            isPaused = false
            updateStatus()
            Log.d("MainActivity", "Hand Gesture Service stopped")
            Toast.makeText(this, "שירות זיהוי יד כבוי", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error stopping service: ${e.message}")
        }
    }

    /**
     * הפסק/חזור לניטור
     */
    private fun togglePauseService() {
        val action = if (isPaused) {
            HandGestureService.ACTION_RESUME
        } else {
            HandGestureService.ACTION_PAUSE
        }

        val serviceIntent = Intent(this, HandGestureService::class.java).apply {
            this.action = action
        }
        startService(serviceIntent)
        isPaused = !isPaused
        updateStatus()
    }

    /**
     * עדכן טקסט הסטטוס
     */
    private fun updateStatus() {
        val status = when {
            !isServiceRunning -> "כבוי"
            isPaused -> "הושהה"
            else -> "פעיל"
        }

        statusText.text = "סטטוס: $status"

        pauseButton.text = if (isPaused) "חזור לניטור" else "השהה ניטור"
        pauseButton.isEnabled = isServiceRunning
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE) {
            if (PermissionManager.hasAllPermissions(this)) {
                Toast.makeText(this, "הרשאות אושרו", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "נדרשות הרשאות לאפליקציה לעבוד כראוי", Toast.LENGTH_LONG).show()
            }
        }
    }
}
