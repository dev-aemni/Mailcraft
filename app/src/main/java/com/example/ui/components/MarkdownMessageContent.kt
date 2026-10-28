package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeuAccent
import com.example.ui.theme.NeuButton
import com.example.ui.theme.NeuInsetCard

/**
 * Rich Markdown renderer for MailCraft AI chat bubbles.
 * Supports:
 * - Code fences (```html, ```css, etc.) with Copy & Load buttons
 * - Headers (#, ##, ###)
 * - Numbered lists (1. , 2. ) & bullet points (- , * , • )
 * - Bold (**bold**) & inline code (`code`)
 */
@Composable
fun MarkdownMessageContent(
  content: String,
  isDark: Boolean,
  textPrimary: Color,
  textSecondary: Color,
  onApplyHtml: (String) -> Unit,
  onCopy: (String) -> Unit
) {
  val codeBlockRegex = Regex("""```([a-zA-Z0-9_-]*)\n?([\s\S]*?)```""")
  val matches = codeBlockRegex.findAll(content).toList()

  if (matches.isEmpty()) {
    // No code blocks: render pure formatted Markdown text
    MarkdownTextBlock(
      markdown = content,
      isDark = isDark,
      textPrimary = textPrimary,
      textSecondary = textSecondary
    )
    return
  }

  // Interleave text blocks and code blocks
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    var lastIndex = 0

    matches.forEach { match ->
      val start = match.range.first
      val end = match.range.last + 1

      // Text before code block
      if (start > lastIndex) {
        val textSegment = content.substring(lastIndex, start).trim()
        if (textSegment.isNotBlank()) {
          MarkdownTextBlock(
            markdown = textSegment,
            isDark = isDark,
            textPrimary = textPrimary,
            textSecondary = textSecondary
          )
        }
      }

      val language = match.groupValues.getOrNull(1)?.trim()?.lowercase().orEmpty().ifBlank { "code" }
      val code = match.groupValues.getOrNull(2)?.trim().orEmpty()

      if (code.isNotBlank()) {
        val isHtmlCode = language == "html" || code.contains("<html", ignoreCase = true) ||
          code.contains("<!DOCTYPE", ignoreCase = true) ||
          code.contains("<table", ignoreCase = true) ||
          code.contains("<div", ignoreCase = true)

        CodeBlockCard(
          code = code,
          language = language,
          isHtmlCode = isHtmlCode,
          isDark = isDark,
          textSecondary = textSecondary,
          onApplyHtml = onApplyHtml,
          onCopy = onCopy
        )
      }

      lastIndex = end
    }

    // Text after last code block
    if (lastIndex < content.length) {
      val textSegment = content.substring(lastIndex).trim()
      if (textSegment.isNotBlank()) {
        MarkdownTextBlock(
          markdown = textSegment,
          isDark = isDark,
          textPrimary = textPrimary,
          textSecondary = textSecondary
        )
      }
    }
  }
}

