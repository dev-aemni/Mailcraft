package com.example.model

enum class BlockType {
  HEADER,
  TEXT,
  BUTTON,
  GIF_IMAGE,
  EMBED_CARD,
  DIVIDER,
  SOCIAL_LINKS,
  FOOTER
}

sealed class EmailBlock(val type: BlockType) {
  abstract val id: String

  data class Header(
    override val id: String,
    val text: String = "Hello There!",
    val subtitle: String = "We have an exciting update for you",
    val align: String = "center", // left, center, right
    val color: String = "#1E293B",
    val subtitleColor: String = "#64748B",
    val fontSizeSp: Int = 26
  ) : EmailBlock(BlockType.HEADER)

  data class Text(
    override val id: String,
    val content: String = "Thank you for being part of our community. Here is what we've been working on recently.",
    val align: String = "left", // left, center, right
    val color: String = "#334155",
    val isQuote: Boolean = false,
    val fontSizeSp: Int = 16
  ) : EmailBlock(BlockType.TEXT)

  data class Button(
    override val id: String,
    val label: String = "Open Dashboard",
    val url: String = "https://google.com",
    val backgroundColor: String = "#4F46E5", // Indigo
    val textColor: String = "#FFFFFF",
    val cornerRadiusDp: Int = 8,
    val align: String = "center", // left, center, right
    val isFullWidth: Boolean = false,
    val paddingVertical: Int = 14,
    val paddingHorizontal: Int = 28
  ) : EmailBlock(BlockType.BUTTON)

  data class GifImage(
    override val id: String,
    val imageUrl: String = "https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif",
    val altText: String = "Celebration GIF",
    val caption: String = "Exciting times ahead!",
    val linkUrl: String = "",
    val align: String = "center",
    val cornerRadiusDp: Int = 12,
    val maxWidthPercent: Int = 90
  ) : EmailBlock(BlockType.GIF_IMAGE)

  data class EmbedCard(
    override val id: String,
    val badge: String = "HIGHLIGHT",
    val badgeColor: String = "#4F46E5",
    val title: String = "Special Feature Embed",
    val body: String = "This is a custom responsive embedding card with border highlights, typography, and optional action link.",
    val linkText: String = "Learn more →",
    val linkUrl: String = "https://google.com",
    val backgroundColor: String = "#F8FAFC",
    val borderColor: String = "#E2E8F0"
  ) : EmailBlock(BlockType.EMBED_CARD)

  data class Divider(
    override val id: String,
    val color: String = "#E2E8F0",
    val thicknessDp: Int = 1,
    val style: String = "solid" // solid, dashed, dotted
  ) : EmailBlock(BlockType.DIVIDER)

  data class SocialLinks(
    override val id: String,
    val websiteUrl: String = "https://google.com",
    val githubUrl: String = "https://github.com",
    val twitterUrl: String = "https://x.com",
    val linkedinUrl: String = "https://linkedin.com",
    val align: String = "center"
  ) : EmailBlock(BlockType.SOCIAL_LINKS)

  data class Footer(
    override val id: String,
    val signOff: String = "Best regards,\nThe Team",
    val companyName: String = "MailCraft Studio",
    val disclaimer: String = "You received this email because you are registered. If you did not intend to receive this, feel free to ignore."
  ) : EmailBlock(BlockType.FOOTER)
}

data class EmailAttachment(
  val id: String = java.util.UUID.randomUUID().toString(),
  val uriString: String,
  val fileName: String,
  val mimeType: String,
  val sizeBytes: Long
) {
  val formattedSize: String
    get() = when {
      sizeBytes < 1024 -> "$sizeBytes B"
      sizeBytes < 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f KB", sizeBytes / 1024.0)
      else -> String.format(java.util.Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
    }

  val isPdf: Boolean
    get() = mimeType.contains("pdf", ignoreCase = true) || fileName.endsWith(".pdf", ignoreCase = true)

  val isImage: Boolean
    get() = mimeType.startsWith("image/", ignoreCase = true) ||
            listOf(".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp").any { fileName.endsWith(it, ignoreCase = true) }
}

enum class ClientDarkMode(val label: String, val description: String) {
  OFF("Standard Light", "Default light client rendering"),
  GMAIL("Gmail Dark", "Selective dark inversion (preserves brand buttons)"),
  OUTLOOK("Outlook Dark", "Aggressive color inversion with dark containers"),
  APPLE_MAIL("Apple Mail Dark", "Full CSS @media (prefers-color-scheme) dark styling")
}

data class SenderAccount(
  val email: String = "",
  val displayName: String = "",
  val appPassword: String = "",
  val smtpHost: String = "smtp.gmail.com",
  val smtpPort: Int = 465,
  val useSsl: Boolean = true,
  val avatarPreset: String = "avatar_purple_orb",
  val avatarUrl: String = ""
)

data class EmailMessage(
  val sender: SenderAccount,
  val to: List<String>,
  val cc: List<String> = emptyList(),
  val bcc: List<String> = emptyList(),
  val subject: String,
  val blocks: List<EmailBlock>,
  val customHtml: String? = null,
  val themePreset: String = "Modern Indigo",
  val attachments: List<EmailAttachment> = emptyList()
)

data class SmtpLog(
  val timestamp: Long = System.currentTimeMillis(),
  val message: String,
  val isError: Boolean = false
)

data class SmtpSendResult(
  val success: Boolean,
  val message: String,
  val logs: List<SmtpLog> = emptyList(),
  val messageId: String? = null
)

