package com.example.ai

import com.example.model.EmailBlock
import java.util.UUID

/**
 * AI Model specification for MailCraft.
 * Uses 100% free models (:free) with clean, professional branding without internal router names.
 */
data class AiModelOption(
  val id: String,
  val displayName: String,
  val badge: String,
  val description: String,
  val isRecommended: Boolean = false
)

val AVAILABLE_AI_MODELS = listOf(
  AiModelOption(
    id = "meta-llama/llama-3.3-70b-instruct:free",
    displayName = "Llama 3.3 70B",
    badge = "Free • Recommended",
    description = "Ultra versatile reasoning for email copywriting, structure & deliverability",
    isRecommended = true
  ),
  AiModelOption(
    id = "deepseek/deepseek-chat:free",
    displayName = "DeepSeek V3",
    badge = "Free • Fast",
    description = "Ultra rapid generation for marketing copy, snippets & support queries"
  ),
  AiModelOption(
    id = "qwen/qwen-2.5-coder-32b-instruct:free",
    displayName = "Qwen 2.5 Coder",
    badge = "Free • Code Specialist",
    description = "Specialized model for HTML email tables and bulletproof CSS styling"
  ),
  AiModelOption(
    id = "deepseek/deepseek-r1:free",
    displayName = "DeepSeek R1",
    badge = "Free • Deep Logic",
    description = "Advanced reasoning for complex campaign architecture and spam prevention"
  ),
  AiModelOption(
    id = "meta-llama/llama-3.1-8b-instruct:free",
    displayName = "Llama 3.1 8B",
    badge = "Free • Snappy",
    description = "Fast, lightweight model for instant subject lines and quick replies"
  )
)

/**
 * Fallback free models tried in sequence if any model errors (404, rate limit, unavailable).
 */
val FREE_MODELS_FALLBACK_LIST = listOf(
  "meta-llama/llama-3.3-70b-instruct:free",
  "deepseek/deepseek-chat:free",
  "qwen/qwen-2.5-coder-32b-instruct:free",
  "deepseek/deepseek-r1:free",
  "meta-llama/llama-3.1-8b-instruct:free",
  "mistralai/mistral-7b-instruct:free"
)

data class AiChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val role: String, // "system", "user", "assistant"
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val extractedCode: String? = null,
  val isCodeHtml: Boolean = false
)

data class GeneratedEmail(
  val subject: String,
  val previewSnippet: String,
  val blocks: List<EmailBlock>,
  val rawHtml: String? = null
)
