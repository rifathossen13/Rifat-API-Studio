package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RifatDarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF00363A),
  onPrimaryContainer = NeonCyan,
  secondary = NeonMagenta,
  onSecondary = Color.White,
  tertiary = NeonPurple,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = GlassBorder,
  error = NeonRed,
  onError = Color.White
)

@Composable
fun RifatApiStudioTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = RifatDarkColorScheme,
    typography = Typography,
    content = content
  )
}
