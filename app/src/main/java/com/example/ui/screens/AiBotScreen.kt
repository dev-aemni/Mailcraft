package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import com.example.ai.AiKeyProvider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AVAILABLE_AI_MODELS
import com.example.ai.AiChatMessage
import com.example.ai.AiModelOption
import com.example.ui.MailCraftViewModel
import com.example.ui.theme.NeuAccent
import com.example.ui.theme.NeuButton
import com.example.ui.theme.NeuCard
import com.example.ui.theme.NeuDarkBg
import com.example.ui.theme.NeuDarkTextMuted
import com.example.ui.theme.NeuDarkTextPrimary
import com.example.ui.theme.NeuDarkTextSecondary
import com.example.ui.theme.NeuIconButton
import com.example.ui.theme.NeuInsetCard
import com.example.ui.theme.NeuLightBg
import com.example.ui.theme.NeuLightTextMuted
import com.example.ui.theme.NeuLightTextPrimary
import com.example.ui.theme.NeuLightTextSecondary
import com.example.ui.theme.NeuPill
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiBotScreen(viewModel: MailCraftViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val isDark by viewModel.isAppDarkMode.collectAsState()
  val chatMessages by viewModel.aiChatMessages.collectAsState()
  val isThinking by viewModel.isAiThinking.collectAsState()
  val aiError by viewModel.aiError.collectAsState()
  val selectedModel by viewModel.selectedAiModel.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var showModelSheet by remember { mutableStateOf(false) }
  var showKeyDialog by remember { mutableStateOf(false) }
  var customKeyInput by remember { mutableStateOf(AiKeyProvider.getApiKey(context)) }

  val listState = rememberLazyListState()

  // Scroll to bottom when new messages arrive
  LaunchedEffect(chatMessages.size, isThinking) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  // Also scroll to bottom when keyboard opens so latest message is visible
  val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
  LaunchedEffect(isImeVisible) {
    if (isImeVisible && chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  val textPrimary = if (isDark) NeuDarkTextPrimary else NeuLightTextPrimary
  val textSecondary = if (isDark) NeuDarkTextSecondary else NeuLightTextSecondary
  val textMuted = if (isDark) NeuDarkTextMuted else NeuLightTextMuted
  val bgScreen = if (isDark) NeuDarkBg else NeuLightBg

  // Model Picker Sheet
  if (showModelSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showModelSheet = false },
      sheetState = sheetState,
      containerColor = if (isDark) Color(0xFF1E2024) else Color(0xFFF8FAFC)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 32.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = NeuAccent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Select Intelligence Engine",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = textPrimary
            )
          }
          IconButton(onClick = { showModelSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary)
          }
        }

        Text(
          text = "Powered by high-parameter language models tuned specifically for email architecture & deliverability.",
          fontSize = 12.sp,
          color = textSecondary,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          AVAILABLE_AI_MODELS.forEach { model ->
            val isSelected = model.id == selectedModel.id
            NeuCard(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.selectAiModel(model)
                  showModelSheet = false
                  Toast.makeText(context, "Switched engine to ${model.displayName}", Toast.LENGTH_SHORT).show()
                }
                .testTag("ai_model_option_${model.id}"),
              cornerRadius = 18.dp,
              isDark = isDark,
              contentPadding = PaddingValues(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = model.displayName,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (model.isRecommended) Color(0xFFDCFCE7) else Color(0xFFEDE9FE))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = model.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (model.isRecommended) Color(0xFF15803D) else Color(0xFF6D28D9)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = model.description,
                    fontSize = 12.sp,
                    color = textSecondary,
                    lineHeight = 16.sp
                  )
                }

                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(NeuAccent),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // AI Key Configuration Dialog
  if (showKeyDialog) {
    val hasKey = AiKeyProvider.hasValidKey(context)
    AlertDialog(
      onDismissRequest = { showKeyDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Key, contentDescription = null, tint = NeuAccent)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "AI Access Key", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = if (hasKey) "Status: Access Key Active ✅" else "Status: No Access Key Detected ⚠️",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (hasKey) Color(0xFF10B981) else Color(0xFFEF4444)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Enter your OpenRouter API key (sk-or-v1-...). Free models will use this key to generate code and copy.",
            fontSize = 12.sp,
            color = textSecondary,
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = customKeyInput,
            onValueChange = { customKeyInput = it },
            modifier = Modifier.fillMaxWidth().testTag("ai_key_input_field"),
            placeholder = { Text("Paste sk-or-v1-...", fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            AiKeyProvider.saveCustomKey(context, customKeyInput)
            showKeyDialog = false
            Toast.makeText(context, "AI Access Key saved successfully!", Toast.LENGTH_SHORT).show()
          }
        ) {
          Text("Save Key", color = NeuAccent, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showKeyDialog = false }) {
          Text("Cancel", color = textSecondary)
        }
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(bgScreen)
      .imePadding()
  ) {
    // Top AI Header Card
    NeuCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      cornerRadius = 24.dp,
      isDark = isDark,
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(NeuAccent.copy(alpha = 0.15f))
              .border(1.dp, NeuAccent.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeuAccent, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "MailCraft AI",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = textPrimary,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF10B981))
              )
            }
            Text(
              text = "Email copywriting, HTML & SMTP support",
              fontSize = 11.sp,
              color = textSecondary
            )
          }
        }

        // Engine selector badge button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0xFF282B31) else Color(0xFFE4E9EE))
            .clickable { showModelSheet = true }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("ai_engine_selector_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = selectedModel.displayName.take(12),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeuAccent
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = NeuAccent, modifier = Modifier.size(16.dp))
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // AI Key configuration button
        NeuIconButton(
          onClick = {
            customKeyInput = AiKeyProvider.getApiKey(context)
            showKeyDialog = true
          },
          isDark = isDark,
          modifier = Modifier.size(34.dp).testTag("ai_key_config_button")
        ) {
          val hasKey = AiKeyProvider.hasValidKey(context)
          Icon(
            Icons.Default.Key,
            contentDescription = "AI Key Configuration",
            tint = if (hasKey) NeuAccent else Color(0xFFEF4444),
            modifier = Modifier.size(17.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Clear chat icon
        NeuIconButton(
          onClick = {
            viewModel.clearAiChatHistory()
            Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
          },
          isDark = isDark,
          modifier = Modifier.size(34.dp).testTag("clear_ai_chat_button")
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = textSecondary, modifier = Modifier.size(17.dp))
        }
      }
    }

    // Quick Prompts Chips Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 14.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickPromptChip(
        label = "🔑 Fix Gmail App Password",
        isDark = isDark,
        onClick = {
          viewModel.sendAiChatMessage("How do I set up a 16-character Google App Password for Gmail SMTP, and why does my normal password give error 535?")
        }
      )
      QuickPromptChip(
        label = "🛡️ Avoid Spam & Boost Inbox",
        isDark = isDark,
        onClick = {
          viewModel.sendAiChatMessage("What are the top 7 deliverability tips to ensure my marketing emails land in the primary Inbox instead of Spam or Promotions?")
        }
      )
      QuickPromptChip(
        label = "⚡ Generate HTML Table",
        isDark = isDark,
        onClick = {
          viewModel.sendAiChatMessage("Write clean, bulletproof HTML email code for a 3-column pricing table with 600px width and mobile responsiveness.")
        }
      )
      QuickPromptChip(
        label = "🎁 Black Friday Promo Email",
        isDark = isDark,
        onClick = {
          viewModel.sendAiChatMessage("Draft a high-converting Black Friday announcement email with strong urgency, 30% discount code, and responsive HTML layout.")
        }
      )
      QuickPromptChip(
        label = "✨ Cold B2B Outreach",
        isDark = isDark,
        onClick = {
          viewModel.sendAiChatMessage("Write a personalized 3-sentence cold outreach email for SaaS founders with a clear soft call-to-action.")
        }
      )
    }

    // Status / Notice Banner (Sanitized, never shows internal provider/model errors)
    if (aiError != null) {
      val rawErr = aiError.orEmpty()
      val isTechnicalOrRouter = rawErr.contains("OpenRouter", ignoreCase = true) ||
        rawErr.contains("404") ||
        rawErr.contains("claude", ignoreCase = true) ||
        rawErr.contains("provider", ignoreCase = true)
      val displayError = if (isTechnicalOrRouter) {
        "AI engine is temporarily busy. Automatically switching to next free engine..."
      } else {
        rawErr
      }

      NeuInsetCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 4.dp),
        cornerRadius = 14.dp,
        isDark = isDark,
        contentPadding = PaddingValues(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = NeuAccent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = displayError,
              color = textPrimary,
              fontSize = 12.sp,
              lineHeight = 15.sp
            )
          }
          IconButton(onClick = { viewModel.clearAiError() }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = textSecondary, modifier = Modifier.size(14.dp))
          }
        }
      }
    }

    // Messages List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 14.dp),
      contentPadding = PaddingValues(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(chatMessages, key = { it.id }) { message ->
        ChatMessageRow(
          message = message,
          isDark = isDark,
          onApplyHtml = { code ->
            viewModel.applyHtmlToWorkstation(code)
            Toast.makeText(context, "Loaded into HTML Workstation!", Toast.LENGTH_SHORT).show()
          },
          onCopy = { code ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Copied Code", code))
            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
          }
        )
      }

      if (isThinking) {
        item {
          ThinkingIndicatorRow(isDark = isDark, modelName = selectedModel.displayName)
        }
      }
    }

    // Bottom Input Field Bar
    NeuCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      cornerRadius = 24.dp,
      isDark = isDark,
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        NeuInsetCard(
          modifier = Modifier.weight(1f),
          cornerRadius = 18.dp,
          isDark = isDark,
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
          BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("ai_chat_input_field"),
            textStyle = TextStyle(
              color = textPrimary,
              fontSize = 14.sp
            ),
            cursorBrush = SolidColor(NeuAccent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
              onSend = {
                if (inputText.isNotBlank() && !isThinking) {
                  val msg = inputText.trim()
                  inputText = ""
                  viewModel.sendAiChatMessage(msg)
                }
              }
            ),
            decorationBox = { innerTextField ->
              if (inputText.isEmpty()) {
                Text(
                  text = "Ask MailCraft AI (code, deliverability, copy)...",
                  color = textMuted,
                  fontSize = 13.sp
                )
              }
              innerTextField()
            }
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        NeuIconButton(
          onClick = {
            if (inputText.isNotBlank() && !isThinking) {
              val msg = inputText.trim()
              inputText = ""
              viewModel.sendAiChatMessage(msg)
            }
          },
          enabled = inputText.isNotBlank() && !isThinking,
          isDark = isDark,
          modifier = Modifier
            .size(44.dp)
            .testTag("ai_chat_send_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (inputText.isNotBlank() && !isThinking) NeuAccent else textMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun QuickPromptChip(
  label: String,
  isDark: Boolean,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(14.dp)
  val bgColor = if (isDark) Color(0xFF26292E) else Color(0xFFE2E7ED)
  val textColor = if (isDark) NeuDarkTextSecondary else NeuLightTextSecondary

  Box(
    modifier = Modifier
      .clip(shape)
      .background(bgColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 7.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = textColor
    )
  }
}

@Composable
fun ChatMessageRow(
  message: AiChatMessage,
  isDark: Boolean,
  onApplyHtml: (String) -> Unit,
  onCopy: (String) -> Unit
) {
  val isUser = message.role == "user"
  val textPrimary = if (isDark) NeuDarkTextPrimary else NeuLightTextPrimary
  val textSecondary = if (isDark) NeuDarkTextSecondary else NeuLightTextSecondary

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(NeuAccent)
          .padding(6.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      modifier = Modifier.widthIn(max = 300.dp),
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
      if (isUser) {
        // User bubble: sleek accent container
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
            .background(NeuAccent)
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Text(
            text = message.content,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 19.sp
          )
        }
      } else {
        // Assistant bubble: Neumorphic card with extracted code preview
        NeuCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 20.dp,
          isDark = isDark,
          contentPadding = PaddingValues(14.dp)
        ) {
          Column {
            Text(
              text = message.content,
              color = textPrimary,
              fontSize = 13.sp,
              lineHeight = 19.sp
            )

            // Extracted code block action card
            if (message.extractedCode != null && message.extractedCode.isNotBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              NeuInsetCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                isDark = isDark,
                contentPadding = PaddingValues(10.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        if (message.isCodeHtml) Icons.Default.Code else Icons.Default.Code,
                        contentDescription = null,
                        tint = NeuAccent,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = if (message.isCodeHtml) "HTML Email Snippet" else "Code Block",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeuAccent
                      )
                    }

                    Text(
                      text = "${message.extractedCode.lines().size} lines",
                      fontSize = 10.sp,
                      color = textSecondary
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  // Monospace snippet preview
                  Text(
                    text = message.extractedCode.take(220) + if (message.extractedCode.length > 220) "\n..." else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                    lineHeight = 14.sp,
                    maxLines = 6
                  )

                  Spacer(modifier = Modifier.height(10.dp))

                  // Action Buttons for Code
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    if (message.isCodeHtml) {
                      NeuButton(
                        onClick = { onApplyHtml(message.extractedCode) },
                        isPrimary = true,
                        cornerRadius = 12.dp,
                        isDark = isDark,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("insert_html_button")
                      ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in Studio", fontSize = 11.sp)
                      }
                    }

                    NeuButton(
                      onClick = { onCopy(message.extractedCode) },
                      isPrimary = false,
                      cornerRadius = 12.dp,
                      isDark = isDark,
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                      modifier = Modifier.weight(1f).testTag("copy_code_button")
                    ) {
                      Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Copy Code", fontSize = 11.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ThinkingIndicatorRow(isDark: Boolean, modelName: String) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(NeuAccent)
        .padding(6.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
    Spacer(modifier = Modifier.width(8.dp))
    NeuCard(
      cornerRadius = 16.dp,
      isDark = isDark,
      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(NeuAccent.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "MailCraft AI is formulating response...",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = NeuAccent.copy(alpha = alpha)
        )
      }
    }
  }
}
