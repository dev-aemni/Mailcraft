package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.ClientDarkMode
import com.example.model.EmailAttachment
import com.example.data.CustomTemplateEntity
import com.example.data.MailCraftDatabase
import com.example.data.SavedDraftEntity
import com.example.data.SentEmailEntity
import com.example.data.SettingsManager
import com.example.model.BlockType
import com.example.model.EmailBlock
import com.example.model.EmailMessage
import com.example.model.EmailTemplate
import com.example.model.SenderAccount
import com.example.model.SmtpSendResult
import com.example.model.TemplatePresets
import com.example.network.GmailSmtpClient
import com.example.util.HtmlEmailGenerator
import com.example.ai.AiAppAction
import com.example.ai.AiChatMessage
import com.example.ai.AiKeyProvider
import com.example.ai.AiModelOption
import com.example.ai.AVAILABLE_AI_MODELS
import com.example.ai.GeneratedEmail
import com.example.ai.MailCraftAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab {
  COMPOSE,
  HTML_WORKSTATION,
  AI_BOT,
  PREVIEW,
  DEPLOY,
  TEMPLATES,
  HISTORY,
  DRAFTS
}

sealed class SendState {
  object Idle : SendState()
  data class Sending(val logs: List<String> = emptyList()) : SendState()
  data class Success(
    val message: String,
    val messageId: String?,
    val recipient: String = "",
    val senderEmail: String = "",
    val isSelfSend: Boolean = false,
    val logs: List<String> = emptyList()
  ) : SendState()
  data class Error(val error: String, val logs: List<String> = emptyList()) : SendState()
}

class MailCraftViewModel(application: Application) : AndroidViewModel(application) {

  val settingsManager = SettingsManager(application)
  private val database = Room.databaseBuilder(
    application,
    MailCraftDatabase::class.java,
    "mailcraft_db"
  ).fallbackToDestructiveMigration(true).build()
  private val mailDao = database.mailDao()

  val senderAccount: StateFlow<SenderAccount> = settingsManager.senderAccount

  private val _recipientEmail = MutableStateFlow("")
  val recipientEmail: StateFlow<String> = _recipientEmail.asStateFlow()

  private val _ccEmail = MutableStateFlow("")
  val ccEmail: StateFlow<String> = _ccEmail.asStateFlow()

  private val _bccEmail = MutableStateFlow("")
  val bccEmail: StateFlow<String> = _bccEmail.asStateFlow()

  private val _subject = MutableStateFlow("Exciting Update from MailCraft ✨")
  val subject: StateFlow<String> = _subject.asStateFlow()

  private val _blocks = MutableStateFlow<List<EmailBlock>>(emptyList())
  val blocks: StateFlow<List<EmailBlock>> = _blocks.asStateFlow()

  private val _activeTab = MutableStateFlow(AppTab.COMPOSE)
  val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

  private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
  val sendState: StateFlow<SendState> = _sendState.asStateFlow()

  private val _testConnectionResult = MutableStateFlow<SmtpSendResult?>(null)
  val testConnectionResult: StateFlow<SmtpSendResult?> = _testConnectionResult.asStateFlow()

  private val _isTestingConnection = MutableStateFlow(false)
  val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

  private val _recentRecipients = MutableStateFlow<List<String>>(settingsManager.getRecentRecipients())
  val recentRecipients: StateFlow<List<String>> = _recentRecipients.asStateFlow()

  private val _customHtmlContent = MutableStateFlow("")
  val customHtmlContent: StateFlow<String> = _customHtmlContent.asStateFlow()

  private val _isCustomHtmlActive = MutableStateFlow(false)
  val isCustomHtmlActive: StateFlow<Boolean> = _isCustomHtmlActive.asStateFlow()

  private val _attachments = MutableStateFlow<List<EmailAttachment>>(emptyList())
  val attachments: StateFlow<List<EmailAttachment>> = _attachments.asStateFlow()

  private val _clientDarkMode = MutableStateFlow(ClientDarkMode.OFF)
  val clientDarkMode: StateFlow<ClientDarkMode> = _clientDarkMode.asStateFlow()

