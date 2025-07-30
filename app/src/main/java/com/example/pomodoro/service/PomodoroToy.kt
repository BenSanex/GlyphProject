package com.example.pomodoro.service

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import com.example.pomodoro.demos.GlyphMatrixService
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroToy : GlyphMatrixService("Pomodoro") {

    private var durationMillis: Long = DEFAULT_DURATION
    private var countdownJob: Job? = null
    private var ringtone: Ringtone? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        durationMillis = intent?.getLongExtra(EXTRA_DURATION, DEFAULT_DURATION) ?: DEFAULT_DURATION
        return START_NOT_STICKY
    }

    override fun performOnServiceConnected(context: Context, glyphMatrixManager: GlyphMatrixManager) {
        startCountdown()
    }

    override fun performOnServiceDisconnected(context: Context) {
        stopCountdown()
    }

    override fun onTouchPointLongPress() {
        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        if (durationMillis <= 0L) return
        countdownJob = serviceScope.launch {
            var remaining = durationMillis
            while (remaining >= 0 && isActive) {
                postTime(remaining)
                delay(1000L)
                remaining -= 1000L
            }
            playAlarm()
            stopSelf()
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        glyphMatrixManager?.turnOff()
    }

    private fun postTime(timeMs: Long) {
        glyphMatrixManager?.let { manager ->
            val totalSeconds = timeMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val minutesText = String.format("%02d", minutes)
            val secondsText = String.format("%02d", seconds)
            val minutesObject = GlyphMatrixObject.Builder()
                .setText(minutesText)
                .setPosition(8, 6)
                .build()
            val secondsObject = GlyphMatrixObject.Builder()
                .setText(secondsText)
                .setPosition(8, 14)
                .build()
            val frame = GlyphMatrixFrame.Builder()
                .addTop(minutesObject)
                .addLow(secondsObject)
                .build(applicationContext)
            manager.setMatrixFrame(frame.render())
        }
    }

    private fun playAlarm() {
        if (ringtone == null) {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        }
        ringtone?.play()
    }

    override fun onDestroy() {
        stopCountdown()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_DURATION = "extra_duration"
        private const val DEFAULT_DURATION = 25 * 60_000L
    }
}

