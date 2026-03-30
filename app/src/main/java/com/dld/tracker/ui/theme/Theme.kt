package com.dld.tracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentGreen,
    tertiary = AccentOrange,
    background = DarkNavy,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCard,
    onPrimary = DarkNavy,
    onSecondary = DarkNavy,
    onTertiary = DarkNavy,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = Color.White,
    outline = TextMuted
)

@Composable
fun DLTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
