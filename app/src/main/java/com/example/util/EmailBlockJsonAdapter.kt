package com.example.util

import com.example.model.EmailBlock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object EmailBlockJsonAdapter {

  fun blocksToJson(blocks: List<EmailBlock>): String {
    val array = JSONArray()
    for (block in blocks) {
      val obj = JSONObject()
      obj.put("id", block.id)
      when (block) {
        is EmailBlock.Header -> {
          obj.put("type", "HEADER")
          obj.put("text", block.text)
          obj.put("subtitle", block.subtitle)
          obj.put("align", block.align)
          obj.put("color", block.color)
          obj.put("subtitleColor", block.subtitleColor)
          obj.put("fontSizeSp", block.fontSizeSp)
        }
        is EmailBlock.Text -> {
          obj.put("type", "TEXT")
          obj.put("content", block.content)
          obj.put("align", block.align)
          obj.put("color", block.color)
          obj.put("isQuote", block.isQuote)
          obj.put("fontSizeSp", block.fontSizeSp)
        }
        is EmailBlock.Button -> {
          obj.put("type", "BUTTON")
          obj.put("label", block.label)
          obj.put("url", block.url)
          obj.put("backgroundColor", block.backgroundColor)
          obj.put("textColor", block.textColor)
          obj.put("cornerRadiusDp", block.cornerRadiusDp)
          obj.put("align", block.align)
          obj.put("isFullWidth", block.isFullWidth)
          obj.put("paddingVertical", block.paddingVertical)
          obj.put("paddingHorizontal", block.paddingHorizontal)
        }
        is EmailBlock.GifImage -> {
          obj.put("type", "GIF_IMAGE")
          obj.put("imageUrl", block.imageUrl)
          obj.put("altText", block.altText)
          obj.put("caption", block.caption)
          obj.put("linkUrl", block.linkUrl)
          obj.put("align", block.align)
          obj.put("cornerRadiusDp", block.cornerRadiusDp)
          obj.put("maxWidthPercent", block.maxWidthPercent)
        }
        is EmailBlock.EmbedCard -> {
          obj.put("type", "EMBED_CARD")
          obj.put("badge", block.badge)
          obj.put("badgeColor", block.badgeColor)
          obj.put("title", block.title)
          obj.put("body", block.body)
          obj.put("linkText", block.linkText)
          obj.put("linkUrl", block.linkUrl)
          obj.put("backgroundColor", block.backgroundColor)
          obj.put("borderColor", block.borderColor)
        }
        is EmailBlock.Divider -> {
          obj.put("type", "DIVIDER")
          obj.put("color", block.color)
          obj.put("thicknessDp", block.thicknessDp)
        }
        is EmailBlock.SocialLinks -> {
          obj.put("type", "SOCIAL_LINKS")
          obj.put("websiteUrl", block.websiteUrl)
          obj.put("githubUrl", block.githubUrl)
          obj.put("twitterUrl", block.twitterUrl)
          obj.put("linkedinUrl", block.linkedinUrl)
          obj.put("align", block.align)
        }
        is EmailBlock.Footer -> {
          obj.put("type", "FOOTER")
          obj.put("signOff", block.signOff)
          obj.put("companyName", block.companyName)
          obj.put("disclaimer", block.disclaimer)
        }
      }
      array.put(obj)
    }
    return array.toString()
  }

  fun jsonToBlocks(jsonStr: String): List<EmailBlock> {
    if (jsonStr.isBlank()) return emptyList()
    val result = mutableListOf<EmailBlock>()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val id = obj.optString("id", UUID.randomUUID().toString())
        val type = obj.optString("type", "")
        val block: EmailBlock? = when (type) {
          "HEADER" -> EmailBlock.Header(
            id = id,
            text = obj.optString("text", "Header"),
            subtitle = obj.optString("subtitle", ""),
            align = obj.optString("align", "center"),
            color = obj.optString("color", "#1E293B"),
            subtitleColor = obj.optString("subtitleColor", "#64748B"),
            fontSizeSp = obj.optInt("fontSizeSp", 26)
          )
          "TEXT" -> EmailBlock.Text(
            id = id,
            content = obj.optString("content", ""),
            align = obj.optString("align", "left"),
            color = obj.optString("color", "#334155"),
            isQuote = obj.optBoolean("isQuote", false),
            fontSizeSp = obj.optInt("fontSizeSp", 16)
          )
          "BUTTON" -> EmailBlock.Button(
            id = id,
            label = obj.optString("label", "Click Here"),
            url = obj.optString("url", "https://google.com"),
            backgroundColor = obj.optString("backgroundColor", "#4F46E5"),
            textColor = obj.optString("textColor", "#FFFFFF"),
            cornerRadiusDp = obj.optInt("cornerRadiusDp", 8),
            align = obj.optString("align", "center"),
            isFullWidth = obj.optBoolean("isFullWidth", false),
            paddingVertical = obj.optInt("paddingVertical", 14),
            paddingHorizontal = obj.optInt("paddingHorizontal", 28)
          )
          "GIF_IMAGE" -> EmailBlock.GifImage(
            id = id,
            imageUrl = obj.optString("imageUrl", ""),
            altText = obj.optString("altText", ""),
            caption = obj.optString("caption", ""),
            linkUrl = obj.optString("linkUrl", ""),
            align = obj.optString("align", "center"),
            cornerRadiusDp = obj.optInt("cornerRadiusDp", 12),
            maxWidthPercent = obj.optInt("maxWidthPercent", 90)
          )
          "EMBED_CARD" -> EmailBlock.EmbedCard(
            id = id,
            badge = obj.optString("badge", "NOTICE"),
            badgeColor = obj.optString("badgeColor", "#4F46E5"),
            title = obj.optString("title", "Card Title"),
            body = obj.optString("body", ""),
            linkText = obj.optString("linkText", "Learn more →"),
            linkUrl = obj.optString("linkUrl", "https://google.com"),
            backgroundColor = obj.optString("backgroundColor", "#F8FAFC"),
            borderColor = obj.optString("borderColor", "#E2E8F0")
          )
          "DIVIDER" -> EmailBlock.Divider(
            id = id,
            color = obj.optString("color", "#E2E8F0"),
            thicknessDp = obj.optInt("thicknessDp", 1)
          )
          "SOCIAL_LINKS" -> EmailBlock.SocialLinks(
            id = id,
            websiteUrl = obj.optString("websiteUrl", "https://google.com"),
            githubUrl = obj.optString("githubUrl", "https://github.com"),
            twitterUrl = obj.optString("twitterUrl", "https://x.com"),
            linkedinUrl = obj.optString("linkedinUrl", "https://linkedin.com"),
            align = obj.optString("align", "center")
          )
          "FOOTER" -> EmailBlock.Footer(
            id = id,
            signOff = obj.optString("signOff", "Best regards,\nThe Team"),
            companyName = obj.optString("companyName", "MailCraft Studio"),
            disclaimer = obj.optString("disclaimer", "")
          )
          else -> null
        }
        if (block != null) {
          result.add(block)
        }
      }
    } catch (_: Exception) {
      // Return successfully parsed elements or empty on corruption
    }
    return result
  }
}
