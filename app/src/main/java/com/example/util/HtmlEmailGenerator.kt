package com.example.util

import com.example.model.EmailBlock
import com.example.model.EmailMessage

object HtmlEmailGenerator {

  fun evaluateVariables(
    content: String,
    senderEmail: String,
    recipientEmail: String,
    appPassword: String = "",
    senderName: String = "",
    subject: String = ""
  ): String {
    val myEmailVal = senderEmail.ifBlank { "xxx@xxx.xxx" }
    val recipientEmailVal = recipientEmail.ifBlank { "xxx@xxx.xxx" }
    val myPasswordVal = if (appPassword.isNotBlank()) appPassword else "xxxxx xxxxx xxxxx"
    val myNameVal = senderName.ifBlank { "My Name" }
    val subjectVal = subject.ifBlank { "Subject" }
    val currentDateVal = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    return content
      .replace("{{MyEmail}}", myEmailVal, ignoreCase = true)
      .replace("{{my_email}}", myEmailVal, ignoreCase = true)
      .replace("\$MyEmail", myEmailVal)
      .replace("{{RecipientEmail}}", recipientEmailVal, ignoreCase = true)
      .replace("{{recipient_email}}", recipientEmailVal, ignoreCase = true)
      .replace("\$RecipientEmail", recipientEmailVal)
      .replace("{{MyPassword}}", myPasswordVal, ignoreCase = true)
      .replace("{{my_password}}", myPasswordVal, ignoreCase = true)
      .replace("\$MyPassword", myPasswordVal)
      .replace("{{SenderName}}", myNameVal, ignoreCase = true)
      .replace("\$SenderName", myNameVal)
      .replace("{{Subject}}", subjectVal, ignoreCase = true)
      .replace("{{CurrentDate}}", currentDateVal, ignoreCase = true)
  }

  fun generateHtml(message: EmailMessage, evaluateVars: Boolean = true): String {
    if (!message.customHtml.isNullOrBlank()) {
      return if (evaluateVars) {
        evaluateVariables(
          content = message.customHtml,
          senderEmail = message.sender.email,
          recipientEmail = message.to.firstOrNull() ?: "",
          appPassword = message.sender.appPassword,
          senderName = message.sender.displayName,
          subject = message.subject
        )
      } else {
        message.customHtml
      }
    }
    val blocksHtml = message.blocks.joinToString("\n") { block ->
      renderBlockToHtml(block)
    }

    val fullHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>${escapeHtml(message.subject)}</title>
  <style>
    body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    img { -ms-interpolation-mode: bicubic; border: 0; height: auto; line-height: 100%; outline: none; text-decoration: none; }
    body { height: 100% !important; margin: 0 !important; padding: 0 !important; width: 100% !important; background-color: #F1F5F9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    @media screen and (max-width: 600px) {
      .email-container { width: 100% !important; padding: 12px !important; }
      .email-card { padding: 20px 16px !important; border-radius: 12px !important; }
      .mobile-button { width: 100% !important; display: block !important; text-align: center !important; }
    }
  </style>
</head>
<body style="margin: 0; padding: 24px 0; background-color: #F1F5F9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
  <center>
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="email-container">
      <tr>
        <td align="center" style="padding-bottom: 16px;">
          <span style="font-size: 13px; font-weight: 700; letter-spacing: 1.2px; color: #64748B; text-transform: uppercase;">
            ${escapeHtml(message.sender.displayName)}
          </span>
        </td>
      </tr>
      <tr>
        <td style="background-color: #FFFFFF; border-radius: 16px; padding: 36px 32px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06); border: 1px solid #E2E8F0;" class="email-card">
          $blocksHtml
        </td>
      </tr>
      <tr>
        <td align="center" style="padding-top: 24px; font-size: 12px; color: #94A3B8;">
          Sent securely via MailCraft Studio &bull; Gmail Delivery Engine
        </td>
      </tr>
    </table>
  </center>
</body>
""".trimIndent()
    return if (evaluateVars) {
      evaluateVariables(
        content = fullHtml,
        senderEmail = message.sender.email,
        recipientEmail = message.to.firstOrNull() ?: "",
        appPassword = message.sender.appPassword,
        senderName = message.sender.displayName,
        subject = message.subject
      )
    } else {
      fullHtml
    }
  }

  private fun renderBlockToHtml(block: EmailBlock): String {
    return when (block) {
      is EmailBlock.Header -> {
        val textAlign = block.align
        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 20px;">
          <tr>
            <td align="$textAlign">
              <h1 style="margin: 0 0 8px 0; font-size: ${block.fontSizeSp}px; font-weight: 800; color: ${block.color}; line-height: 1.25; letter-spacing: -0.5px;">
                ${escapeHtml(block.text)}
              </h1>
              ${if (block.subtitle.isNotBlank()) """
              <p style="margin: 0; font-size: 15px; color: ${block.subtitleColor}; line-height: 1.5;">
                ${escapeHtml(block.subtitle)}
              </p>
              """ else ""}
            </td>
          </tr>
        </table>
        """.trimIndent()
      }

