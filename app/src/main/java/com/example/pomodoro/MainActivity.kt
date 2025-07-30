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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    private fun stopPomodoroService() {
        val intent = Intent(this, PomodoroService::class.java)
        stopService(intent)
    }

    @Composable
    private fun PomodoroScreen(modifier: Modifier = Modifier) {
        val minuteText = remember { mutableStateOf("25") }
        val running = remember { mutableStateOf(false) }
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pomodoro Timer",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Stay focused! \uD83C\uDF45",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = minuteText.value,
                onValueChange = { minuteText.value = it.filter { ch -> ch.isDigit() } },
                label = { Text("Minutes") },
                enabled = !running.value,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (running.value) {
                        stopPomodoroService()
                    } else {
                        val minutes = minuteText.value.toIntOrNull() ?: 25
                        startServiceWithMinutes(minutes)
                    }
                    running.value = !running.value
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(if (running.value) "Stop" else "Start")
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

