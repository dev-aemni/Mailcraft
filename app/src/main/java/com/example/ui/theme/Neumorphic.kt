package com.example.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Neumorphic Color Constants (Matches Provided CSS #ecf0f3) ---
val NeuLightBg = Color(0xFFECF0F3)
val NeuLightHighlight = Color(0xFFFFFFFF)
val NeuLightShadow = Color(0x2E000000)      // rgba(0, 0, 0, 0.18)
val NeuLightInnerShadow = Color(0x24000000)

val NeuLightTextPrimary = Color(0xFF3C414B)    // rgb(60, 65, 75)
val NeuLightTextSecondary = Color(0xFF6E737D)  // rgb(110, 115, 125)
val NeuLightTextMuted = Color(0xFF828791)      // rgb(130, 135, 145)

val NeuDarkBg = Color(0xFF1E2024)
val NeuDarkHighlight = Color(0xFF2B2E35)
val NeuDarkShadow = Color(0xFF101214)
val NeuDarkInnerShadow = Color(0x66000000)

val NeuDarkTextPrimary = Color(0xFFF1F5F9)
val NeuDarkTextSecondary = Color(0xFF94A3B8)
val NeuDarkTextMuted = Color(0xFF64748B)

val NeuAccent = Color(0xFF6366F1) // Indigo accent for primary actions

/**
 * Custom Neumorphic shadow modifier rendering dual light-highlight (top-left)
 * and dark-shadow (bottom-right) exactly as in the CSS example:
 * box-shadow: -5px -5px 12px #ffffff, 5px 5px 12px rgba(0, 0, 0, 0.16)
 */
fun Modifier.neuShadow(
  cornerRadius: Dp = 24.dp,
  offset: Dp = 4.dp,
  blurRadius: Dp = 10.dp,
  isInset: Boolean = false,
  isDark: Boolean = false
): Modifier = this.drawBehind {
  val highlightColor = if (isDark) NeuDarkHighlight else NeuLightHighlight
  val shadowColor = if (isDark) NeuDarkShadow else NeuLightShadow
  val cornerRadiusPx = cornerRadius.toPx()
  val offsetPx = offset.toPx()
  val blurPx = blurRadius.toPx().coerceAtLeast(1f)

  drawIntoCanvas { canvas ->
    if (!isInset) {
      // 1. Top-Left Light Highlight (-offset, -offset)
      val lightPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = highlightColor.toArgb()
        maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
      }
      canvas.nativeCanvas.drawRoundRect(
        -offsetPx,
        -offsetPx,
        size.width - offsetPx,
        size.height - offsetPx,
        cornerRadiusPx,
        cornerRadiusPx,
        lightPaint
      )

      // 2. Bottom-Right Dark Shadow (+offset, +offset)
      val darkPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = shadowColor.toArgb()
        maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
      }
      canvas.nativeCanvas.drawRoundRect(
        offsetPx,
        offsetPx,
        size.width + offsetPx,
        size.height + offsetPx,
        cornerRadiusPx,
        cornerRadiusPx,
        darkPaint
      )
    } else {
      // Inset Sunken Shadow: soft inner top-left shadow + inner bottom-right light
      val innerDarkColor = if (isDark) NeuDarkInnerShadow else NeuLightInnerShadow
      val innerPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = innerDarkColor.toArgb()
        maskFilter = BlurMaskFilter(blurPx / 2, BlurMaskFilter.Blur.NORMAL)
      }
      canvas.nativeCanvas.drawRoundRect(
        0f,
        0f,
        size.width,
        offsetPx * 2,
        cornerRadiusPx,
        cornerRadiusPx,
        innerPaint
      )
    }
  }
}

/**
 * NeuCard - Embodying the .neu-card CSS class with soft 24dp-30dp rounded corners
 * and prominent dual-tone neumorphic depth.
 */
@Composable
fun NeuCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 26.dp,
  isDark: Boolean = false,
  contentPadding: PaddingValues = PaddingValues(18.dp),
  content: @Composable BoxScope.() -> Unit
) {
  val bgColor = if (isDark) NeuDarkBg else NeuLightBg
  val shape = RoundedCornerShape(cornerRadius)

  Box(
    modifier = modifier
      .neuShadow(cornerRadius = cornerRadius, isDark = isDark)
      .clip(shape)
      .background(bgColor)
      .border(0.5.dp, if (isDark) Color(0xFF2C3038) else Color(0x60FFFFFF), shape)
      .padding(contentPadding),
    content = content
  )
}

/**
 * NeuInsetCard - Embodying the .ad-box inset container with recessed inner depth.
 * Ideal for text input boxes, code blocks, metrics, and sunken display containers.
 */
