package com.example.pomodoro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import com.example.pomodoro.ui.theme.DigitalBackground
import com.example.pomodoro.ui.theme.DigitalGreen
import androidx.compose.runtime.Composable

private val DigitalColorScheme = darkColorScheme(
    primary = DigitalGreen,
    onPrimary = DigitalBackground,
    secondary = DigitalGreen,
    tertiary = DigitalGreen,
    background = DigitalBackground,
    surface = DigitalBackground,
    onBackground = DigitalGreen,
    onSurface = DigitalGreen
)

@Composable
fun PomodoroTheme(content: @Composable () -> Unit) {
    val colorScheme = DigitalColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}