      is EmailBlock.Text -> {
        val textAlign = block.align
        if (block.isQuote) {
          """
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 20px;">
            <tr>
              <td style="border-left: 4px solid #6366F1; background-color: #F8FAFC; padding: 14px 18px; border-radius: 0 8px 8px 0; font-style: italic; color: ${block.color}; font-size: ${block.fontSizeSp}px; line-height: 1.6; text-align: $textAlign;">
                “${escapeHtml(block.content).replace("\n", "<br>")}”
              </td>
            </tr>
          </table>
          """.trimIndent()
        } else {
          """
          <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 18px;">
            <tr>
              <td align="$textAlign" style="font-size: ${block.fontSizeSp}px; color: ${block.color}; line-height: 1.65;">
                ${escapeHtml(block.content).replace("\n", "<br>")}
              </td>
            </tr>
          </table>
          """.trimIndent()
        }
      }

      is EmailBlock.Button -> {
        val align = block.align
        val widthAttr = if (block.isFullWidth) "width=\"100%\"" else ""
        val tableAlign = when (align) {
          "left" -> "left"
          "right" -> "right"
          else -> "center"
        }
        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 22px 0;">
          <tr>
            <td align="$tableAlign">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" $widthAttr>
                <tr>
                  <td align="center" bgcolor="${block.backgroundColor}" style="border-radius: ${block.cornerRadiusDp}px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);">
                    <a href="${escapeHtml(block.url)}" target="_blank" style="display: inline-block; padding: ${block.paddingVertical}px ${block.paddingHorizontal}px; font-size: 15px; font-weight: 700; color: ${block.textColor}; text-decoration: none; border-radius: ${block.cornerRadiusDp}px; letter-spacing: 0.2px;">
                      ${escapeHtml(block.label)}
                    </a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        """.trimIndent()
      }

      is EmailBlock.GifImage -> {
        val align = block.align
        val tableAlign = when (align) {
          "left" -> "left"
          "right" -> "right"
          else -> "center"
        }
        val imgTag = """
        <img src="${escapeHtml(block.imageUrl)}" alt="${escapeHtml(block.altText)}" style="width: 100%; max-width: ${block.maxWidthPercent}%; border-radius: ${block.cornerRadiusDp}px; display: block; border: 0; outline: none; margin: 0 auto; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);" />
        """.trimIndent()

        val contentWithOptionalLink = if (block.linkUrl.isNotBlank()) {
          """<a href="${escapeHtml(block.linkUrl)}" target="_blank" style="text-decoration: none; display: inline-block;">$imgTag</a>"""
        } else {
          imgTag
        }

        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 20px 0;">
          <tr>
            <td align="$tableAlign">
              $contentWithOptionalLink
              ${if (block.caption.isNotBlank()) """
              <p style="margin: 8px 0 0 0; font-size: 13px; color: #64748B; font-style: italic; text-align: $tableAlign;">
                ${escapeHtml(block.caption)}
              </p>
              """ else ""}
            </td>
          </tr>
        </table>
        """.trimIndent()
      }