@Composable
fun NeuInsetCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 20.dp,
  isDark: Boolean = false,
  contentPadding: PaddingValues = PaddingValues(14.dp),
  content: @Composable BoxScope.() -> Unit
) {
  val bgColor = if (isDark) Color(0xFF1A1C20) else Color(0xFFE5E9EE)
  val shape = RoundedCornerShape(cornerRadius)

  Box(
    modifier = modifier
      .neuShadow(cornerRadius = cornerRadius, isInset = true, isDark = isDark)
      .clip(shape)
      .background(bgColor)
      .border(1.dp, if (isDark) Color(0xFF141619) else Color(0xFFD6DBE2), shape)
      .padding(contentPadding),
    content = content
  )
}

/**
 * NeuButton - Interactive neumorphic button with raised state that compresses into
 * an inset / flat state when pressed, matching .repo-card:active.
 */
@Composable
fun NeuButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPrimary: Boolean = false,
  cornerRadius: Dp = 22.dp,
  isDark: Boolean = false,
  enabled: Boolean = true,
  contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val baseBg = when {
    !enabled -> if (isDark) Color(0xFF16181B) else Color(0xFFE2E6EA)
    isPrimary -> NeuAccent
    isDark -> NeuDarkBg
    else -> NeuLightBg
  }

  val textColor = when {
    !enabled -> if (isDark) NeuDarkTextMuted else NeuLightTextMuted
    isPrimary -> Color.White
    isDark -> NeuDarkTextPrimary
    else -> NeuLightTextPrimary
  }

  val shape = RoundedCornerShape(cornerRadius)

  Box(
    modifier = modifier
      .then(
        if (enabled && !isPressed) {
          Modifier.neuShadow(cornerRadius = cornerRadius, isDark = isDark)
        } else {
          Modifier
        }
      )
      .clip(shape)
      .background(baseBg)
      .then(
        if (isPressed) {
          Modifier.border(1.dp, if (isDark) Color(0xFF121417) else Color(0xFFCBD2DC), shape)
        } else {
          Modifier.border(0.5.dp, if (isDark) Color(0xFF2C3038) else Color(0x60FFFFFF), shape)
        }
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .padding(contentPadding),
    contentAlignment = Alignment.Center
  ) {
    ProvideTextStyle(
      value = TextStyle(
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    ) {
      CompositionLocalProvider(LocalContentColor provides textColor) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          content = content
        )
      }
    }
  }
}

/**
 * NeuIconButton - Circular soft-embossed icon button
 */
@Composable
fun NeuIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isDark: Boolean = false,
  enabled: Boolean = true,
  content: @Composable () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val bgColor = if (isDark) NeuDarkBg else NeuLightBg

  Box(
    modifier = modifier
      .then(
        if (enabled && !isPressed) {
          Modifier.neuShadow(cornerRadius = 24.dp, isDark = isDark)
        } else {
          Modifier
        }
      )
      .clip(CircleShape)
      .background(bgColor)
      .border(0.5.dp, if (isDark) Color(0xFF2C3038) else Color(0x80FFFFFF), CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .padding(10.dp),
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}

/**
 * NeuPill - Sleek toggle chip with selected / unselected neumorphic elevation
 */
@Composable
fun NeuPill(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isDark: Boolean = false,
  content: @Composable RowScope.() -> Unit
) {
  val shape = RoundedCornerShape(16.dp)
  val bgColor = when {
    selected -> if (isDark) Color(0xFF312E81) else Color(0xFFE0E7FF)
    isDark -> NeuDarkBg
    else -> NeuLightBg
  }
  val textColor = when {
    selected -> if (isDark) Color(0xFFC7D2FE) else Color(0xFF4338CA)
    isDark -> NeuDarkTextSecondary
    else -> NeuLightTextSecondary
  }

  Box(
    modifier = modifier
      .then(
        if (!selected) {
          Modifier.neuShadow(cornerRadius = 16.dp, blurRadius = 6.dp, offset = 2.dp, isDark = isDark)
        } else {
          Modifier
        }
      )
      .clip(shape)
      .background(bgColor)
      .border(
        width = if (selected) 1.5.dp else 0.5.dp,
        color = if (selected) (if (isDark) Color(0xFF6366F1) else Color(0xFF818CF8)) else (if (isDark) Color(0xFF2C3038) else Color(0x60FFFFFF)),
        shape = shape
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    ProvideTextStyle(
      value = TextStyle(
        color = textColor,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.sp
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        content = content
      )
    }
  }
}
