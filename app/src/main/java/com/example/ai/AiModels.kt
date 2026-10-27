package com.example.ai

import com.example.model.EmailBlock
import java.util.UUID

/**
 * AI Model specification for MailCraft.
 * UI display names are clean and professional without internal router naming.
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
    id = "anthropic/claude-3.5-sonnet",
    displayName = "Claude 3.5 Sonnet",
    badge = "Ultra Intelligent",
    description = "Industry standard for creative email copywriting, structured layouts & bulletproof HTML",
    isRecommended = true
  ),
  AiModelOption(
    id = "openai/gpt-4o-mini",
    displayName = "GPT-4o Mini",
    badge = "Fast & Concise",
    description = "Ultra rapid generation for snappy newsletters, quick subject lines & support answers"
  ),
  AiModelOption(
    id = "meta-llama/llama-3.3-70b-instruct",
    displayName = "Llama 3.3 70B",
    badge = "High Versatility",
    description = "Strong open reasoning model with natural conversational tone"
  ),
  AiModelOption(
    id = "deepseek/deepseek-chat",
    displayName = "DeepSeek V3",
    badge = "Code Specialist",
    description = "Deep technical reasoning, excellent for CSS debugging and email compatibility"
  )
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
