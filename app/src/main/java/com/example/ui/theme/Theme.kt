package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalPrimary,
    secondary = NaturalSecondary,
    tertiary = NaturalTertiary,
    background = NaturalBackground,
    surface = NaturalSurface,
    onPrimary = NaturalOnPrimary,
    onBackground = NaturalOnBackground,
    onSurface = NaturalOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalPrimary,
    secondary = NaturalSecondary,
    tertiary = NaturalTertiary,
    background = NaturalBackground,
    surface = NaturalSurface,
    onPrimary = NaturalOnPrimary,
    onBackground = NaturalOnBackground,
    onSurface = NaturalOnSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to enforce Natural Tones theme styling
  dynamicColor: Boolean = false,
  theme: String = "azul",
  content: @Composable () -> Unit,
) {
  val primaryColor = when(theme) {
    "verde" -> Color(0xFF16A34A)
    "minimalista" -> Color(0xFF1F2937)
    "naranja" -> Color(0xFFEA580C)
    "purpura" -> Color(0xFF7C3AED)
    else -> Color(0xFF0061A4)
  }

  val lightColorActive = LightColorScheme.copy(
    primary = primaryColor,
    secondary = primaryColor
  )
  val darkColorActive = DarkColorScheme.copy(
    primary = primaryColor,
    secondary = primaryColor
  )

  val colorScheme =
    when {
      darkTheme -> darkColorActive
      else -> lightColorActive
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
