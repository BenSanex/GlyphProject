package com.example.pomodoro.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.nothing.ketchum.Glyph
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

class PomodoroService : Service() {

    private var glyphMatrixManager: GlyphMatrixManager? = null
    private val gmmCallback = object : GlyphMatrixManager.Callback {
        override fun onServiceConnected(name: android.content.ComponentName?) {
            glyphMatrixManager?.register(Glyph.DEVICE_23112)
            startCountdown()
        }

        override fun onServiceDisconnected(name: android.content.ComponentName?) {}
    }

    private var durationMillis: Long = 0L
    private var countdownJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        durationMillis = intent?.getLongExtra(EXTRA_DURATION, 0L) ?: 0L
        GlyphMatrixManager.getInstance(applicationContext)?.let { gmm ->
            glyphMatrixManager = gmm
            gmm.init(gmmCallback)
        }
        return START_NOT_STICKY
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
            stopSelf()
        }
    }

    private fun postTime(timeMs: Long) {
        glyphMatrixManager?.let { manager ->
            val totalSeconds = timeMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val text = String.format("%02d:%02d", minutes, seconds)
            val textObject = GlyphMatrixObject.Builder()
                .setText(text)
                .setPosition(2, 10)
                .build()
            val frame = GlyphMatrixFrame.Builder()
                .addTop(textObject)
                .build(applicationContext)
            manager.setMatrixFrame(frame.render())
        }
    }

    override fun onDestroy() {
        countdownJob?.cancel()
        serviceScope.cancel()
        glyphMatrixManager?.turnOff()
        glyphMatrixManager?.unInit()
        glyphMatrixManager = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_DURATION = "extra_duration"
    }
}

