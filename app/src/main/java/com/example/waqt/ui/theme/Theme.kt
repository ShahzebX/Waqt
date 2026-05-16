package com.example.waqt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WaqtColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = SurfaceColor,
    primaryContainer = TertiaryIce,
    onPrimaryContainer = PrimaryNavy,
    secondary = SecondaryGold,
    onSecondary = SurfaceColor,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary,
    surfaceVariant = TertiaryIce,
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