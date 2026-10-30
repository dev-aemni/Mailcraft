package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.ui.components.MailCraftFooter
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import android.widget.Toast
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ui.theme.NeuCard
import com.example.ui.theme.NeuButton
import com.example.ui.theme.NeuInsetCard
import com.example.ui.theme.NeuAccent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockType
import com.example.model.EmailBlock
import com.example.ui.AppTab
import com.example.ui.MailCraftViewModel
import com.example.ui.components.BlockCardWrapper
import com.example.ui.components.ButtonBlockEditor
import com.example.ui.components.EmbedCardBlockEditor
import com.example.ui.components.FooterBlockEditor
import com.example.ui.components.GifImageBlockEditor
import com.example.ui.components.GifPickerSheet
import com.example.ui.components.HeaderBlockEditor
import com.example.ui.components.TextBlockEditor
import com.example.util.HtmlEmailGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
  viewModel: MailCraftViewModel,
  onOpenSettings: () -> Unit
) {
  val context = LocalContext.current
  val sender by viewModel.senderAccount.collectAsState()
  val recipient by viewModel.recipientEmail.collectAsState()
  val cc by viewModel.ccEmail.collectAsState()
  val bcc by viewModel.bccEmail.collectAsState()
  val subject by viewModel.subject.collectAsState()
  val blocks by viewModel.blocks.collectAsState()
  val recentRecipients by viewModel.recentRecipients.collectAsState()

  var showCcBcc by remember { mutableStateOf(false) }
  var activeGifBlockId by remember { mutableStateOf<String?>(null) }
  var showAdvancedOptions by remember { mutableStateOf(false) }

  val attachments by viewModel.attachments.collectAsState()

  val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris ->
    uris.forEach { uri ->
      viewModel.addAttachmentFromUri(uri)
    }
  }

  // GIF picker sheet
  if (activeGifBlockId != null) {
    val block = blocks.find { it.id == activeGifBlockId } as? EmailBlock.GifImage
    GifPickerSheet(
      currentUrl = block?.imageUrl ?: "",
      onDismiss = { activeGifBlockId = null },
      onSelectGif = { url, caption ->
        if (block != null) {
          viewModel.updateBlock(block.copy(imageUrl = url, caption = caption))
        }
      }
    )
  }

  val isDark by viewModel.isAppDarkMode.collectAsState()
  var showAiEmailSheet by remember { mutableStateOf(false) }
  var showAiSubjectSheet by remember { mutableStateOf(false) }
  var aiPromptText by remember { mutableStateOf("") }
  var selectedTone by remember { mutableStateOf("Modern & Bold") }
  var isGeneratingAi by remember { mutableStateOf(false) }
  var aiSubjectSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
  var isSuggestingSubjects by remember { mutableStateOf(false) }

  // 1. AI Email Creator Bottom Sheet
  if (showAiEmailSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showAiEmailSheet = false },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface
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
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("MailCraft AI Creator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
              Text("Generate structured headings, text, CTA & layout", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          IconButton(onClick = { showAiEmailSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tone Pills
        Text("Tone & Style", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Modern & Bold", "Casual & Warm", "Urgent & High-Energy", "Sleek Minimalist", "Professional B2B").forEach { tone ->
            FilterChip(
              selected = selectedTone == tone,
              onClick = { selectedTone = tone },
              label = { Text(tone, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prompt Input
        OutlinedTextField(
          value = aiPromptText,
          onValueChange = { aiPromptText = it },
          label = { Text("What email would you like to create?") },
          placeholder = { Text("e.g., Cyber Monday announcement with 30% discount code CYBER30, bold hero banner, and Shop Now button...") },
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .testTag("ai_email_prompt_input"),
          shape = RoundedCornerShape(14.dp),
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            if (aiPromptText.isBlank()) {
              Toast.makeText(context, "Please enter an email description", Toast.LENGTH_SHORT).show()
              return@Button
            }
            isGeneratingAi = true
            viewModel.generateEmailWithAi(
              prompt = aiPromptText.trim(),
              tone = selectedTone,
              onSuccess = { generated ->
                isGeneratingAi = false
                viewModel.applyGeneratedEmail(generated)
                showAiEmailSheet = false
                Toast.makeText(context, "Generated \"${generated.subject}\"!", Toast.LENGTH_SHORT).show()
              },
              onError = { err ->
                isGeneratingAi = false
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
              }
            )
          },
          enabled = !isGeneratingAi,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("ai_generate_email_submit_button")
        ) {
          if (isGeneratingAi) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI is crafting your email...", fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Complete Email", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // 2. AI Subject Line Suggestions Bottom Sheet
  if (showAiSubjectSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showAiSubjectSheet = false },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface
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
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Subject Line Suggestions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
          IconButton(onClick = { showAiSubjectSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Text(
          "Click any subject line to instantly apply it to your email.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 14.dp)
        )

        if (isSuggestingSubjects) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFF6366F1))
              Spacer(modifier = Modifier.height(10.dp))
              Text("Crafting high open-rate subject lines...", fontSize = 12.sp, color = Color(0xFF6366F1))
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            aiSubjectSuggestions.forEach { subj ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    viewModel.setSubject(subj)
                    showAiSubjectSheet = false
                    Toast.makeText(context, "Subject updated!", Toast.LENGTH_SHORT).show()
                  }
                  .testTag("suggested_subject_item"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = subj,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                  )
                  Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      }
    }
  }


  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Sender Banner
    item {
      NeuCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        isDark = isDark,
        contentPadding = PaddingValues(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF282B30) else MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              val preset = sender.avatarPreset
              val customUrl = sender.avatarUrl
              if (preset == "avatar_custom" && customUrl.isNotBlank()) {
                AsyncImage(
                  model = customUrl,
                  contentDescription = "PFP",
                  modifier = Modifier.size(38.dp).clip(CircleShape),
                  contentScale = ContentScale.Crop
                )
              } else if (preset == "avatar_monogram") {
                val letter = (sender.displayName.ifBlank { sender.email }).take(1).uppercase()
                Text(
                  text = letter.ifBlank { "U" },
                  fontWeight = FontWeight.Black,
                  fontSize = 15.sp,
                  color = if (isDark) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary
                )
              } else if (preset == "avatar_rocket") {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = if (isDark) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              } else if (preset == "avatar_code") {
                Icon(Icons.Default.Code, contentDescription = null, tint = if (isDark) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              } else if (preset == "avatar_heart") {
                Text("❤️", fontSize = 16.sp)
              } else if (preset == "avatar_sparkles") {
                Text("✨", fontSize = 16.sp)
              } else {
                Text("🟣", fontSize = 16.sp)
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "From: ${sender.displayName.ifBlank { "Sender Account" }}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
              )
              Text(
                text = sender.email.ifBlank { "No email configured (tap to set)" },
                style = MaterialTheme.typography.bodySmall,
                color = if (sender.email.isBlank()) MaterialTheme.colorScheme.error else (if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          // Password Status Badge
          Row(verticalAlignment = Alignment.CenterVertically) {
            val hasPassword = sender.appPassword.isNotBlank()
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (hasPassword) (if (isDark) Color(0xFF14532D) else Color(0xFFDCFCE7)) else (if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  if (hasPassword) Icons.Default.CheckCircle else Icons.Default.Key,
                  contentDescription = null,
                  tint = if (hasPassword) (if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)) else (if (isDark) Color(0xFFFCD34D) else Color(0xFFD97706)),
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (hasPassword) "Password Ready" else "Need Password",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (hasPassword) (if (isDark) Color(0xFFBBF7D0) else Color(0xFF15803D)) else (if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309))
                )
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Sender Account", modifier = Modifier.size(18.dp), tint = if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569))
            }
          }
        }
      }
    }

    // 2. Recipient & Subject Card
    item {
      NeuCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        isDark = isDark,
        contentPadding = PaddingValues(16.dp)
      ) {
        Column {
          // Recipient Field ("that person email")
          OutlinedTextField(
            value = recipient,
            onValueChange = { viewModel.setRecipientEmail(it) },
            label = { Text("To (Recipient Email)") },
            placeholder = { Text("friend@example.com") },
            singleLine = true,
            leadingIcon = {
              Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
              Text(
                text = if (showCcBcc) "Hide CC" else "+ CC/BCC",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                  .clickable { showCcBcc = !showCcBcc }
                  .padding(end = 12.dp)
              )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("recipient_email_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Recent recipients chip list
          if (recentRecipients.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                "Recent:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
              )
              recentRecipients.take(4).forEach { email ->
                FilterChip(
                  selected = recipient == email,
                  onClick = { viewModel.setRecipientEmail(email) },
                  label = { Text(email, fontSize = 11.sp) }
                )
              }
            }
          }

          // Expandable CC/BCC
          AnimatedVisibility(visible = showCcBcc) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
              OutlinedTextField(
                value = cc,
                onValueChange = { viewModel.setCcEmail(it) },
                label = { Text("Cc (Carbon Copy)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = bcc,
                onValueChange = { viewModel.setBccEmail(it) },
                label = { Text("Bcc (Blind Carbon Copy)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Subject Field
          OutlinedTextField(
            value = subject,
            onValueChange = { viewModel.setSubject(it) },
            label = { Text("Subject Line") },
            placeholder = { Text("Subject...") },
            singleLine = true,
            leadingIcon = {
              Icon(Icons.Default.Title, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            },
            trailingIcon = {
              IconButton(
                onClick = {
                  isSuggestingSubjects = true
                  showAiSubjectSheet = true
                  viewModel.suggestSubjectLinesWithAi(
                    onSuccess = { list ->
                      aiSubjectSuggestions = list
                      isSuggestingSubjects = false
                    },
                    onError = { err ->
                      isSuggestingSubjects = false
                      Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                  )
                },
                modifier = Modifier.testTag("ai_suggest_subject_button")
              ) {
                Icon(
                  Icons.Default.AutoAwesome,
                  contentDescription = "Suggest Subject with AI",
                  tint = Color(0xFF6366F1),
                  modifier = Modifier.size(20.dp)
                )
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("email_subject_input"),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // ✨ MailCraft AI Email Creator Card
    item {
      NeuCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showAiEmailSheet = true }
          .testTag("open_ai_email_creator_card"),
        cornerRadius = 22.dp,
        isDark = isDark,
        contentPadding = PaddingValues(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF6366F1)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Create Email with AI",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isDark) Color(0xFFE0E7FF) else Color(0xFF1E1B4B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF6366F1))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "SMART",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              Text(
                text = "Describe topic: AI writes subject, catchy text & layout",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
              )
            }
          }
          Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 3. Advance Options Expandable Box (Keeps UI clean & uncluttered)
    item {
      NeuCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showAdvancedOptions = !showAdvancedOptions }
          .testTag("advance_options_toggle"),
        cornerRadius = 22.dp,
        isDark = isDark,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF282B30) else Color(0xFFE2E8F0)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = if (showAdvancedOptions) "Advance Options ↑" else "Advance Options ↓",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                )
                if (attachments.isNotEmpty()) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "${attachments.size} attached",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF2563EB)
                    )
                  }
                }
              }
              Text(
                text = "Attachments (${attachments.size}), HTML Studio code editor",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
              )
            }
          }
          Icon(
            if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
          )
        }
      }
    }

    // Expanded Advance Features
    if (showAdvancedOptions) {
      // Attachments Card (PDFs, Media & Documents)
      item {
        NeuCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("attachments_card"),
          cornerRadius = 22.dp,
          isDark = isDark,
          contentPadding = PaddingValues(16.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF282B30) else MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "Attachments",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                    )
                    if (attachments.isNotEmpty()) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = "${attachments.size}",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        )
                      }
                    }
                  }
                  Text(
                    text = "PDF documents, images & files",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              // Quick file picker buttons
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                  onClick = {
                    documentPickerLauncher.launch(
                      arrayOf(
                        "application/pdf",
                        "image/*",
                        "text/plain",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "*/*"
                      )
                    )
                  },
                  modifier = Modifier
                    .height(36.dp)
                    .testTag("add_attachment_button"),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add File", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }

            // Total Size Tracker & Warnings
            val totalBytes = attachments.sumOf { it.sizeBytes }
            val totalMb = totalBytes / (1024.0 * 1024.0)
            val isExceedingGmailLimit = totalBytes > 25 * 1024 * 1024

            if (attachments.isNotEmpty()) {
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isExceedingGmailLimit) Color(0xFFFEE2E2) else (if (isDark) Color(0xFF282B30) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                  )
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isExceedingGmailLimit) {
                    Icon(
                      Icons.Default.Warning,
                      contentDescription = null,
                      tint = Color(0xFFDC2626),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = if (isExceedingGmailLimit) {
                      "Total ${String.format(java.util.Locale.US, "%.1f", totalMb)} MB • Exceeds 25 MB Gmail SMTP limit"
                    } else {
                      "Total payload: ${String.format(java.util.Locale.US, "%.2f", totalMb)} MB / 25 MB max"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isExceedingGmailLimit) Color(0xFFDC2626) else (if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurfaceVariant)
                  )
                }

                TextButton(
                  onClick = { viewModel.clearAttachments() },
                  modifier = Modifier.height(28.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                  Text("Clear All", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Attachment item chips
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                attachments.forEach { att ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF222428) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isDark) Color(0xFF333740) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("attachment_item_${att.id}")
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                              when {
                                att.isPdf -> Color(0xFFFEE2E2)
                                att.isImage -> Color(0xFFE0E7FF)
                                else -> (if (isDark) Color(0xFF2E323A) else Color(0xFFF1F5F9))
                              }
                            ),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = when {
                              att.isPdf -> Icons.Default.PictureAsPdf
                              att.isImage -> Icons.Default.Image
                              else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = when {
                              att.isPdf -> Color(0xFFDC2626)
                              att.isImage -> Color(0xFF4F46E5)
                              else -> (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                            },
                            modifier = Modifier.size(18.dp)
                          )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                          Text(
                            text = att.fileName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                            maxLines = 1
                          )
                          Text(
                            text = "${att.formattedSize} • ${att.mimeType.substringAfterLast('/')}",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }

                      IconButton(
                        onClick = { viewModel.removeAttachment(att.id) },
                        modifier = Modifier
                          .size(36.dp)
                          .testTag("remove_attachment_${att.id}")
                      ) {
                        Icon(
                          Icons.Default.Close,
                          contentDescription = "Remove attachment",
                          modifier = Modifier.size(16.dp),
                          tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // HTML Workstation Quick Switch Card
      item {
        NeuCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              viewModel.enableCustomHtmlMode()
              viewModel.setActiveTab(AppTab.HTML_WORKSTATION)
            }
            .testTag("switch_to_html_workstation_card"),
          cornerRadius = 22.dp,
          isDark = isDark,
          contentPadding = PaddingValues(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Terminal,
                  contentDescription = null,
                  tint = Color(0xFF7C3AED),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Switch to HTML Studio",
                    color = if (isDark) Color(0xFFDDD6FE) else Color(0xFF1E1B4B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = "CODE MODE",
                      color = Color(0xFF7C3AED),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
                Text(
                  text = "Live syntax highlighting, snippets, line numbers & raw control",
                  color = if (isDark) Color(0xFFA78BFA) else Color(0xFF5B21B6),
                  fontSize = 11.sp
                )
              }
            }
            Icon(
              Icons.Default.ChevronRight,
              contentDescription = null,
              tint = Color(0xFF7C3AED),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // 3. Add Custom Blocks Bar
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Add Email Elements & Media",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AddBlockChip(
            label = "CTA Button",
            icon = Icons.Default.SmartButton,
            tag = "add_button_chip"
          ) {
            viewModel.addBlock(BlockType.BUTTON)
          }

          AddBlockChip(
            label = "GIF / Image",
            icon = Icons.Default.Gif,
            tag = "add_gif_chip"
          ) {
            viewModel.addBlock(BlockType.GIF_IMAGE)
          }

          AddBlockChip(
            label = "Embed Card",
            icon = Icons.Default.CreditCard,
            tag = "add_embed_chip"
          ) {
            viewModel.addBlock(BlockType.EMBED_CARD)
          }

          AddBlockChip(
            label = "Text Paragraph",
            icon = Icons.Default.TextFields,
            tag = "add_text_chip"
          ) {
            viewModel.addBlock(BlockType.TEXT)
          }

          AddBlockChip(
            label = "Headline",
            icon = Icons.Default.Title,
            tag = "add_header_chip"
          ) {
            viewModel.addBlock(BlockType.HEADER)
          }

          AddBlockChip(
            label = "Divider Line",
            icon = Icons.Default.HorizontalRule,
            tag = "add_divider_chip"
          ) {
            viewModel.addBlock(BlockType.DIVIDER)
          }

          AddBlockChip(
            label = "Social Links",
            icon = Icons.Default.Share,
            tag = "add_social_chip"
          ) {
            viewModel.addBlock(BlockType.SOCIAL_LINKS)
          }
        }
      }
    }

    // 4. List of Blocks
    itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
      when (block) {
        is EmailBlock.Header -> {
          BlockCardWrapper(
            title = "Header (${block.text.take(18)}...)",
            icon = Icons.Default.Title,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            HeaderBlockEditor(block) { viewModel.updateBlock(it) }
          }
        }

        is EmailBlock.Text -> {
          BlockCardWrapper(
            title = "Text Paragraph",
            icon = Icons.Default.TextFields,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            TextBlockEditor(block) { viewModel.updateBlock(it) }
          }
        }

        is EmailBlock.Button -> {
          BlockCardWrapper(
            title = "Button: \"${block.label}\"",
            icon = Icons.Default.SmartButton,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            ButtonBlockEditor(block) { viewModel.updateBlock(it) }
          }
        }

        is EmailBlock.GifImage -> {
          BlockCardWrapper(
            title = "GIF / Media (${block.caption.ifBlank { "Media" }})",
            icon = Icons.Default.Gif,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            GifImageBlockEditor(
              block = block,
              onOpenGifPicker = { activeGifBlockId = block.id },
              onUpdate = { viewModel.updateBlock(it) }
            )
          }
        }

        is EmailBlock.EmbedCard -> {
          BlockCardWrapper(
            title = "Embed Card: \"${block.title}\"",
            icon = Icons.Default.CreditCard,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            EmbedCardBlockEditor(block) { viewModel.updateBlock(it) }
          }
        }

        is EmailBlock.Divider -> {
          BlockCardWrapper(
            title = "Divider Line",
            icon = Icons.Default.HorizontalRule,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            Text("A subtle separating line between content sections.")
          }
        }

        is EmailBlock.SocialLinks -> {
          BlockCardWrapper(
            title = "Social Channels",
            icon = Icons.Default.Share,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            Text("Website, GitHub, and Twitter badges rendered at the bottom.")
          }
        }

        is EmailBlock.Footer -> {
          BlockCardWrapper(
            title = "Email Signature & Footer",
            icon = Icons.Default.Edit,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.size - 1,
            onMoveUp = { viewModel.moveBlockUp(block.id) },
            onMoveDown = { viewModel.moveBlockDown(block.id) },
            onDuplicate = { viewModel.duplicateBlock(block.id) },
            onDelete = { viewModel.removeBlock(block.id) }
          ) {
            FooterBlockEditor(block) { viewModel.updateBlock(it) }
          }
        }
      }
    }

    // 5. Action Dispatch Footer
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Ready to Dispatch?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (recipient.isNotBlank()) "Sending to $recipient" else "Enter recipient email above",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = {
                  viewModel.saveCurrentDraft()
                  android.widget.Toast.makeText(context, "Draft saved to Room!", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("save_draft_composer_button")
              ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Draft", fontSize = 12.sp)
              }
              OutlinedButton(
                onClick = { viewModel.setActiveTab(AppTab.PREVIEW) },
                modifier = Modifier.testTag("open_preview_button")
              ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Preview", fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Primary Send Button
          Button(
            onClick = { viewModel.sendEmail() },
            enabled = recipient.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("send_email_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Send via Gmail SMTP",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Fallback / Alternative: Send via Android Gmail App (Sleek and compact)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                val message = viewModel.getCurrentEmailMessage()
                val plainText = HtmlEmailGenerator.generatePlainText(message)
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                  data = Uri.parse("mailto:${recipient.trim()}")
                  putExtra(Intent.EXTRA_SUBJECT, subject)
                  putExtra(Intent.EXTRA_TEXT, plainText)
                }
                try {
                  context.startActivity(Intent.createChooser(intent, "Send Email"))
                } catch (_: Exception) {}
              },
              shape = RoundedCornerShape(20.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
              modifier = Modifier
                .height(34.dp)
                .testTag("open_gmail_app_button")
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(5.dp))
              Text("Send via Gmail App", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }

    item {
      MailCraftFooter(isDark = isDark)
    }
  }
}

@Composable
fun AddBlockChip(
  label: String,
  icon: ImageVector,
  tag: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag(tag)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
      Spacer(modifier = Modifier.width(6.dp))
      Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.width(4.dp))
      Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
    }
  }
}
