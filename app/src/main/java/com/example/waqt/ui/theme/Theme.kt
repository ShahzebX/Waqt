package com.example.waqt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WaqtColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = SurfaceColor,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = PrimaryGreen,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary,
    surfaceVariant = Surface2Color,
    onSurfaceVariant = TextMuted,
    outline = BorderColor,
    error = ErrorColor
)

@Composable
fun WaqtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WaqtColorScheme,
        typography = Typography,
        content = content
    )
}