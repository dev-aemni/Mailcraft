package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

// Rich Light Palette (Editor default)
val LightColorHtmlTag = Color(0xFF4F46E5)         // Deep Indigo bracket
val LightColorHtmlTagName = Color(0xFF0284C7)     // Ocean Blue tag name
val LightColorHtmlSpecialTag = Color(0xFF7C3AED)  // Royal Purple tag name for structural tags
val LightColorHtmlAttr = Color(0xFF6D28D9)        // Purple attribute name
val LightColorHtmlString = Color(0xFF059669)      // Emerald Green string
val LightColorHtmlComment = Color(0xFF64748B)     // Slate Gray comment
val LightColorHtmlDoctype = Color(0xFF9333EA)     // Deep Violet doctype
val LightColorHtmlEntity = Color(0xFFD97706)      // Amber entity
val LightColorHtmlText = Color(0xFF1E293B)        // Slate text
val LightColorHtmlVar = Color(0xFFC026D3)         // Fuchsia / Magenta for dynamic {{Variables}}
val LightColorCssProp = Color(0xFF0369A1)         // Sky Blue for CSS properties
val LightColorCssValue = Color(0xFFB45309)        // Amber for CSS values
val LightColorHex = Color(0xFFE11D48)             // Rose for #HEX color codes
val LightColorNumber = Color(0xFF2563EB)          // Blue for units & numbers (14px, 100%)

// Rich Dark Palette
val DarkColorHtmlTag = Color(0xFF38BDF8)
val DarkColorHtmlTagName = Color(0xFF4EC9B0)
val DarkColorHtmlSpecialTag = Color(0xFFA78BFA)
val DarkColorHtmlAttr = Color(0xFFC084FC)
val DarkColorHtmlString = Color(0xFF34D399)
val DarkColorHtmlComment = Color(0xFF94A3B8)
val DarkColorHtmlDoctype = Color(0xFFF472B6)
val DarkColorHtmlEntity = Color(0xFFFBBF24)
val DarkColorHtmlText = Color(0xFFF1F5F9)
val DarkColorHtmlVar = Color(0xFFF472B6)
val DarkColorCssProp = Color(0xFF38BDF8)
val DarkColorCssValue = Color(0xFFFCD34D)
val DarkColorHex = Color(0xFFFB7185)
val DarkColorNumber = Color(0xFF60A5FA)

