package com.example.ai

import com.example.model.EmailBlock
import java.util.UUID

/**
 * AI Model specification for MailCraft.
 * Uses 100% active free models (:free) with clean, professional branding without internal router names.
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
    id = "openrouter/free",
    displayName = "Smart Auto Free",
    badge = "Free • Recommended",
    description = "Automatically connects to the most reliable, highest-speed free AI model available",
    isRecommended = true
  ),
  AiModelOption(
    id = "google/gemma-4-31b-it:free",
    displayName = "Gemma 4 31B",
    badge = "Free • High Quality",
    description = "Ultra versatile reasoning for email copywriting, structure & deliverability"
  ),
  AiModelOption(
    id = "nvidia/nemotron-3.5-lightning:free",
    displayName = "Nemotron 3.5 Fast",
    badge = "Free • Snappy",
    description = "Ultra rapid generation for marketing copy, snippets & support queries"
  ),
  AiModelOption(
    id = "cohere/north-mini-code:free",
    displayName = "North Mini Code",
    badge = "Free • Code Specialist",
    description = "Specialized model for HTML email tables and bulletproof CSS styling"
  ),
  AiModelOption(
    id = "google/gemma-4-26b-a4b-it:free",
    displayName = "Gemma 4 26B",
    badge = "Free • Lightweight",
    description = "Fast model for instant subject lines and quick replies"
  )
)

/**
 * Fallback free models tried in sequence if any model errors (404, rate limit, unavailable).
 * 'openrouter/free' is first to guarantee dynamic failover to whatever free model is currently active.
 */
val FREE_MODELS_FALLBACK_LIST = listOf(
  "openrouter/free",
  "google/gemma-4-31b-it:free",
  "nvidia/nemotron-3.5-lightning:free",
  "google/gemma-4-26b-a4b-it:free",
  "cohere/north-mini-code:free",
  "liquid/lfm-2.5-2.6b:free",
  "thinkingmachines/inkling:free",
  "poolside/laguna-s-2.1:free"
)

sealed class AiAppAction {
  data class OpenDraft(val id: Long, val title: String? = null) : AiAppAction()
  data class SwitchTab(val tabName: String) : AiAppAction()
  data class SetSubject(val text: String) : AiAppAction()
  data class SetRecipient(val email: String) : AiAppAction()
  data class SetHtml(val html: String) : AiAppAction()
  data class SendTest(val note: String? = null) : AiAppAction()
  object FormatHtml : AiAppAction()
}

data class AiChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val role: String, // "system", "user", "assistant"
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val extractedCode: String? = null,
  val isCodeHtml: Boolean = false,
  val actions: List<AiAppAction> = emptyList()
)

data class GeneratedEmail(
  val subject: String,
  val previewSnippet: String,
  val blocks: List<EmailBlock>,
  val rawHtml: String? = null
)
