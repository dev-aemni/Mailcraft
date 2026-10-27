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

// Neumorphic Light Theme (Matching user's CSS #ecf0f3)
private val NeuLightColorScheme = lightColorScheme(
  primary = NeuAccent,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0E7FF),
  onPrimaryContainer = Color(0xFF312E81),
  secondary = Color(0xFF7C3AED),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF3E8FF),
  onSecondaryContainer = Color(0xFF581C87),
  background = NeuLightBg,
  onBackground = NeuLightTextPrimary,
  surface = NeuLightBg,
  onSurface = NeuLightTextPrimary,
  surfaceVariant = Color(0xFFE4E9EE),
  onSurfaceVariant = NeuLightTextSecondary,
  outline = Color(0xFFD1D9E6),
  outlineVariant = Color(0xFFE2E8F0),
  error = ErrorRed,
  onError = Color.White
)

// Neumorphic Dark Theme (Deep soft-recessed dark aesthetic)
private val NeuDarkColorScheme = darkColorScheme(
  primary = Color(0xFF818CF8),
  onPrimary = Color(0xFF1E1B4B),
  primaryContainer = Color(0xFF312E81),
  onPrimaryContainer = Color(0xFFE0E7FF),
  secondary = Color(0xFFA78BFA),
  onSecondary = Color(0xFF2E1065),
  secondaryContainer = Color(0xFF4C1D95),
  onSecondaryContainer = Color(0xFFF5F3FF),
  background = NeuDarkBg,
  onBackground = NeuDarkTextPrimary,
  surface = NeuDarkBg,
  onSurface = NeuDarkTextPrimary,
  surfaceVariant = Color(0xFF282B31),
  onSurfaceVariant = NeuDarkTextSecondary,
  outline = Color(0xFF373B44),
  outlineVariant = Color(0xFF23262C),
  error = Color(0xFFF87171),
  onError = Color(0xFF450A0A)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> NeuDarkColorScheme
    else -> NeuLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
