package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val FireOrange = Color(0xFFFF6B00)
val FireOrangeLight = Color(0xFFFFA000)
val FireYellow = Color(0xFFFFD54F)
val FireRed = Color(0xFFFF3D00)

val DiamondCyan = Color(0xFF00E5FF)
val DiamondCyanDark = Color(0xFF00B0FF)
val DiamondCyanLight = Color(0xFF80D8FF)

val GoldAccent = Color(0xFFFFD700)
val EmeraldGreen = Color(0xFF00E676)
val ErrorRed = Color(0xFFFF5252)

val DarkBackground = Color(0xFF0B0C10)
val DarkSurface = Color(0xFF14161F)
val DarkSurfaceCard = Color(0xFF1B1E2B)
val DarkSurfaceElevated = Color(0xFF242838)
val DarkSurfaceBorder = Color(0xFF2E3349)

val LightText = Color(0xFFFFFFFF)
val SecondaryText = Color(0xFFB0B5C9)
val MutedText = Color(0xFF757A90)

// Brushes
val FireGradient = Brush.horizontalGradient(
    colors = listOf(FireRed, FireOrange, FireOrangeLight)
)

val DiamondGradient = Brush.linearGradient(
    colors = listOf(DiamondCyan, DiamondCyanDark)
)

val GoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFDF00), Color(0xFFFFA500))
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(DarkSurfaceElevated, DarkSurfaceCard)
)