fun highlightHtmlSyntax(raw: String, isDark: Boolean = false): AnnotatedString {
  if (raw.isEmpty()) return AnnotatedString("")

  val cTag = if (isDark) DarkColorHtmlTag else LightColorHtmlTag
  val cTagName = if (isDark) DarkColorHtmlTagName else LightColorHtmlTagName
  val cSpecialTag = if (isDark) DarkColorHtmlSpecialTag else LightColorHtmlSpecialTag
  val cAttr = if (isDark) DarkColorHtmlAttr else LightColorHtmlAttr
  val cString = if (isDark) DarkColorHtmlString else LightColorHtmlString
  val cComment = if (isDark) DarkColorHtmlComment else LightColorHtmlComment
  val cDoctype = if (isDark) DarkColorHtmlDoctype else LightColorHtmlDoctype
  val cEntity = if (isDark) DarkColorHtmlEntity else LightColorHtmlEntity
  val cText = if (isDark) DarkColorHtmlText else LightColorHtmlText
  val cVar = if (isDark) DarkColorHtmlVar else LightColorHtmlVar
  val cCssProp = if (isDark) DarkColorCssProp else LightColorCssProp
  val cHex = if (isDark) DarkColorHex else LightColorHex
  val cNumber = if (isDark) DarkColorNumber else LightColorNumber

  val specialTags = setOf(
    "html", "head", "body", "table", "tbody", "tr", "td", "th",
    "style", "script", "meta", "title", "link", "center"
  )

  return buildAnnotatedString {
    append(raw)
    // 0. Base text color
    addStyle(SpanStyle(color = cText), 0, raw.length)

    try {
      // 1. Comments: <!-- ... --> (including Outlook conditionals <!--[if ...]> ... <![endif]-->)
      val commentPattern = Pattern.compile("<!--[\\s\\S]*?-->")
      val commentMatcher = commentPattern.matcher(raw)
      while (commentMatcher.find()) {
        addStyle(
          SpanStyle(color = cComment, fontStyle = FontStyle.Italic),
          commentMatcher.start(),
          commentMatcher.end()
        )
      }

      // 2. Doctype & XML directives: <!DOCTYPE ...> or <?xml ...?>
      val doctypePattern = Pattern.compile("(<!DOCTYPE[\\s\\S]*?>|<\\?[\\s\\S]*?\\?>)", Pattern.CASE_INSENSITIVE)
      val doctypeMatcher = doctypePattern.matcher(raw)
      while (doctypeMatcher.find()) {
        addStyle(
          SpanStyle(color = cDoctype, fontWeight = FontWeight.Bold),
          doctypeMatcher.start(),
          doctypeMatcher.end()
        )
      }

      // 3. HTML Tags & Attributes
      val tagPattern = Pattern.compile("<(/?)([a-zA-Z0-9\\-]+)([^>]*)>")
      val tagMatcher = tagPattern.matcher(raw)
      while (tagMatcher.find()) {
        val start = tagMatcher.start()
        val end = tagMatcher.end()

        // Opening bracket < or </
        addStyle(SpanStyle(color = cTag, fontWeight = FontWeight.Medium), start, start + 1)
        if (tagMatcher.group(1).isNotEmpty()) {
          addStyle(SpanStyle(color = cTag, fontWeight = FontWeight.Medium), start + 1, start + 2)
        }
        // Closing bracket >
        addStyle(SpanStyle(color = cTag, fontWeight = FontWeight.Medium), end - 1, end)

        // Tag name
        val tagName = tagMatcher.group(2)
        val nameStart = start + 1 + tagMatcher.group(1).length
        val nameEnd = nameStart + tagName.length
        val isSpecial = specialTags.contains(tagName.lowercase())
        addStyle(
          SpanStyle(
            color = if (isSpecial) cSpecialTag else cTagName,
            fontWeight = FontWeight.Bold
          ),
          nameStart,
          nameEnd
        )

        // Tag attributes
        val attrsGroup = tagMatcher.group(3)
        if (attrsGroup.isNotEmpty()) {
          val attrOffset = nameEnd
          val attrPattern = Pattern.compile("([a-zA-Z0-9\\-_:]+)\\s*(=)?\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)?")
          val attrMatcher = attrPattern.matcher(attrsGroup)
          while (attrMatcher.find()) {
            val aName = attrMatcher.group(1)
            val aStart = attrOffset + attrMatcher.start(1)
            val aEnd = aStart + aName.length
            addStyle(SpanStyle(color = cAttr, fontWeight = FontWeight.Medium), aStart, aEnd)

            val aVal = attrMatcher.group(3)
            if (aVal != null) {
              val vStart = attrOffset + attrMatcher.start(3)
              val vEnd = vStart + aVal.length
              // Base string color
              addStyle(SpanStyle(color = cString), vStart, vEnd)

              // If this is an inline style="..." attribute, apply rich CSS micro-syntax highlighting!
              if (aName.equals("style", ignoreCase = true) && aVal.length >= 2) {
                highlightInlineCss(this, aVal, vStart, cCssProp, cHex, cNumber)
              }
            }
          }
        }
      }

      // 4. CSS Syntax inside <style> ... </style> blocks
      val styleBlockPattern = Pattern.compile("<style[^>]*>([\\s\\S]*?)</style>", Pattern.CASE_INSENSITIVE)
      val styleBlockMatcher = styleBlockPattern.matcher(raw)
      while (styleBlockMatcher.find()) {
        val cssContent = styleBlockMatcher.group(1)
        val cssStart = styleBlockMatcher.start(1)
        highlightCssBlock(this, cssContent, cssStart, cCssProp, cHex, cNumber, cComment)
      }

      // 5. Dynamic Template Variables: {{Variable}} or {{...}}
      val varPattern = Pattern.compile("\\{\\{[a-zA-Z0-9_\\-\\s]+\\}\\}")
      val varMatcher = varPattern.matcher(raw)
      while (varMatcher.find()) {
        addStyle(
          SpanStyle(
            color = cVar,
            fontWeight = FontWeight.ExtraBold
          ),
          varMatcher.start(),
          varMatcher.end()
        )
      }

      // 6. HTML Entities: &nbsp;, &amp;, &#123;, etc.
      val entityPattern = Pattern.compile("&[a-zA-Z0-9#]+;")
      val entityMatcher = entityPattern.matcher(raw)
      while (entityMatcher.find()) {
        addStyle(
          SpanStyle(color = cEntity, fontWeight = FontWeight.Bold),
          entityMatcher.start(),
          entityMatcher.end()
        )
      }

    } catch (_: Exception) {
      // Graceful fallback: text remains displayed without crash
    }
  }
}

