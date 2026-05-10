package com.example.handgesturevolume.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private var isDrivingMode = false
    private var pausedForStationary = false

    private val toggleListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            if (!PermissionManager.hasAllPermissions(this)) {
                Toast.makeText(this, "אנא אשר את כל ההרשאות", Toast.LENGTH_SHORT).show()
                toggleSwitch.isChecked = false
                return@OnCheckedChangeListener
            }
            startGestureService()
        } else {
            stopGestureService()
        }
    }

    private val uiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != HandGestureService.ACTION_UI_STATE) return
            isPaused = intent.getBooleanExtra(HandGestureService.EXTRA_SERVICE_PAUSED, isPaused)
            isServiceRunning = intent.getBooleanExtra(HandGestureService.EXTRA_SERVICE_RUNNING, isServiceRunning)
            isDrivingMode = intent.getBooleanExtra(HandGestureService.EXTRA_DRIVING_MODE, isDrivingMode)
            pausedForStationary =
                intent.getBooleanExtra(HandGestureService.EXTRA_PAUSED_FOR_STATIONARY, pausedForStationary)
            setToggleSilently(isServiceRunning)
            updateStatus()
        }
    }

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

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            uiStateReceiver,
            IntentFilter(HandGestureService.ACTION_UI_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(uiStateReceiver)
    }

    private fun setupUI() {
        toggleSwitch = findViewById(R.id.toggleSwitch)
        statusText = findViewById(R.id.statusText)
        pauseButton = findViewById(R.id.pauseButton)

        toggleSwitch.setOnCheckedChangeListener(toggleListener)

        // מטפל ל-Pause/Resume
        pauseButton.setOnClickListener {
            if (isServiceRunning) {
                togglePauseService()
            }
        }

        updateStatus()
    }

    private fun setToggleSilently(checked: Boolean) {
        toggleSwitch.setOnCheckedChangeListener(null)
        toggleSwitch.isChecked = checked
        toggleSwitch.setOnCheckedChangeListener(toggleListener)
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
            isPaused -> {
                if (pausedForStationary) "מושהה (עצירה / רמזור)" else "מושהה (ידני)"
            }
            else -> {
                if (isDrivingMode) "פעיל — נהיגה" else "פעיל"
            }
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
