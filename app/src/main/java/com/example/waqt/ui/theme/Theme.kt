package com.example.waqt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WaqtColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = OnPrimaryNavy,
    primaryContainer = SoftIceBlue,
    onPrimaryContainer = PrimaryNavy,
    secondary = SecondaryGold,
    onSecondary = PrimaryNavy,
    background = NeutralBackground,
    onBackground = TextPrimary,
    surface = SoftIceBlue,
    onSurface = TextPrimary,
    surfaceVariant = SoftIceBlue,
    onSurfaceVariant = TextMuted,
    outline = OutlineStroke,
    error = ErrorColor,
    onError = OnPrimaryNavy
)

@Composable
fun WaqtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WaqtColorScheme,
        typography = Typography,
        content = content
    )
}