private fun highlightInlineCss(
  builder: AnnotatedString.Builder,
  attrValue: String,
  offset: Int,
  cCssProp: Color,
  cHex: Color,
  cNumber: Color
) {
  try {
    // 1. CSS property names: e.g. "padding:", "background-color:", "font-size:"
    val propPattern = Pattern.compile("([a-zA-Z\\-]+)\\s*:")
    val propMatcher = propPattern.matcher(attrValue)
    while (propMatcher.find()) {
      val pStart = offset + propMatcher.start(1)
      val pEnd = pStart + propMatcher.group(1).length
      builder.addStyle(SpanStyle(color = cCssProp, fontWeight = FontWeight.SemiBold), pStart, pEnd)
    }

    // 2. Hex colors: #ffffff, #10b981
    val hexPattern = Pattern.compile("#([0-9a-fA-F]{3,8})\\b")
    val hexMatcher = hexPattern.matcher(attrValue)
    while (hexMatcher.find()) {
      val hStart = offset + hexMatcher.start()
      val hEnd = offset + hexMatcher.end()
      builder.addStyle(SpanStyle(color = cHex, fontWeight = FontWeight.Bold), hStart, hEnd)
    }

    // 3. Numbers & Units: 14px, 100%, 20pt, 1.5em
    val numPattern = Pattern.compile("\\b(\\d+(?:\\.\\d+)?)(px|pt|em|rem|%|vw|vh)?\\b")
    val numMatcher = numPattern.matcher(attrValue)
    while (numMatcher.find()) {
      val nStart = offset + numMatcher.start()
      val nEnd = offset + numMatcher.end()
      builder.addStyle(SpanStyle(color = cNumber), nStart, nEnd)
    }
  } catch (_: Exception) {}
}

private fun highlightCssBlock(
  builder: AnnotatedString.Builder,
  cssContent: String,
  offset: Int,
  cCssProp: Color,
  cHex: Color,
  cNumber: Color,
  cComment: Color
) {
  try {
    // Comments /* ... */
    val cssCommentPattern = Pattern.compile("/\\*[\\s\\S]*?\\*/")
    val cssCommentMatcher = cssCommentPattern.matcher(cssContent)
    while (cssCommentMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = cComment, fontStyle = FontStyle.Italic),
        offset + cssCommentMatcher.start(),
        offset + cssCommentMatcher.end()
      )
    }

    // Properties: display, color, background-color, etc.
    val propPattern = Pattern.compile("([a-zA-Z\\-_]+)\\s*:")
    val propMatcher = propPattern.matcher(cssContent)
    while (propMatcher.find()) {
      val pStart = offset + propMatcher.start(1)
      val pEnd = pStart + propMatcher.group(1).length
      builder.addStyle(SpanStyle(color = cCssProp, fontWeight = FontWeight.SemiBold), pStart, pEnd)
    }

    // Hex colors
    val hexPattern = Pattern.compile("#([0-9a-fA-F]{3,8})\\b")
    val hexMatcher = hexPattern.matcher(cssContent)
    while (hexMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = cHex, fontWeight = FontWeight.Bold),
        offset + hexMatcher.start(),
        offset + hexMatcher.end()
      )
    }

    // Numbers & units
    val numPattern = Pattern.compile("\\b(\\d+(?:\\.\\d+)?)(px|pt|em|rem|%|vw|vh)?\\b")
    val numMatcher = numPattern.matcher(cssContent)
    while (numMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = cNumber),
        offset + numMatcher.start(),
        offset + numMatcher.end()
      )
    }
  } catch (_: Exception) {}
}

class HtmlSyntaxVisualTransformation(val isDark: Boolean = false) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    return TransformedText(highlightHtmlSyntax(text.text, isDark), OffsetMapping.Identity)
  }
}