  private val isSystemDarkDefault: Boolean =
    (application.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

  private val _isAppDarkMode = MutableStateFlow(settingsManager.isDarkMode(isSystemDarkDefault))
  val isAppDarkMode: StateFlow<Boolean> = _isAppDarkMode.asStateFlow()

  fun toggleAppDarkMode() {
    val next = !_isAppDarkMode.value
    _isAppDarkMode.value = next
    settingsManager.saveDarkMode(next)
  }

  fun setAppDarkMode(isDark: Boolean) {
    _isAppDarkMode.value = isDark
    settingsManager.saveDarkMode(isDark)
  }

  private val _currentDraftId = MutableStateFlow<Long?>(null)
  val currentDraftId: StateFlow<Long?> = _currentDraftId.asStateFlow()

  val aiService = MailCraftAiService()

  private val _selectedAiModel = MutableStateFlow(AVAILABLE_AI_MODELS[0])
  val selectedAiModel: StateFlow<AiModelOption> = _selectedAiModel.asStateFlow()

  private val _aiChatMessages = MutableStateFlow<List<AiChatMessage>>(
    listOf(
      AiChatMessage(
        role = "assistant",
        content = "👋 Hello! I am your **MailCraft AI Assistant**.\n\nI can help you:\n• Create high-converting emails & catchy subject lines\n• Fix, optimize, and beautify your custom HTML code\n• Configure Gmail SMTP (16-char App Passwords) and troubleshoot deliverability\n\nHow can I help you today?"
      )
    )
  )
  val aiChatMessages: StateFlow<List<AiChatMessage>> = _aiChatMessages.asStateFlow()

  private val _isAiThinking = MutableStateFlow(false)
  val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

  private val _aiError = MutableStateFlow<String?>(null)
  val aiError: StateFlow<String?> = _aiError.asStateFlow()

  fun selectAiModel(model: AiModelOption) {
    _selectedAiModel.value = model
  }

  fun clearAiError() {
    _aiError.value = null
  }

  fun clearAiChatHistory() {
    _aiChatMessages.value = listOf(
      AiChatMessage(
        role = "assistant",
        content = "Chat cleared. What would you like to work on next?"
      )
    )
  }

  private var _previousHtmlCode: String? = null
  val canUndoAiEdit: Boolean get() = _previousHtmlCode != null

  fun undoLastAiCodeEdit(): Boolean {
    val prev = _previousHtmlCode ?: return false
    _customHtmlContent.value = prev
    _previousHtmlCode = null
    saveCurrentDraft()
    return true
  }

  fun getCurrentProjectContextPrompt(): String {
    val currentCode = _customHtmlContent.value
    val assetNames = _attachments.value.map { "${it.fileName} (${it.sizeBytes / 1024}KB)" }
    val templates = customTemplates.value
    val drafts = savedDrafts.value
    val history = sentEmails.value
    val currentSender = senderAccount.value
    val activeTabName = _activeTab.value.name

    val draftsSummary = if (drafts.isEmpty()) {
      "No drafts currently saved in Room database."
    } else {
      drafts.mapIndexed { idx, d ->
        val length = if (d.isCustomHtml) d.customHtml.length else d.blocksJson.length
        val words = if (d.isCustomHtml) {
          d.customHtml.split(Regex("\\s+")).count { it.isNotBlank() }
        } else {
          d.previewSnippet.split(Regex("\\s+")).count { it.isNotBlank() }
        }
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(d.updatedTimestamp))
        """Draft #${idx + 1} [ID: ${d.id}]:
  • Title: "${d.title}"
  • Subject: "${d.subject.ifBlank { "(No subject)" }}"
  • Recipient: "${d.recipient.ifBlank { "(None)" }}"
  • Format: ${if (d.isCustomHtml) "Custom HTML" else "Visual Blocks"}
  • Content Size: $length characters, ~$words words
  • Preview Snippet: "${d.previewSnippet.take(120)}"
  • Last Updated: $dateStr"""
      }.joinToString("\n\n")
    }

    val historySummary = if (history.isEmpty()) {
      "No emails sent yet."
    } else {
      history.take(6).mapIndexed { idx, h ->
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(h.sentTimestamp))
        "• Sent #${idx + 1} [ID: ${h.id}]: To=${h.toEmail}, Subject=\"${h.subject}\", Status=${h.status}, Date=$dateStr"
      }.joinToString("\n")
    }

    val codePreview = if (currentCode.length > 5000) {
      currentCode.take(5000) + "\n...[code continued, ${currentCode.lines().size} lines total]"
    } else {
      currentCode
    }