      is EmailBlock.EmbedCard -> {
        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 20px 0; background-color: ${block.backgroundColor}; border: 1px solid ${block.borderColor}; border-radius: 12px; overflow: hidden;">
          <tr>
            <td style="padding: 20px 22px;">
              ${if (block.badge.isNotBlank()) """
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 10px;">
                <tr>
                  <td bgcolor="${block.badgeColor}" style="border-radius: 4px; padding: 3px 8px; font-size: 11px; font-weight: 800; color: #FFFFFF; text-transform: uppercase; letter-spacing: 0.8px;">
                    ${escapeHtml(block.badge)}
                  </td>
                </tr>
              </table>
              """ else ""}
              <h3 style="margin: 0 0 8px 0; font-size: 17px; font-weight: 700; color: #0F172A;">
                ${escapeHtml(block.title)}
              </h3>
              <p style="margin: 0 0 ${if (block.linkUrl.isNotBlank()) "12px" else "0"} 0; font-size: 14px; color: #475569; line-height: 1.55;">
                ${escapeHtml(block.body).replace("\n", "<br>")}
              </p>
              ${if (block.linkUrl.isNotBlank()) """
              <a href="${escapeHtml(block.linkUrl)}" target="_blank" style="font-size: 13px; font-weight: 700; color: ${block.badgeColor}; text-decoration: none;">
                ${escapeHtml(block.linkText)}
              </a>
              """ else ""}
            </td>
          </tr>
        </table>
        """.trimIndent()
      }

      is EmailBlock.Divider -> {
        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 24px 0;">
          <tr>
            <td style="border-top: ${block.thicknessDp}px ${block.style} ${block.color}; font-size: 1px; line-height: 1px;">&nbsp;</td>
          </tr>
        </table>
        """.trimIndent()
      }

