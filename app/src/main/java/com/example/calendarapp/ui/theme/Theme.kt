package com.example.calendarapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = White,
    primaryContainer = SoftBlue,
    onPrimaryContainer = Ink,
    secondary = Cyan,
    onSecondary = White,
    secondaryContainer = SoftCyan,
    onSecondaryContainer = Ink,
    tertiary = Violet,
    onTertiary = White,
    background = Cloud,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceContainer = Mist,
    surfaceContainerHigh = SoftBlue.copy(alpha = 0.45f),
    surfaceBright = White,
    onSurfaceVariant = Slate,
)

private val DarkColors = darkColorScheme(
    primary = SoftBlue,
    onPrimary = DarkBase,
    primaryContainer = DarkCard,
    onPrimaryContainer = White,
    secondary = SoftCyan,
    onSecondary = DarkBase,
    secondaryContainer = ColorTokens.darkSecondaryContainer,
    onSecondaryContainer = White,
    tertiary = SoftLavender,
    onTertiary = DarkBase,
    background = DarkBase,
    onBackground = White,
    surface = DarkCard,
    onSurface = White,
    surfaceContainer = ColorTokens.darkSurfaceContainer,
    surfaceContainerHigh = ColorTokens.darkSurfaceContainerHigh,
    surfaceBright = ColorTokens.darkSurfaceBright,
    onSurfaceVariant = DarkMuted,
)

private object ColorTokens {
    val darkSecondaryContainer = DarkCard.copy(alpha = 0.96f)
    val darkSurfaceContainer = DarkCard.copy(alpha = 0.98f)
    val darkSurfaceContainerHigh = DarkCard.copy(alpha = 0.82f)
    val darkSurfaceBright = DarkCard.copy(alpha = 0.72f)
}

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