    return """
Developer, Project Knowledge & Important URLs:
- Project: MailCraft (v1.1.1) — Native Android HTML Email Workstation & Direct Primary Inbox Client
- Author/Creator: 2026 @dev-aemni
- Official Webpage & Showcase: https://dev-aemni.github.io/Mailcraft/ (dev-aemni.github.io/Mailcraft)
- Help & Setup Documentation: https://dev-aemni.github.io/Mailcraft/help/ (dev-aemni.github.io/Mailcraft/help)
- "To Request features/update" / Issue Tracker: https://github.com/dev-aemni/Mailcraft/issues
- Releases & APK Downloads: https://github.com/dev-aemni/Mailcraft/releases
- Knowledge rules: If the user asks about documentation, 16-char Google App Passwords, local HTTP preview on random ephemeral ports (e.g. 63823), bug reports, feature requests, or updates, cite © 2026 @dev-aemni and provide these exact clean official URLs.

You are MailCraft AI, with FULL REAL-TIME AWARENESS AND DIRECT CONTROL of the entire application.
You have direct read and control access to every single corner of our app:
1. Active Screen / Workspace: $activeTabName
2. Current Recipient: "${_recipientEmail.value}"
3. Current Subject: "${_subject.value}"
4. Sender Account: Email=${currentSender.email.ifBlank { "Not configured" }}, Name="${currentSender.displayName}", SMTP=${currentSender.smtpHost}:${currentSender.smtpPort}, HasAppPassword=${currentSender.appPassword.isNotBlank()}
5. Attachments: ${if (assetNames.isNotEmpty()) assetNames.joinToString(", ") else "None"}
6. Active HTML Workplace (index.html): ${currentCode.lines().size} lines, ${currentCode.length} chars

Saved Room Database Drafts (${drafts.size} total):
$draftsSummary

Recent Sent History (${history.size} total):
$historySummary

Saved Custom Templates: ${templates.size} saved (${templates.map { it.title }.joinToString(", ").ifBlank { "none" }})

APP CONTROL COMMANDS:
You can control the app! When the user asks you to navigate, load a draft, change recipient/subject, check drafts, or apply code, you can execute commands by appending action tags to your answer:
• To open/load a draft into editor: [ACTION: OPEN_DRAFT id=<draft_id>]
• To switch app screens: [ACTION: SWITCH_TAB tab=COMPOSE|HTML_WORKSTATION|PREVIEW|DEPLOY|DRAFTS|HISTORY|TEMPLATES]
• To set email subject: [ACTION: SET_SUBJECT text="..."]
• To set recipient email: [ACTION: SET_RECIPIENT email="..."]
• To load/update HTML code: [ACTION: SET_HTML]```html ... ```[/ACTION] (or include ```html ... ``` code fences)
• To send a test email to the user's inbox: [ACTION: SEND_TEST]
• To format the code: [ACTION: FORMAT_HTML]

IMPORTANT EXAMPLE:
If the user asks: "check my drafts .. which one is shortest that I can continue now", you MUST:
1. Examine the Saved Room Database Drafts listed above.
2. Compare their content size / character count.
3. Clearly state the total drafts found and identify the shortest draft (with its ID, title, character count, and preview).
4. Include [ACTION: OPEN_DRAFT id=<id>] so the app can load that draft directly for the user!

Current Code in index.html:
```html
$codePreview
```
""".trimIndent()
  }

  fun parseAiActions(content: String): List<AiAppAction> {
    val actions = mutableListOf<AiAppAction>()

    // [ACTION: OPEN_DRAFT id=123]
    val openDraftRegex = Regex("""\[ACTION:\s*OPEN_DRAFT\s+id=(\d+)\]""", RegexOption.IGNORE_CASE)
    openDraftRegex.findAll(content).forEach { match ->
      match.groupValues.getOrNull(1)?.toLongOrNull()?.let { id ->
        val title = savedDrafts.value.find { it.id == id }?.title
        actions.add(AiAppAction.OpenDraft(id, title))
      }
    }

    // [ACTION: SWITCH_TAB tab=DRAFTS]
    val switchTabRegex = Regex("""\[ACTION:\s*SWITCH_TAB\s+tab=([A-Za-z_]+)\]""", RegexOption.IGNORE_CASE)
    switchTabRegex.findAll(content).forEach { match ->
      match.groupValues.getOrNull(1)?.let { tabName ->
        actions.add(AiAppAction.SwitchTab(tabName.uppercase()))
      }
    }

    // [ACTION: SET_SUBJECT text="..."]
    val setSubjectRegex = Regex("""\[ACTION:\s*SET_SUBJECT\s+text="([^"]+)"\]""", RegexOption.IGNORE_CASE)
    setSubjectRegex.findAll(content).forEach { match ->
      match.groupValues.getOrNull(1)?.let { text ->
        actions.add(AiAppAction.SetSubject(text))
      }
    }

    // [ACTION: SET_RECIPIENT email="..."]
    val setRecipientRegex = Regex("""\[ACTION:\s*SET_RECIPIENT\s+email="([^"]+)"\]""", RegexOption.IGNORE_CASE)
    setRecipientRegex.findAll(content).forEach { match ->
      match.groupValues.getOrNull(1)?.let { email ->
        actions.add(AiAppAction.SetRecipient(email))
      }
    }

    // [ACTION: SEND_TEST]
    if (content.contains("[ACTION: SEND_TEST]", ignoreCase = true)) {
      actions.add(AiAppAction.SendTest())
    }

    // [ACTION: FORMAT_HTML]
    if (content.contains("[ACTION: FORMAT_HTML]", ignoreCase = true)) {
      actions.add(AiAppAction.FormatHtml)
    }

    return actions
  }

  fun executeAiAppAction(action: AiAppAction, onFeedback: ((String) -> Unit)? = null) {
    when (action) {
      is AiAppAction.OpenDraft -> {
        val target = savedDrafts.value.find { it.id == action.id }
        if (target != null) {
          loadDraft(target)
          onFeedback?.invoke("Loaded draft \"${target.title}\" into active workspace!")
        } else {
          onFeedback?.invoke("Draft #${action.id} not found.")
        }
      }
      is AiAppAction.SwitchTab -> {
        try {
          val tab = AppTab.valueOf(action.tabName)
          setActiveTab(tab)
          onFeedback?.invoke("Switched to ${tab.name}")
        } catch (_: Exception) {
          onFeedback?.invoke("Unknown tab: ${action.tabName}")
        }
      }
      is AiAppAction.SetSubject -> {
        _subject.value = action.text
        onFeedback?.invoke("Subject updated to: \"${action.text}\"")
      }
      is AiAppAction.SetRecipient -> {
        _recipientEmail.value = action.email
        onFeedback?.invoke("Recipient set to: ${action.email}")
      }
      is AiAppAction.SetHtml -> {
        applyHtmlToWorkstation(action.html)
        onFeedback?.invoke("HTML applied to Workstation!")
      }
      is AiAppAction.SendTest -> {
        sendToMyInbox()
        onFeedback?.invoke("Sending test to ${senderAccount.value.email}...")
      }
      is AiAppAction.FormatHtml -> {
        formatCurrentCustomHtml()
        onFeedback?.invoke("HTML formatted!")
      }
    }
  }

  fun sendAiChatMessage(userPrompt: String) {
    if (userPrompt.isBlank()) return
    val userMsg = AiChatMessage(role = "user", content = userPrompt.trim())
    val updatedList = _aiChatMessages.value + userMsg
    _aiChatMessages.value = updatedList
    _isAiThinking.value = true
    _aiError.value = null

    viewModelScope.launch {
      val result = aiService.chat(
        conversation = updatedList.takeLast(10),
        model = _selectedAiModel.value.id,
        systemPrompt = getCurrentProjectContextPrompt()
      )
      _isAiThinking.value = false
      result.fold(
        onSuccess = { reply ->
          val extracted = aiService.extractCodeBlock(reply, "html")
          val actions = parseAiActions(reply)
          val displayContent = reply.replace(Regex("""\[ACTION:[^\]]+\]"""), "").trim()

          _aiChatMessages.value = _aiChatMessages.value + AiChatMessage(
            role = "assistant",
            content = displayContent.ifBlank { reply },
            extractedCode = extracted,
            isCodeHtml = extracted != null,
            actions = actions
          )

          // If a single navigation or draft load was found, auto-execute it!
          if (actions.size == 1) {
            executeAiAppAction(actions[0])
          }
        },
        onFailure = { err ->
          val raw = err.localizedMessage.orEmpty()
          val friendly = if (raw.contains("OpenRouter", ignoreCase = true) || raw.contains("404") || raw.contains("claude", ignoreCase = true) || raw.contains("provider", ignoreCase = true)) {
            "MailCraft AI is momentarily busy. Please try sending your message again."
          } else {
            raw.ifBlank { "MailCraft AI is momentarily busy. Please check your connection and try again." }
          }
          _aiError.value = friendly
          _aiChatMessages.value = _aiChatMessages.value + AiChatMessage(
            role = "assistant",
            content = "⚠️ $friendly"
          )
        }
      )
    }
  }

  /**
   * Directly modifies the workplace code using AI instructions.
   */
  fun modifyCodeWithAi(instruction: String, onComplete: (Boolean, String) -> Unit) {
    if (instruction.isBlank()) return
    _isAiThinking.value = true
    viewModelScope.launch {
      val contextPrompt = getCurrentProjectContextPrompt()
      val userPrompt = """
Task: $instruction
Please modify and update the active code according to this task.
Return the complete, updated HTML code inside ```html ... ``` code fences.
      """.trimIndent()

      val result = aiService.chat(
        conversation = listOf(AiChatMessage(role = "user", content = userPrompt)),
        model = _selectedAiModel.value.id,
        systemPrompt = contextPrompt
      )
      _isAiThinking.value = false
      result.fold(
        onSuccess = { reply ->
          val extracted = aiService.extractCodeBlock(reply, "html") ?: reply.trim()
          if (extracted.contains("<html", ignoreCase = true) ||
            extracted.contains("<!DOCTYPE", ignoreCase = true) ||
            extracted.contains("<table", ignoreCase = true) ||
            extracted.contains("<div", ignoreCase = true)
          ) {
            _previousHtmlCode = _customHtmlContent.value
            _customHtmlContent.value = extracted
            _isCustomHtmlActive.value = true
            saveCurrentDraft()
            onComplete(true, "Applied changes directly to index.html!")
          } else {
            onComplete(false, "AI provided recommendations without full replacement code.")
          }
        },
        onFailure = { err ->
          onComplete(false, err.localizedMessage ?: "Failed to connect to MailCraft AI.")
        }
      )
    }
  }

  fun generateEmailWithAi(
    prompt: String,
    tone: String = "Professional & Engaging",
    onSuccess: (GeneratedEmail) -> Unit,
    onError: (String) -> Unit
  ) {
    _isAiThinking.value = true
    viewModelScope.launch {
      val res = aiService.generateVisualEmail(prompt, tone, _selectedAiModel.value.id)
      _isAiThinking.value = false
      res.fold(
        onSuccess = { generated -> onSuccess(generated) },
        onFailure = { err -> onError(err.localizedMessage ?: "Failed to generate email.") }
      )
    }
  }

  fun fixHtmlWithAi(
    html: String? = null,
    instruction: String = "Fix unclosed tags, inline styles, add responsive tables for Gmail and Outlook, and beautify design",
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    val currentHtml = if (!html.isNullOrBlank()) {
      html
    } else if (_isCustomHtmlActive.value && _customHtmlContent.value.isNotBlank()) {
      _customHtmlContent.value
    } else {
      HtmlEmailGenerator.generateHtml(getCurrentEmailMessage(), evaluateVars = false)
    }

    _isAiThinking.value = true
    viewModelScope.launch {
      val res = aiService.fixAndOptimizeHtml(currentHtml, instruction, _selectedAiModel.value.id)
      _isAiThinking.value = false
      res.fold(
        onSuccess = { fixed -> onSuccess(fixed) },
        onFailure = { err -> onError(err.localizedMessage ?: "Failed to optimize HTML.") }
      )
    }
  }

  fun generateHtmlWithAi(
    prompt: String,
    style: String = "Modern & Responsive",
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    _isAiThinking.value = true
    viewModelScope.launch {
      val res = aiService.generateHtmlFromPrompt(prompt, style, _selectedAiModel.value.id)
      _isAiThinking.value = false
      res.fold(
        onSuccess = { code -> onSuccess(code) },
        onFailure = { err -> onError(err.localizedMessage ?: "Failed to generate HTML.") }
      )
    }
  }

  fun suggestSubjectLinesWithAi(
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
  ) {
    val content = if (_isCustomHtmlActive.value && _customHtmlContent.value.isNotBlank()) {
      _customHtmlContent.value.take(400)
    } else {
      _blocks.value.filterIsInstance<EmailBlock.Header>().firstOrNull()?.text
        ?: _blocks.value.filterIsInstance<EmailBlock.Text>().firstOrNull()?.content
        ?: _subject.value
    }

    _isAiThinking.value = true
    viewModelScope.launch {
      val res = aiService.suggestSubjectLines(content, _selectedAiModel.value.id)
      _isAiThinking.value = false
      res.fold(
        onSuccess = { list -> onSuccess(list) },
        onFailure = { err -> onError(err.localizedMessage ?: "Failed to suggest subject lines.") }
      )
    }
  }

  fun applyGeneratedEmail(generated: GeneratedEmail) {
    _subject.value = generated.subject
    _blocks.value = generated.blocks
    _isCustomHtmlActive.value = false
    _activeTab.value = AppTab.COMPOSE
  }

  fun applyHtmlToWorkstation(html: String) {
    _customHtmlContent.value = html
    _isCustomHtmlActive.value = true
    _activeTab.value = AppTab.HTML_WORKSTATION
  }

  val sentEmails: StateFlow<List<SentEmailEntity>> = mailDao.getAllSentEmails().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  val savedDrafts: StateFlow<List<SavedDraftEntity>> = mailDao.getAllDrafts().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  val customTemplates: StateFlow<List<CustomTemplateEntity>> = mailDao.getAllCustomTemplates().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  fun saveCurrentDraft(customTitle: String? = null, onSaved: ((Long) -> Unit)? = null) {
    val isCustom = _isCustomHtmlActive.value
    val htmlContent = _customHtmlContent.value
    val blocks = _blocks.value
    val jsonBlocks = com.example.util.EmailBlockJsonAdapter.blocksToJson(blocks)

    val fallbackTitle = when {
      _subject.value.isNotBlank() && _subject.value != "Exciting Update from MailCraft ✨" -> _subject.value
      isCustom -> "HTML Draft"
      blocks.isNotEmpty() -> {
        val firstHeader = blocks.filterIsInstance<EmailBlock.Header>().firstOrNull()?.text
        firstHeader ?: "Visual Draft (${blocks.size} blocks)"
      }
      else -> "Untitled Draft"
    }

    val finalTitle = customTitle?.ifBlank { null } ?: fallbackTitle
    val snippet = if (isCustom && htmlContent.isNotBlank()) {
      htmlContent.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim().take(120)
    } else {
      blocks.joinToString(" ") { block ->
        when (block) {
          is EmailBlock.Header -> "${block.text} ${block.subtitle}"
          is EmailBlock.Text -> block.content
          is EmailBlock.Button -> block.label
          is EmailBlock.EmbedCard -> "${block.title} ${block.body}"
          else -> ""
        }
      }.trim().take(120)
    }

    viewModelScope.launch {
      val draft = SavedDraftEntity(
        id = _currentDraftId.value ?: 0L,
        title = finalTitle,
        recipient = _recipientEmail.value,
        cc = _ccEmail.value,
        bcc = _bccEmail.value,
        subject = _subject.value,
        blocksJson = jsonBlocks,
        customHtml = htmlContent,
        isCustomHtml = isCustom,
        previewSnippet = snippet,
        updatedTimestamp = System.currentTimeMillis()
      )
      val newId = mailDao.insertDraft(draft)
      _currentDraftId.value = newId
      onSaved?.invoke(newId)
    }
  }

  fun loadDraft(draft: SavedDraftEntity) {
    _recipientEmail.value = draft.recipient
    _ccEmail.value = draft.cc
    _bccEmail.value = draft.bcc
    _subject.value = draft.subject
    _currentDraftId.value = draft.id

    if (draft.isCustomHtml || draft.customHtml.isNotBlank()) {
      _customHtmlContent.value = draft.customHtml
      _isCustomHtmlActive.value = true
      _activeTab.value = AppTab.HTML_WORKSTATION
    } else {
      _isCustomHtmlActive.value = false
      if (draft.blocksJson.isNotBlank()) {
        val loaded = com.example.util.EmailBlockJsonAdapter.jsonToBlocks(draft.blocksJson)
        if (loaded.isNotEmpty()) {
          _blocks.value = loaded
        }
      }
      _activeTab.value = AppTab.COMPOSE
    }
  }

  fun deleteDraft(id: Long) {
    viewModelScope.launch {
      if (_currentDraftId.value == id) {
        _currentDraftId.value = null
      }
      mailDao.deleteDraft(id)
    }
  }

  fun clearAllDrafts() {
    viewModelScope.launch {
      _currentDraftId.value = null
      mailDao.clearAllDrafts()
    }
  }

  fun createNewDraft() {
    _currentDraftId.value = null
    _recipientEmail.value = ""
    _ccEmail.value = ""
    _bccEmail.value = ""
    _subject.value = "Exciting Update from MailCraft ✨"
    _attachments.value = emptyList()
    _isCustomHtmlActive.value = false
    _customHtmlContent.value = ""
    loadTemplate(TemplatePresets.allTemplates.first())
    _activeTab.value = AppTab.COMPOSE
  }

  fun saveCustomTemplate(title: String, iconEmoji: String = "📄", explicitHtml: String? = null) {
    val content = explicitHtml
      ?: if (_isCustomHtmlActive.value && _customHtmlContent.value.isNotBlank()) {
        _customHtmlContent.value
      } else {
        HtmlEmailGenerator.generateHtml(getCurrentEmailMessage(), evaluateVars = false)
      }
    val snippet = content.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim().take(120)

    viewModelScope.launch {
      mailDao.insertCustomTemplate(
        CustomTemplateEntity(
          title = title.ifBlank { "Custom Template" },
          htmlContent = content,
          subject = _subject.value,
          previewSnippet = snippet,
          iconEmoji = iconEmoji,
          createdTimestamp = System.currentTimeMillis()
        )
      )
    }
  }

  fun deleteCustomTemplate(id: Long) {
    viewModelScope.launch {
      mailDao.deleteCustomTemplate(id)
    }
  }

  fun loadCustomTemplate(template: CustomTemplateEntity) {
    _subject.value = template.subject
    _customHtmlContent.value = template.htmlContent
    _isCustomHtmlActive.value = true
  }

  fun updateAvatar(preset: String, customUrl: String = "") {
    val current = senderAccount.value
    val updated = current.copy(avatarPreset = preset, avatarUrl = customUrl)
    updateSenderAccount(updated)
  }

  init {
    // Load initial welcome template
    loadTemplate(TemplatePresets.allTemplates[0])
  }

  fun setActiveTab(tab: AppTab) {
    _activeTab.value = tab
  }

  fun setRecipientEmail(email: String) {
    _recipientEmail.value = email
  }

  fun setCcEmail(email: String) {
    _ccEmail.value = email
  }

  fun setBccEmail(email: String) {
    _bccEmail.value = email
  }

  fun setSubject(subject: String) {
    _subject.value = subject
  }

  fun setClientDarkMode(mode: ClientDarkMode) {
    _clientDarkMode.value = mode
  }

  fun addAttachmentFromUri(uri: Uri) {
    viewModelScope.launch {
      val contentResolver = getApplication<Application>().contentResolver
      var fileName = "attachment"
      var fileSize: Long = 0

      // Query OpenableColumns
      try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex != -1) {
              val name = cursor.getString(nameIndex)
              if (!name.isNullOrBlank()) fileName = name
            }
            if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
              fileSize = cursor.getLong(sizeIndex)
            }
          }
        }
      } catch (_: Exception) {}

      // Fallback mime type
      val mimeType = contentResolver.getType(uri) ?: when {
        fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
        fileName.endsWith(".png", ignoreCase = true) -> "image/png"
        fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
        fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
        fileName.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
        fileName.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
        else -> "application/octet-stream"
      }

      if (fileSize == 0L) {
        try {
          contentResolver.openInputStream(uri)?.use { stream ->
            fileSize = stream.available().toLong()
          }
        } catch (_: Exception) {}
      }

      val attachment = EmailAttachment(
        uriString = uri.toString(),
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = fileSize
      )

      val current = _attachments.value.toMutableList()
      if (current.none { it.uriString == uri.toString() }) {
        current.add(attachment)
        _attachments.value = current
      }
    }
  }

  fun removeAttachment(id: String) {
    _attachments.value = _attachments.value.filter { it.id != id }
  }

  fun clearAttachments() {
    _attachments.value = emptyList()
  }

  fun getTotalAttachmentSizeBytes(): Long {
    return _attachments.value.sumOf { it.sizeBytes }
  }

  fun updateSenderAccount(account: SenderAccount) {
    settingsManager.saveSenderAccount(account)
  }


  fun addBlock(type: BlockType, index: Int? = null) {
    val newBlock: EmailBlock = when (type) {
      BlockType.HEADER -> EmailBlock.Header(id = UUID.randomUUID().toString())
      BlockType.TEXT -> EmailBlock.Text(id = UUID.randomUUID().toString())
      BlockType.BUTTON -> EmailBlock.Button(id = UUID.randomUUID().toString())
      BlockType.GIF_IMAGE -> EmailBlock.GifImage(id = UUID.randomUUID().toString())
      BlockType.EMBED_CARD -> EmailBlock.EmbedCard(id = UUID.randomUUID().toString())
      BlockType.DIVIDER -> EmailBlock.Divider(id = UUID.randomUUID().toString())
      BlockType.SOCIAL_LINKS -> EmailBlock.SocialLinks(id = UUID.randomUUID().toString())
      BlockType.FOOTER -> EmailBlock.Footer(id = UUID.randomUUID().toString())
    }

    val current = _blocks.value.toMutableList()
    if (index != null && index in 0..current.size) {
      current.add(index, newBlock)
    } else {
      current.add(newBlock)
    }
    _blocks.value = current
  }

  fun updateBlock(updated: EmailBlock) {
    _blocks.value = _blocks.value.map { if (it.id == updated.id) updated else it }
  }

  fun removeBlock(id: String) {
    _blocks.value = _blocks.value.filter { it.id != id }
  }

  fun moveBlockUp(id: String) {
    val current = _blocks.value.toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index > 0) {
      val item = current.removeAt(index)
      current.add(index - 1, item)
      _blocks.value = current
    }
  }

  fun moveBlockDown(id: String) {
    val current = _blocks.value.toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index in 0 until current.size - 1) {
      val item = current.removeAt(index)
      current.add(index + 1, item)
      _blocks.value = current
    }
  }

  fun duplicateBlock(id: String) {
    val current = _blocks.value.toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index != -1) {
      val item = current[index]
      val duplicated: EmailBlock = when (item) {
        is EmailBlock.Header -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Text -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Button -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.GifImage -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.EmbedCard -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Divider -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.SocialLinks -> item.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Footer -> item.copy(id = UUID.randomUUID().toString())
      }
      current.add(index + 1, duplicated)
      _blocks.value = current
    }
  }

  fun loadTemplate(template: EmailTemplate) {
    _subject.value = template.subject
    // Clone blocks with fresh IDs
    _blocks.value = template.blocks.map { block ->
      when (block) {
        is EmailBlock.Header -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Text -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Button -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.GifImage -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.EmbedCard -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Divider -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.SocialLinks -> block.copy(id = UUID.randomUUID().toString())
        is EmailBlock.Footer -> block.copy(id = UUID.randomUUID().toString())
      }
    }
  }

  fun testGmailConnection(account: SenderAccount) {
    viewModelScope.launch {
      _isTestingConnection.value = true
      _testConnectionResult.value = null
      val result = GmailSmtpClient.testConnection(account)
      _testConnectionResult.value = result
      _isTestingConnection.value = false
    }
  }

  fun clearTestConnectionResult() {
    _testConnectionResult.value = null
  }

  fun resetSendState() {
    _sendState.value = SendState.Idle
  }

  fun sendEmail() {
    val currentRecipient = _recipientEmail.value.trim()
    if (currentRecipient.isBlank()) {
      _sendState.value = SendState.Error("Please enter recipient email address.")
      return
    }

    val sender = senderAccount.value
    if (sender.appPassword.isBlank()) {
      _sendState.value = SendState.Error(
        "Google App Password is missing. Please set it in Account Settings (tap key icon in top bar)."
      )
      return
    }

    val message = EmailMessage(
      sender = sender,
      to = listOf(currentRecipient),
      cc = if (_ccEmail.value.isNotBlank()) listOf(_ccEmail.value.trim()) else emptyList(),
      bcc = if (_bccEmail.value.isNotBlank()) listOf(_bccEmail.value.trim()) else emptyList(),
      subject = _subject.value.ifBlank { "Notification from MailCraft" },
      blocks = _blocks.value,
      customHtml = if (_isCustomHtmlActive.value && _customHtmlContent.value.isNotBlank()) _customHtmlContent.value else null,
      attachments = _attachments.value
    )

    viewModelScope.launch {
      _sendState.value = SendState.Sending(listOf("Initiating Gmail SMTP TLS connection..."))

      val contentResolver = getApplication<Application>().contentResolver
      val isSelf = currentRecipient.equals(sender.email.trim(), ignoreCase = true)
      val result = GmailSmtpClient.sendEmail(message, contentResolver, forceInboxSync = isSelf)
      if (result.success) {
        _sendState.value = SendState.Success(
          message = result.message,
          messageId = result.messageId,
          recipient = currentRecipient,
          senderEmail = sender.email.trim(),
          isSelfSend = isSelf,
          logs = result.logs.map { it.message }
        )
        settingsManager.addRecentRecipient(currentRecipient)
        _recentRecipients.value = settingsManager.getRecentRecipients()

        // Log to database
        val html = HtmlEmailGenerator.generateHtml(message)
        mailDao.insertSentEmail(
          SentEmailEntity(
            toEmail = currentRecipient,
            subject = message.subject,
            previewSnippet = HtmlEmailGenerator.generatePlainText(message).take(120),
            status = "DELIVERED",
            fullHtml = html
          )
        )
      } else {
        _sendState.value = SendState.Error(result.message, result.logs.map { it.message })
        // Also record failed attempt
        mailDao.insertSentEmail(
          SentEmailEntity(
            toEmail = currentRecipient,
            subject = message.subject,
            previewSnippet = HtmlEmailGenerator.generatePlainText(message).take(120),
            status = "FAILED",
            fullHtml = HtmlEmailGenerator.generateHtml(message),
            errorMessage = result.message
          )
        )
      }
    }
  }

  fun setCustomHtml(html: String) {
    _customHtmlContent.value = html
    _isCustomHtmlActive.value = true
  }

  fun enableCustomHtmlMode() {
    if (_customHtmlContent.value.isBlank()) {
      _customHtmlContent.value = HtmlEmailGenerator.formatHtml(
        HtmlEmailGenerator.generateHtml(
          EmailMessage(
            sender = senderAccount.value,
            to = if (_recipientEmail.value.isNotBlank()) listOf(_recipientEmail.value.trim()) else listOf("recipient@example.com"),
            subject = _subject.value,
            blocks = _blocks.value
          )
        )
      )
    }
    _isCustomHtmlActive.value = true
  }

  fun disableCustomHtmlMode() {
    _isCustomHtmlActive.value = false
  }

  fun formatCurrentCustomHtml() {
    val current = _customHtmlContent.value
    if (current.isNotBlank()) {
      _customHtmlContent.value = HtmlEmailGenerator.formatHtml(current)
    }
  }

  fun resetCustomHtmlFromBlocks() {
    _customHtmlContent.value = HtmlEmailGenerator.formatHtml(
      HtmlEmailGenerator.generateHtml(
        EmailMessage(
          sender = senderAccount.value,
          to = if (_recipientEmail.value.isNotBlank()) listOf(_recipientEmail.value.trim()) else listOf("recipient@example.com"),
          subject = _subject.value,
          blocks = _blocks.value
        )
      )
    )
    _isCustomHtmlActive.value = true
  }

  fun insertHtmlSnippet(snippet: String) {
    if (_customHtmlContent.value.isBlank()) {
      enableCustomHtmlMode()
    }
    val current = _customHtmlContent.value
    // Insert before closing body or table tag if present, else append
    if (current.contains("</td>\n      </tr>\n    </table>\n  </center>\n</body>")) {
      _customHtmlContent.value = current.replace(
        "</td>\n      </tr>\n    </table>\n  </center>\n</body>",
        "  $snippet\n        </td>\n      </tr>\n    </table>\n  </center>\n</body>"
      )
    } else {
      _customHtmlContent.value = current + "\n" + snippet
    }
    _isCustomHtmlActive.value = true
  }

  fun deleteSentEmail(id: Long) {
    viewModelScope.launch {
      mailDao.deleteSentEmail(id)
    }
  }

  fun clearSentHistory() {
    viewModelScope.launch {
      mailDao.clearSentHistory()
    }
  }

  fun sendToMyInbox() {
    val sender = senderAccount.value
    if (sender.email.isBlank()) {
      _sendState.value = SendState.Error("Please configure your sender email in Account Settings first.")
      return
    }
    _recipientEmail.value = sender.email.trim()
    val currentSubj = _subject.value
    if (currentSubj.isBlank() || currentSubj.startsWith("Test Email")) {
      val timeStamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
      _subject.value = "Test Email ($timeStamp) ✨"
    }
    sendEmail()
  }

  fun sendViaDeviceEmailApp(context: android.content.Context) {
    val currentMsg = getCurrentEmailMessage()
    val targetRecipient = if (_recipientEmail.value.isNotBlank()) _recipientEmail.value.trim() else senderAccount.value.email.trim()
    val plainBody = HtmlEmailGenerator.generatePlainText(currentMsg)

    try {
      val mailtoIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
        data = android.net.Uri.parse("mailto:${targetRecipient.trim()}")
        putExtra(android.content.Intent.EXTRA_SUBJECT, currentMsg.subject)
        putExtra(android.content.Intent.EXTRA_TEXT, plainBody)
        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(android.content.Intent.createChooser(mailtoIntent, "Send via Email App"))
    } catch (_: Exception) {
      try {
        val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
          type = "message/rfc822"
          if (targetRecipient.isNotBlank()) {
            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(targetRecipient))
          }
          putExtra(android.content.Intent.EXTRA_SUBJECT, currentMsg.subject)
          putExtra(android.content.Intent.EXTRA_TEXT, plainBody)
          flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(android.content.Intent.createChooser(fallbackIntent, "Send via Email App"))
      } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Could not open email client: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
      }
    }
  }

  fun addAttachmentDirect(attachment: EmailAttachment) {
    val current = _attachments.value.toMutableList()
    if (current.none { it.uriString == attachment.uriString }) {
      current.add(attachment)
      _attachments.value = current
    }
  }

  fun getCurrentEmailMessage(): EmailMessage {
    return EmailMessage(
      sender = senderAccount.value,
      to = if (_recipientEmail.value.isNotBlank()) listOf(_recipientEmail.value.trim()) else listOf("recipient@example.com"),
      cc = if (_ccEmail.value.isNotBlank()) listOf(_ccEmail.value.trim()) else emptyList(),
      bcc = if (_bccEmail.value.isNotBlank()) listOf(_bccEmail.value.trim()) else emptyList(),
      subject = _subject.value.ifBlank { "Email Preview" },
      blocks = _blocks.value,
      customHtml = if (_isCustomHtmlActive.value && _customHtmlContent.value.isNotBlank()) _customHtmlContent.value else null,
      attachments = _attachments.value
    )
  }
}