      is EmailBlock.SocialLinks -> {
        val align = block.align
        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 20px 0;">
          <tr>
            <td align="$align">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td style="padding: 0 8px;">
                    <a href="${escapeHtml(block.websiteUrl)}" target="_blank" style="color: #64748B; font-size: 13px; font-weight: 600; text-decoration: none;">🌐 Website</a>
                  </td>
                  <td style="padding: 0 8px;">
                    <a href="${escapeHtml(block.githubUrl)}" target="_blank" style="color: #64748B; font-size: 13px; font-weight: 600; text-decoration: none;">💻 GitHub</a>
                  </td>
                  <td style="padding: 0 8px;">
                    <a href="${escapeHtml(block.twitterUrl)}" target="_blank" style="color: #64748B; font-size: 13px; font-weight: 600; text-decoration: none;">🐦 Twitter</a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        """.trimIndent()
      }

      is EmailBlock.Footer -> {
        """
        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-top: 24px; border-top: 1px solid #E2E8F0; padding-top: 18px;">
          <tr>
            <td>
              <p style="margin: 0 0 10px 0; font-size: 14px; font-weight: 600; color: #334155; line-height: 1.5;">
                ${escapeHtml(block.signOff).replace("\n", "<br>")}
              </p>
              <p style="margin: 0 0 6px 0; font-size: 12px; font-weight: 700; color: #64748B;">
                ${escapeHtml(block.companyName)}
              </p>
              ${if (block.disclaimer.isNotBlank()) """
              <p style="margin: 0; font-size: 11px; color: #94A3B8; line-height: 1.4;">
                ${escapeHtml(block.disclaimer)}
              </p>
              """ else ""}
            </td>
          </tr>
        </table>
        """.trimIndent()
      }
    }
  }

  fun generatePlainText(message: EmailMessage): String {
    val sb = StringBuilder()
    sb.appendLine("Subject: ${message.subject}")
    sb.appendLine("From: ${message.sender.displayName} <${message.sender.email}>")
    sb.appendLine("To: ${message.to.joinToString(", ")}")
    sb.appendLine("=".repeat(40))
    sb.appendLine()

    for (block in message.blocks) {
      when (block) {
        is EmailBlock.Header -> {
          sb.appendLine(block.text)
          if (block.subtitle.isNotBlank()) sb.appendLine(block.subtitle)
          sb.appendLine()
        }
        is EmailBlock.Text -> {
          if (block.isQuote) sb.appendLine("> ${block.content}") else sb.appendLine(block.content)
          sb.appendLine()
        }
        is EmailBlock.Button -> {
          sb.appendLine("[ ${block.label} ]: ${block.url}")
          sb.appendLine()
        }
        is EmailBlock.GifImage -> {
          sb.appendLine("[ Image / GIF: ${block.altText} ] (${block.imageUrl})")
          if (block.caption.isNotBlank()) sb.appendLine(block.caption)
          sb.appendLine()
        }
        is EmailBlock.EmbedCard -> {
          sb.appendLine("--- ${block.badge}: ${block.title} ---")
          sb.appendLine(block.body)
          if (block.linkUrl.isNotBlank()) sb.appendLine("Link: ${block.linkUrl}")
          sb.appendLine()
        }
        is EmailBlock.Divider -> {
          sb.appendLine("-".repeat(30))
          sb.appendLine()
        }
        is EmailBlock.SocialLinks -> {
          sb.appendLine("Website: ${block.websiteUrl} | GitHub: ${block.githubUrl}")
          sb.appendLine()
        }
        is EmailBlock.Footer -> {
          sb.appendLine(block.signOff)
          sb.appendLine(block.companyName)
          if (block.disclaimer.isNotBlank()) sb.appendLine(block.disclaimer)
          sb.appendLine()
        }
      }
    }
    return sb.toString()
  }

  fun generateNodemailerScript(message: EmailMessage): String {
    val html = generateHtml(message)
    val escapedHtml = html.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
    val toList = message.to.joinToString("', '")

    return """
// Node.js script using Nodemailer with Gmail SMTP and App Password
// npm install nodemailer

const nodemailer = require('nodemailer');

async function sendMail() {
  // 1. Create reusable transporter object using Gmail SMTP
  const transporter = nodemailer.createTransport({
    service: 'gmail', // or host: 'smtp.gmail.com', port: 465, secure: true
    auth: {
      user: process.env.GMAIL_USER || '${message.sender.email.ifBlank { "your.email@gmail.com" }}',
      // 'pass' is the 16-character Google App Password (not your standard password)
      // Generated at https://myaccount.google.com/apppasswords
      pass: process.env.GMAIL_APP_PASSWORD || '${message.sender.appPassword.ifBlank { "your-16-char-app-password" }}',
    },
  });

  // 2. Define mail options
  const mailOptions = {
    from: '"${message.sender.displayName.ifBlank { "My Name" }}" <${message.sender.email.ifBlank { "your.email@gmail.com" }}>',
    to: ['$toList'],
    subject: '${message.subject.replace("'", "\\'")}',
    text: `${generatePlainText(message).replace("`", "\\`").replace("$", "\\$")}`,
    html: `$escapedHtml`,
  };

  try {
    console.log('Connecting to Gmail SMTP server...');
    const info = await transporter.sendMail(mailOptions);
    console.log('✅ Email sent successfully!');
    console.log('Message ID:', info.messageId);
    console.log('Response:', info.response);
  } catch (error) {
    console.error('❌ Failed to send email via Gmail:', error);
  }
}

sendMail();
""".trimIndent()
  }

  fun formatHtml(html: String): String {
    val singleLineTags = setOf("img", "br", "hr", "meta", "input", "!doctype", "link", "!DOCTYPE")
    val lines = html.replace("><", ">\n<").lines()
    val sb = StringBuilder()
    var indent = 0
    for (rawLine in lines) {
      val trimmed = rawLine.trim()
      if (trimmed.isEmpty()) continue
      if (trimmed.startsWith("</")) {
        indent = (indent - 1).coerceAtLeast(0)
      }
      sb.append("  ".repeat(indent)).append(trimmed).append("\n")
      val tagMatch = Regex("^<([a-zA-Z0-9!]+)").find(trimmed)
      val tagName = tagMatch?.groupValues?.get(1)?.lowercase() ?: ""
      if (trimmed.startsWith("<") && !trimmed.startsWith("</") && !trimmed.endsWith("/>") && !singleLineTags.contains(tagName) && !trimmed.contains("</")) {
        indent++
      }
    }
    return sb.toString().trimEnd()
  }

  private fun escapeHtml(text: String): String {
    return text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;")
  }
}
