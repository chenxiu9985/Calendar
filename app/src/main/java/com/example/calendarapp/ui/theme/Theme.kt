package com.example.calendarapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue500,
    onPrimary = White,
    primaryContainer = Blue200,
    background = Sand50,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Sand200,
    onSurfaceVariant = Slate600,
)

private val DarkColors = darkColorScheme(
    primary = Blue200,
    onPrimary = Slate900,
    primaryContainer = Blue500,
    background = Slate900,
    onBackground = White,
    surface = Slate800,
    onSurface = White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Sand200,
)

@Composable
fun CalendarAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
