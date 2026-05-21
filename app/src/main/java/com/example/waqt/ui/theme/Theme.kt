package com.example.waqt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WaqtColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = OnPrimaryNavy,
    primaryContainer = NavyMid,
    onPrimaryContainer = OnPrimaryNavy,
    secondary = SecondaryGold,
    onSecondary = PrimaryNavy,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = PrimaryNavy,
    tertiary = NavySoft,
    onTertiary = OnPrimaryNavy,
    background = NeutralBackground,
    onBackground = TextPrimary,
    surface = SurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = SoftIceBlue,
    onSurfaceVariant = TextMuted,
    outline = OutlineStroke,
    outlineVariant = OutlineSoft,
    error = ErrorColor,
    onError = OnPrimaryNavy
)

@Composable
fun WaqtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WaqtColorScheme,
        typography = Typography,
        shapes = WaqtShapes,
        content = content
    )
}
