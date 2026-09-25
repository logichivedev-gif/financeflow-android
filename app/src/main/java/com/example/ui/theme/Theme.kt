package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF38BDF8),
    tertiary = NaturalTertiary,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalPrimary,
    secondary = NaturalSecondary,
    tertiary = NaturalTertiary,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  
  dynamicColor: Boolean = false,
  theme: String = "azul",
  content: @Composable () -> Unit,
) {
  val primaryColor = when(theme) {
    "verde" -> if (darkTheme) Color(0xFF4ADE80) else Color(0xFF16A34A)
    "minimalista" -> if (darkTheme) Color(0xFF9CA3AF) else Color(0xFF1F2937)
    "naranja" -> if (darkTheme) Color(0xFFFB923C) else Color(0xFFEA580C)
    "purpura" -> if (darkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED)
    else -> if (darkTheme) Color(0xFF38BDF8) else Color(0xFF0061A4)
  }

  val lightColorActive = LightColorScheme.copy(
    primary = primaryColor,
    secondary = primaryColor
  )
  val darkColorActive = DarkColorScheme.copy(
    primary = primaryColor,
    secondary = primaryColor
  )

  val colorScheme = if (darkTheme) darkColorActive else lightColorActive

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