@Composable
private fun MarkdownTextBlock(
  markdown: String,
  isDark: Boolean,
  textPrimary: Color,
  textSecondary: Color
) {
  val lines = markdown.lines()

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    lines.forEach { rawLine ->
      val line = rawLine.trimEnd()
      when {
        line.isBlank() -> {
          Spacer(modifier = Modifier.height(4.dp))
        }
        // H3 Header
        line.startsWith("### ") -> {
          val headingText = line.removePrefix("### ").trim()
          Text(
            text = parseInlineMarkdown(headingText, textPrimary, isDark),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NeuAccent,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
          )
        }
        // H2 Header
        line.startsWith("## ") -> {
          val headingText = line.removePrefix("## ").trim()
          Text(
            text = parseInlineMarkdown(headingText, textPrimary, isDark),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NeuAccent,
            modifier = Modifier.padding(top = 10.dp, bottom = 3.dp)
          )
        }
        // H1 Header
        line.startsWith("# ") -> {
          val headingText = line.removePrefix("# ").trim()
          Text(
            text = parseInlineMarkdown(headingText, textPrimary, isDark),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
          )
        }
        // Numbered list item: "1. ", "2. ", etc.
        line.matches(Regex("""^\s*\d+[.)]\s+.*""")) -> {
          val match = Regex("""^\s*(\d+)[.)]\s+(.*)""").find(line)
          val num = match?.groupValues?.getOrNull(1) ?: "1"
          val itemText = match?.groupValues?.getOrNull(2) ?: line

          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(NeuAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = num,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeuAccent
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = parseInlineMarkdown(itemText, textPrimary, isDark),
              color = textPrimary,
              fontSize = 13.sp,
              lineHeight = 18.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
        // Bullet list item: "- ", "* ", "• "
        line.matches(Regex("""^\s*[-*•]\s+.*""")) -> {
          val itemText = line.replace(Regex("""^\s*[-*•]\s+"""), "")

          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(NeuAccent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = parseInlineMarkdown(itemText, textPrimary, isDark),
              color = textPrimary,
              fontSize = 13.sp,
              lineHeight = 18.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
        // Standard text paragraph
        else -> {
          Text(
            text = parseInlineMarkdown(line, textPrimary, isDark),
            color = textPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )
        }
      }
    }
  }
}

@Composable
private fun CodeBlockCard(
  code: String,
  language: String,
  isHtmlCode: Boolean,
  isDark: Boolean,
  textSecondary: Color,
  onApplyHtml: (String) -> Unit,
  onCopy: (String) -> Unit
) {
  val codeBg = if (isDark) Color(0xFF181A1F) else Color(0xFF1E293B)
  val codeTextColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFFF1F5F9)
  val borderColor = if (isDark) Color(0xFF2E333D) else Color(0xFF334155)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(codeBg)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
  ) {
    // Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(if (isDark) Color(0xFF131518) else Color(0xFF0F172A))
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Code,
          contentDescription = null,
          tint = NeuAccent,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = language.uppercase(),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = NeuAccent,
          fontFamily = FontFamily.Monospace
        )
      }

      Text(
        text = "${code.lines().size} lines",
        fontSize = 10.sp,
        color = Color(0xFF94A3B8)
      )
    }

    // Code Body with horizontal scroll
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .horizontalScroll(rememberScrollState())
    ) {
      Text(
        text = code,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = codeTextColor,
        lineHeight = 16.sp
      )
    }

    // Actions Toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (isHtmlCode) {
        NeuButton(
          onClick = { onApplyHtml(code) },
          isPrimary = true,
          cornerRadius = 10.dp,
          isDark = isDark,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
          modifier = Modifier.weight(1f).testTag("markdown_apply_html_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Open in Studio", fontSize = 11.sp)
        }
      }

      NeuButton(
        onClick = { onCopy(code) },
        isPrimary = false,
        cornerRadius = 10.dp,
        isDark = isDark,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
        modifier = Modifier.weight(1f).testTag("markdown_copy_code_button")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Copy Code", fontSize = 11.sp)
      }
    }
  }
}

/**
 * Parses bold (**text**), inline code (`code`), and italics (*text*) into an AnnotatedString.
 */
fun parseInlineMarkdown(text: String, baseColor: Color, isDark: Boolean = false): AnnotatedString {
  return buildAnnotatedString {
    val pattern = Regex("""(\*\*([^*]+)\*\*|__([^_]+)__|`([^`]+)`|\*([^*]+)\*)""")
    var lastIndex = 0

    val codeBg = if (isDark) Color(0xFF282C34) else Color(0xFFE2E8F0)
    val accentColor = Color(0xFF6366F1) // NeuAccent

    pattern.findAll(text).forEach { matchResult ->
      val start = matchResult.range.first
      val end = matchResult.range.last + 1

      if (start > lastIndex) {
        append(text.substring(lastIndex, start))
      }

      val fullMatch = matchResult.value
      when {
        fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
          val inner = fullMatch.removeSurrounding("**")
          pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor))
          append(inner)
          pop()
        }
        fullMatch.startsWith("__") && fullMatch.endsWith("__") -> {
          val inner = fullMatch.removeSurrounding("__")
          pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor))
          append(inner)
          pop()
        }
        fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
          val inner = fullMatch.removeSurrounding("`")
          pushStyle(
            SpanStyle(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold,
              background = codeBg,
              color = accentColor,
              fontSize = 12.sp
            )
          )
          append(" $inner ")
          pop()
        }
        fullMatch.startsWith("*") && fullMatch.endsWith("*") -> {
          val inner = fullMatch.removeSurrounding("*")
          pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
          append(inner)
          pop()
        }
        else -> {
          append(fullMatch)
        }
      }

      lastIndex = end
    }

    if (lastIndex < text.length) {
      append(text.substring(lastIndex))
    }
  }
}
