package com.example.handgesturevolume.utils

import android.content.Context
import android.media.AudioManager
import android.util.Log

/**
 * שליטה בעוצמת השמע של המערכת
 */
class VolumeController(private val context: Context) {

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    enum class VolumeAction {
        UP,
        DOWN,
        MUTE,
        UNMUTE
    }

    /**
     * קפיצה של 20% למעלה
     */
    fun volumeUp() {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val step = (max * VOLUME_STEP_FRACTION).toInt().coerceAtLeast(1)
        val newVolume = (current + step).coerceAtMost(max)

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            newVolume,
            AudioManager.FLAG_SHOW_UI
        )

        Log.d("VolumeController", "Volume UP 20%: $current -> $newVolume")
    }

    /**
     * קפיצה של 20% למטה
     */
    fun volumeDown() {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val step = (max * VOLUME_STEP_FRACTION).toInt().coerceAtLeast(1)
        val newVolume = (current - step).coerceAtLeast(0)

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            newVolume,
            AudioManager.FLAG_SHOW_UI
        )

        Log.d("VolumeController", "Volume DOWN 20%: $current -> $newVolume")
    }

    /**
     * השתק/בטל השתקה
     */
    fun toggleMute() {
        val isMuted = isMuted()

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (isMuted) AudioManager.ADJUST_UNMUTE else AudioManager.ADJUST_MUTE,
            AudioManager.FLAG_SHOW_UI
        )

        Log.d("VolumeController", "Mute toggled: $isMuted")
    }

    companion object {
        private const val VOLUME_STEP_FRACTION = 0.2f
    }

    /**
     * קבלת עוצמה נוכחית (0–1)
     */
    fun getCurrentVolume(): Float {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return current.toFloat() / max.toFloat()
    }

    /**
     * מקסימום ווליום
     */
    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * בדיקה אם מושתק
     */
    fun isMuted(): Boolean {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }

    /**
     * ביצוע פעולה כללית
     */
    fun performAction(action: VolumeAction) {
        when (action) {
            VolumeAction.UP -> volumeUp()
            VolumeAction.DOWN -> volumeDown()
            VolumeAction.MUTE -> if (!isMuted()) toggleMute()
            VolumeAction.UNMUTE -> if (isMuted()) toggleMute()
        }
    }
}