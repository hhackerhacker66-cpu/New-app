package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FireOrange,
    onPrimary = LightText,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = FireOrangeLight,
    secondary = DiamondCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceBorder,
    onSecondaryContainer = DiamondCyanLight,
    tertiary = GoldAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = SecondaryText,
    outline = DarkSurfaceBorder,
    error = ErrorRed,
    onError = LightText
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
