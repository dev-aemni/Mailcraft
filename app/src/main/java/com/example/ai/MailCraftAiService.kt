package com.example.ai

import android.util.Log
import com.example.model.EmailBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class MailCraftAiService(
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(35, TimeUnit.SECONDS)
    .readTimeout(50, TimeUnit.SECONDS)
    .writeTimeout(35, TimeUnit.SECONDS)
    .build()
) {
  companion object {
    private const val TAG = "MailCraftAiService"
    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"
    const val DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free"

    private const val SUPPORT_SYSTEM_PROMPT = """
You are MailCraft AI, the intelligent, built-in support assistant and email design expert for MailCraft Studio.
Your goals:
1. Help users design beautiful, high-converting, responsive emails.
2. Provide bulletproof HTML email code that works perfectly on Gmail, Apple Mail, and Outlook (tables with inline styles, 600px max width, mobile responsiveness).
3. Assist with Gmail SMTP configuration, including:
   - Setting up a 16-character Google App Password (via Google Account > Security > 2-Step Verification > App passwords).
   - Explaining why normal Google passwords fail on SMTP.
   - Diagnosing authentication (535) or connection errors.
4. Provide deliverability best practices: avoid spam trigger words, configure SPF/DKIM, keep attachments under 25MB, and maintain a high text-to-image ratio.
5. Provide actionable email copy, catchy subject lines, and clear Call-to-Action (CTA) strategies.

Communication style:
- Be clear, practical, inspiring, and concise.
- Format code blocks using ```html for HTML emails, or ```json when providing structured data.
- Never mention internal API gateways, proxies, or router names. You are simply "MailCraft AI".
"""
  }

  /**
   * Executes a chat completion request with automatic, silent fallback across free models.
   * If a selected model returns a 404, rate limit, or outage, the request immediately
   * and invisibly shifts to the next candidate in FREE_MODELS_FALLBACK_LIST.
   */
  private suspend fun executeChatWithFallback(
    messages: JSONArray,
    preferredModel: String,
    temperature: Double = 0.7
  ): Result<String> = withContext(Dispatchers.IO) {
    val apiKey = AiKeyProvider.getApiKey()
    if (apiKey.isBlank()) {
      return@withContext Result.failure(
        IllegalStateException("AI API key is missing. Please configure 'openrouter_api' in AI Studio Secrets.")
      )
    }

    // Prepare sequence of candidate models starting with a verified free model
    val candidates = buildList {
      val validPreferred = if (preferredModel.endsWith(":free") && !preferredModel.contains("claude", ignoreCase = true)) {
        preferredModel
      } else {
        DEFAULT_MODEL
      }
      add(validPreferred)
      FREE_MODELS_FALLBACK_LIST.forEach { fallback ->
        if (!contains(fallback)) add(fallback)
      }
    }

    var lastErrorMessage = "AI service is currently busy. Please try again shortly."

    for (model in candidates) {
      try {
        val payload = JSONObject().apply {
          put("model", model)
          put("messages", messages)
          put("temperature", temperature)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payload.toString().toRequestBody(mediaType)

        val request = Request.Builder()
          .url(API_URL)
          .addHeader("Authorization", "Bearer $apiKey")
          .addHeader("Content-Type", "application/json")
          .addHeader("HTTP-Referer", "https://mailcraft.studio")
          .addHeader("X-Title", "MailCraft")
          .post(body)
          .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (response.isSuccessful) {
          val json = JSONObject(responseBody)
          val choices = json.optJSONArray("choices")
          if (choices != null && choices.length() > 0) {
            val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content").orEmpty()
            if (content.isNotBlank()) {
              Log.d(TAG, "AI answered successfully using free model: $model")
              return@withContext Result.success(content)
            }
          }
        } else {
          // Model error (e.g. 404 model not found, 402, 429 rate limit, 503)
          // Silently proceed to next free candidate without showing error to user!
          Log.w(TAG, "Model $model returned HTTP ${response.code}. Silently switching to next engine...")
          lastErrorMessage = sanitizeError(responseBody, response.code)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Network exception with model $model: ${e.message}. Silently trying next engine...")
        lastErrorMessage = "Network connection timed out. Please check your internet connection."
      }
    }

    Result.failure(Exception(lastErrorMessage))
  }

  suspend fun chat(
    conversation: List<AiChatMessage>,
    model: String = DEFAULT_MODEL,
    systemPrompt: String = SUPPORT_SYSTEM_PROMPT
  ): Result<String> {
    val messagesArray = JSONArray()

    if (systemPrompt.isNotBlank()) {
      val sysObj = JSONObject().apply {
        put("role", "system")
        put("content", systemPrompt)
      }
      messagesArray.put(sysObj)
    }

    conversation.forEach { msg ->
      val mObj = JSONObject().apply {
        put("role", msg.role)
        put("content", msg.content)
      }
      messagesArray.put(mObj)
    }

    return executeChatWithFallback(
      messages = messagesArray,
      preferredModel = model,
      temperature = 0.7
    )
  }

  suspend fun generateVisualEmail(
    prompt: String,
    tone: String = "Professional & Engaging",
    model: String = DEFAULT_MODEL
  ): Result<GeneratedEmail> = withContext(Dispatchers.IO) {
    val systemPrompt = """
You are an expert visual email architect.
The user wants to generate an email. Return ONLY a valid JSON object without markdown formatting, code ticks, or extra explanation.
The JSON must follow this exact schema:
{
  "subject": "Email Subject Line",
  "previewSnippet": "Short 1-line snippet",
  "blocks": [
    {
      "type": "HEADER",
      "text": "Main Headline",
      "subtitle": "Secondary subtitle",
      "align": "center",
      "color": "#1E293B",
      "subtitleColor": "#64748B",
      "fontSizeSp": 26
    },
    {
      "type": "TEXT",
      "content": "Paragraph body text...",
      "align": "left",
      "color": "#334155",
      "fontSizeSp": 16
    },
    {
      "type": "BUTTON",
      "label": "Primary Call To Action",
      "url": "https://example.com",
      "backgroundColor": "#4F46E5",
      "textColor": "#FFFFFF",
      "cornerRadiusDp": 8,
      "align": "center"
    },
    {
      "type": "DIVIDER"
    },
    {
      "type": "FOOTER",
      "signOff": "Best regards,\nThe Team",
      "companyName": "MailCraft Studio",
      "disclaimer": "You received this because of your account."
    }
  ]
}
Supported types: HEADER, TEXT, BUTTON, EMBED_CARD, DIVIDER, FOOTER.
"""
    val userMsg = "Generate an email for: $prompt\nTone: $tone\nReturn pure raw JSON only."

    val messagesArray = JSONArray().apply {
      put(JSONObject().apply {
        put("role", "system")
        put("content", systemPrompt)
      })
      put(JSONObject().apply {
        put("role", "user")
        put("content", userMsg)
      })
    }

    val res = executeChatWithFallback(
      messages = messagesArray,
      preferredModel = model,
      temperature = 0.6
    )

    res.fold(
      onSuccess = { rawContent ->
        try {
          val cleanJson = cleanJsonString(rawContent)
          val json = JSONObject(cleanJson)
          val subject = json.optString("subject", "New Email")
          val preview = json.optString("previewSnippet", "")

          val blocksArray = json.optJSONArray("blocks") ?: JSONArray()
          val parsedBlocks = mutableListOf<EmailBlock>()

          for (i in 0 until blocksArray.length()) {
            val b = blocksArray.getJSONObject(i)
            val type = b.optString("type", "").uppercase()
            val id = UUID.randomUUID().toString()
            when (type) {
              "HEADER" -> parsedBlocks.add(
                EmailBlock.Header(
                  id = id,
                  text = b.optString("text", "Hello!"),
                  subtitle = b.optString("subtitle", ""),
                  align = b.optString("align", "center"),
                  color = b.optString("color", "#1E293B"),
                  subtitleColor = b.optString("subtitleColor", "#64748B"),
                  fontSizeSp = b.optInt("fontSizeSp", 26)
                )
              )
              "TEXT" -> parsedBlocks.add(
                EmailBlock.Text(
                  id = id,
                  content = b.optString("content", ""),
                  align = b.optString("align", "left"),
                  color = b.optString("color", "#334155"),
                  isQuote = b.optBoolean("isQuote", false),
                  fontSizeSp = b.optInt("fontSizeSp", 16)
                )
              )
              "BUTTON" -> parsedBlocks.add(
                EmailBlock.Button(
                  id = id,
                  label = b.optString("label", "Take Action"),
                  url = b.optString("url", "https://example.com"),
                  backgroundColor = b.optString("backgroundColor", "#4F46E5"),
                  textColor = b.optString("textColor", "#FFFFFF"),
                  cornerRadiusDp = b.optInt("cornerRadiusDp", 8),
                  align = b.optString("align", "center")
                )
              )
              "EMBED_CARD" -> parsedBlocks.add(
                EmailBlock.EmbedCard(
                  id = id,
                  badge = b.optString("badge", "NOTICE"),
                  badgeColor = b.optString("badgeColor", "#4F46E5"),
                  title = b.optString("title", "Important Update"),
                  body = b.optString("body", ""),
                  linkText = b.optString("linkText", "Learn more →"),
                  linkUrl = b.optString("linkUrl", "https://example.com")
                )
              )
              "DIVIDER" -> parsedBlocks.add(EmailBlock.Divider(id = id))
              "FOOTER" -> parsedBlocks.add(
                EmailBlock.Footer(
                  id = id,
                  signOff = b.optString("signOff", "Best regards,\nThe Team"),
                  companyName = b.optString("companyName", "MailCraft Studio"),
                  disclaimer = b.optString("disclaimer", "Unsubscribe or manage preferences at any time.")
                )
              )
            }
          }

          if (parsedBlocks.isEmpty()) {
            parsedBlocks.add(EmailBlock.Header(id = UUID.randomUUID().toString(), text = prompt))
            parsedBlocks.add(EmailBlock.Text(id = UUID.randomUUID().toString(), content = rawContent))
          }

          Result.success(GeneratedEmail(subject, preview, parsedBlocks))
        } catch (e: Exception) {
          Log.w(TAG, "Failed to parse structured JSON, returning as text fallback", e)
          val fallbackBlocks = listOf(
            EmailBlock.Header(id = UUID.randomUUID().toString(), text = "Generated Email", subtitle = prompt),
            EmailBlock.Text(id = UUID.randomUUID().toString(), content = rawContent.replace("```", "").trim()),
            EmailBlock.Button(id = UUID.randomUUID().toString(), label = "View Details", url = "https://example.com"),
            EmailBlock.Footer(id = UUID.randomUUID().toString())
          )
          Result.success(GeneratedEmail("New Email Update", prompt, fallbackBlocks))
        }
      },
      onFailure = { Result.failure(it) }
    )
  }

  suspend fun fixAndOptimizeHtml(
    rawHtml: String,
    instruction: String = "Fix unclosed tags, inline styles, add responsive tables for Gmail and Outlook, and beautify design",
    model: String = DEFAULT_MODEL
  ): Result<String> = withContext(Dispatchers.IO) {
    val sysPrompt = """
You are a senior HTML email engineer. Your task is to take the provided HTML email code and optimize it.
Requirements:
1. Fix all unclosed tags, malformed syntax, or deprecated markup.
2. Ensure full email-client compatibility (Gmail, Outlook, Apple Mail):
   - Wrap content in a 600px max-width centered container table.
   - Use inline CSS styles (avoid external stylesheets or unsupported CSS properties).
   - Ensure clean typography, generous padding, and responsive image max-widths.
3. Apply modern, elegant visual design with harmonious colors and buttons.
4. Return ONLY the raw HTML code inside a ```html ``` markdown block. No conversational filler.
"""
    val userMsg = "Instruction: $instruction\n\nOriginal HTML:\n```html\n$rawHtml\n```"

    val messagesArray = JSONArray().apply {
      put(JSONObject().apply {
        put("role", "system")
        put("content", sysPrompt)
      })
      put(JSONObject().apply {
        put("role", "user")
        put("content", userMsg)
      })
    }

    val res = executeChatWithFallback(
      messages = messagesArray,
      preferredModel = model,
      temperature = 0.5
    )

    res.fold(
      onSuccess = { content ->
        val extracted = extractCodeBlock(content, "html") ?: content.trim()
        Result.success(extracted)
      },
      onFailure = { Result.failure(it) }
    )
  }

  suspend fun generateHtmlFromPrompt(
    prompt: String,
    style: String = "Modern & Responsive",
    model: String = DEFAULT_MODEL
  ): Result<String> = withContext(Dispatchers.IO) {
    val sysPrompt = """
You are an expert HTML email designer.
Write a complete, production-ready, bulletproof responsive HTML email based on the user's prompt.
Requirements:
- 100% inline CSS styles.
- 600px centered table wrapper (`role="presentation"`).
- Visually stunning typography, buttons, hero section, and responsive tables.
- Cross-client compatible with Gmail, Yahoo, iOS Mail, and Outlook.
- Return ONLY the HTML code wrapped inside ```html ```.
"""
    val userMsg = "Create an email design for: $prompt\nStyle: $style\nInclude full HTML skeleton from <!DOCTYPE html> to </html>."

    val messagesArray = JSONArray().apply {
      put(JSONObject().apply {
        put("role", "system")
        put("content", sysPrompt)
      })
      put(JSONObject().apply {
        put("role", "user")
        put("content", userMsg)
      })
    }

    val res = executeChatWithFallback(
      messages = messagesArray,
      preferredModel = model,
      temperature = 0.7
    )

    res.fold(
      onSuccess = { content ->
        val extracted = extractCodeBlock(content, "html") ?: content.trim()
        Result.success(extracted)
      },
      onFailure = { Result.failure(it) }
    )
  }

  suspend fun suggestSubjectLines(
    topicOrContent: String,
    model: String = DEFAULT_MODEL
  ): Result<List<String>> = withContext(Dispatchers.IO) {
    val sysPrompt = """
You are a master email copywriter specializing in subject lines with 45%+ open rates.
Generate 5 distinct subject line options (Urgent, Curiosity, Direct, Benefit-focused, and Playful with emoji).
Return ONLY a valid JSON array of strings, e.g. ["Subject 1", "Subject 2", "Subject 3", "Subject 4", "Subject 5"].
No markdown tags or extra explanation.
"""
    val messagesArray = JSONArray().apply {
      put(JSONObject().apply {
        put("role", "system")
        put("content", sysPrompt)
      })
      put(JSONObject().apply {
        put("role", "user")
        put("content", "Topic/Content: $topicOrContent")
      })
    }

    val res = executeChatWithFallback(
      messages = messagesArray,
      preferredModel = model,
      temperature = 0.8
    )

    res.fold(
      onSuccess = { content ->
        try {
          val clean = cleanJsonString(content)
          val array = JSONArray(clean)
          val list = mutableListOf<String>()
          for (i in 0 until array.length()) {
            list.add(array.getString(i))
          }
          Result.success(list)
        } catch (_: Exception) {
          val lines = content.lines()
            .map { it.replace(Regex("^[0-9]+[.)]\\s*"), "").replace("\"", "").trim() }
            .filter { it.isNotBlank() && !it.startsWith("[") && !it.startsWith("]") }
            .take(5)
          Result.success(if (lines.isNotEmpty()) lines else listOf("Exciting News for You ✨", "Quick update inside", "Don't miss this!"))
        }
      },
      onFailure = { Result.failure(it) }
    )
  }

  /**
   * Sanitizes error messages to completely hide third-party router and internal names.
   */
  private fun sanitizeError(responseBody: String, statusCode: Int): String {
    return when (statusCode) {
      401 -> "Invalid AI credentials. Please ensure your API key is correctly saved in Secrets."
      404 -> "Model temporarily unavailable. Retrying with next free engine..."
      429 -> "Rate limit reached. Please wait a brief moment before sending another prompt."
      500, 502, 503 -> "AI engine is temporarily busy. Retrying shortly..."
      else -> "AI service is currently busy ($statusCode). Please try again in a few moments."
    }
  }

  private fun cleanJsonString(raw: String): String {
    var s = raw.trim()
    if (s.startsWith("```json")) s = s.substring(7)
    else if (s.startsWith("```")) s = s.substring(3)
    if (s.endsWith("```")) s = s.substring(0, s.length - 3)
    return s.trim()
  }

  fun extractCodeBlock(content: String, lang: String = "html"): String? {
    val regex = Regex("```(?:$lang)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
    val match = regex.find(content)
    return match?.groupValues?.get(1)?.trim()
  }
}
