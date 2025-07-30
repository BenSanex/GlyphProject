package com.example.pomodoro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.pomodoro.service.PomodoroService
import com.example.pomodoro.ui.theme.PomodoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PomodoroTheme {
                Scaffold { innerPadding ->
                    PomodoroScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun startServiceWithMinutes(minutes: Int) {
        val duration = minutes * 60_000L
        val intent = Intent(this, PomodoroService::class.java).apply {
            putExtra(PomodoroService.EXTRA_DURATION, duration)
        }
        startService(intent)
    }

    private fun startServiceWithDuration(duration: Long) {
        val intent = Intent(this, PomodoroService::class.java).apply {
            putExtra(PomodoroService.EXTRA_DURATION, duration)
        }
        startService(intent)
    }

    private fun stopPomodoroService() {
        val intent = Intent(this, PomodoroService::class.java)
        stopService(intent)
    }

    @Composable
    private fun PomodoroScreen(modifier: Modifier = Modifier) {
        val minuteText = remember { mutableStateOf("25") }
        val running = remember { mutableStateOf(false) }
        val remaining = remember { mutableLongStateOf(25 * 60_000L) }
        val initialDuration = remember { mutableLongStateOf(25 * 60_000L) }

        LaunchedEffect(running.value) {
            while (running.value && remaining.value > 0) {
                delay(1000L)
                remaining.value -= 1000L
            }
            if (remaining.value <= 0L) running.value = false
        }

        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ben's awesome vibe coded glyph matrix pomodoro timer app",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val minutes = (remaining.value / 1000) / 60
            val seconds = (remaining.value / 1000) % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (!running.value) {
                OutlinedTextField(
                    value = minuteText.value,
                    onValueChange = { minuteText.value = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val minutes = minuteText.value.toIntOrNull() ?: 25
                        val duration = minutes * 60_000L
                        remaining.value = duration
                        initialDuration.value = duration
                        startServiceWithDuration(duration)
                        running.value = true
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Start")
                }
            } else {
                Button(
                    onClick = {
                        stopPomodoroService()
                        running.value = false
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Pause")
                }
            }

            Button(
                onClick = {
                    stopPomodoroService()
                    remaining.value = initialDuration.value
                    running.value = false
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Reset")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        PomodoroTheme {
            PomodoroScreen()
        }
    }
}

