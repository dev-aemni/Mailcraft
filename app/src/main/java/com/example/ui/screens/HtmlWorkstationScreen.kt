package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import com.example.network.LocalPreviewServer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardTab
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.EmojiEmotions
import android.util.Base64
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.model.ClientDarkMode
import com.example.model.EmailAttachment
import com.example.model.EmailBlock
import com.example.model.EmailMessage
import com.example.model.TemplatePresets
import com.example.ui.AppTab
import com.example.ui.MailCraftViewModel
import com.example.ui.components.HtmlSyntaxVisualTransformation
import com.example.util.HtmlEmailGenerator

// Spck IDE color theme - Clean & Crisp Light Material 3 Styling
val SpckDarkBg = Color(0xFFF8FAFC)
val SpckTopBarBg = Color(0xFFF1F5F9)
val SpckGutterBg = Color(0xFFF1F5F9)
val SpckTabBg = Color(0xFFFFFFFF)
val SpckBorder = Color(0xFFE2E8F0)
val SpckTextGutter = Color(0xFF64748B)
val SpckGreenRun = Color(0xFF10B981) // Modern emerald green run button
val SpckHtml5Red = Color(0xFFE44D26) // Official HTML5 badge red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlWorkstationScreen(viewModel: MailCraftViewModel) {
  val context = LocalContext.current
  val customHtml by viewModel.customHtmlContent.collectAsState()
  val recipient by viewModel.recipientEmail.collectAsState()
  val subject by viewModel.subject.collectAsState()
  val sender by viewModel.senderAccount.collectAsState()
  val clientDarkMode by viewModel.clientDarkMode.collectAsState()
  val attachments by viewModel.attachments.collectAsState()
  val isDark by viewModel.isAppDarkMode.collectAsState()

  val wsDarkBg = if (isDark) Color(0xFF16181B) else Color(0xFFF8FAFC)
  val wsTopBarBg = if (isDark) Color(0xFF1E2024) else Color(0xFFF1F5F9)
  val wsGutterBg = if (isDark) Color(0xFF1A1C20) else Color(0xFFF1F5F9)
  val wsTabBg = if (isDark) Color(0xFF282B31) else Color(0xFFFFFFFF)
  val wsBorder = if (isDark) Color(0xFF2C3038) else Color(0xFFE2E8F0)
  val wsTextGutter = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
  val wsCodeColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
  val wsTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E1B4B)
  val wsSubtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
  val wsModalBg = if (isDark) Color(0xFF1E2024) else Color.White
  val wsCardBg = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)

  // Make sure custom HTML is populated on first entrance
  LaunchedEffect(Unit) {
    if (customHtml.isBlank()) {
      viewModel.enableCustomHtmlMode()
    }
  }

  var textFieldValue by remember {
    mutableStateOf(TextFieldValue(customHtml, TextRange(customHtml.length)))
  }

  // Synchronize when customHtml changes externally (e.g. templates or format)
  LaunchedEffect(customHtml) {
    if (textFieldValue.text != customHtml) {
      textFieldValue = textFieldValue.copy(text = customHtml)
    }
  }

  val customTemplates by viewModel.customTemplates.collectAsState()
  var showPreviewModal by remember { mutableStateOf(false) }
  val previewServer = remember { LocalPreviewServer() }
  var activePreviewPort by remember { mutableStateOf(0) }

  // Ephemeral random-port local HTTP server for real-time index.html preview
  LaunchedEffect(showPreviewModal) {
    if (showPreviewModal) {
      val htmlToServe = textFieldValue.text
      activePreviewPort = previewServer.start { htmlToServe }
    } else {
      previewServer.stop()
      activePreviewPort = 0
    }
  }

  // Ensure server socket closes immediately when workstation leaves or preview closes
  DisposableEffect(Unit) {
    onDispose {
      previewServer.stop()
    }
  }

  var pendingWsExportJson by remember { mutableStateOf<String?>(null) }
  var pendingWsExportName by remember { mutableStateOf("mailcraft_template.json") }

  val wsExportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    uri?.let { destUri ->
      val json = pendingWsExportJson ?: return@let
      val success = viewModel.writeJsonToUri(context, destUri, json)
      if (success) {
        Toast.makeText(context, "✅ Template JSON exported successfully!", Toast.LENGTH_LONG).show()
      } else {
        Toast.makeText(context, "Failed to write JSON.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val wsImportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let { sourceUri ->
      val json = viewModel.readJsonFromUri(context, sourceUri)
      if (!json.isNullOrBlank()) {
        viewModel.importTemplatesFromJson(json) { count, msg ->
          Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
      } else {
        Toast.makeText(context, "Failed to read JSON file.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var showTemplatesModal by remember { mutableStateOf(false) }
  var showVariablesModal by remember { mutableStateOf(false) }
  var showSaveTemplateDialog by remember { mutableStateOf(false) }
  var newTemplateName by remember { mutableStateOf("") }
  var newTemplateEmoji by remember { mutableStateOf("📄") }
  var templatesTab by remember { mutableStateOf(0) }
  var showMenuDropdown by remember { mutableStateOf(false) }
  var showSearchOverlay by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var isPreviewMobileViewport by remember { mutableStateOf(false) }
  var showAssetsModal by remember { mutableStateOf(false) }
  var assetModalTab by remember { mutableStateOf(0) }
  var showAiModal by remember { mutableStateOf(false) }
  var aiModalTab by remember { mutableStateOf(0) }
  var aiGenPrompt by remember { mutableStateOf("") }
  var aiGenStyle by remember { mutableStateOf("Modern & Responsive") }
  var isAiWorking by remember { mutableStateOf(false) }

  val insertTextAtCursor: (String) -> Unit = { textToInsert ->
    insertChar(textFieldValue, textToInsert) {
      textFieldValue = it
      viewModel.setCustomHtml(it.text)
    }
  }

  // File pickers for Images, PDFs, and HTML
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      try {
        val resolver = context.contentResolver
        var fileName = "image.png"
        var fileSize = 0L
        resolver.query(uri, null, null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIdx >= 0) fileName = cursor.getString(nameIdx) ?: "image.png"
            if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
          }
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
        val mimeType = resolver.getType(uri) ?: "image/png"
        val base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val dataUri = "data:$mimeType;base64,$base64Data"

        viewModel.addAttachmentDirect(
          EmailAttachment(
            fileName = fileName,
            uriString = uri.toString(),
            mimeType = mimeType,
            sizeBytes = fileSize
          )
        )

        val imgSnippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 16px 0;"><tr><td align="center"><img src="$dataUri" alt="${fileName.replace("\"", "")}" style="max-width: 100%; height: auto; border-radius: 8px; display: block; border: 0;" /></td></tr></table>"""
        insertTextAtCursor("\n$imgSnippet\n")
        Toast.makeText(context, "Imported \"$fileName\" & inserted into HTML!", Toast.LENGTH_SHORT).show()
        showAssetsModal = false
      } catch (e: Exception) {
        Toast.makeText(context, "Failed to import image: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
      }
    }
  }

  val pdfPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      try {
        val resolver = context.contentResolver
        var fileName = "document.pdf"
        var fileSize = 0L
        resolver.query(uri, null, null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIdx >= 0) fileName = cursor.getString(nameIdx) ?: "document.pdf"
            if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
          }
        }
        val formattedSize = if (fileSize < 1024) "${fileSize} B" else if (fileSize < 1024 * 1024) "${fileSize / 1024} KB" else "${fileSize / (1024 * 1024)} MB"
        viewModel.addAttachmentDirect(
          EmailAttachment(
            fileName = fileName,
            uriString = uri.toString(),
            mimeType = "application/pdf",
            sizeBytes = fileSize
          )
        )

        val pdfSnippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 16px 0; background-color: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px;"><tr><td style="padding: 16px;"><table role="presentation" border="0" cellpadding="0" cellspacing="0"><tr><td width="36" valign="middle"><span style="font-size: 26px;">📄</span></td><td style="padding-left: 12px;" valign="middle"><div style="font-family: sans-serif; font-size: 14px; font-weight: 700; color: #1E293B;">${fileName.replace("\"", "")}</div><div style="font-family: sans-serif; font-size: 12px; color: #64748B;">PDF Attachment included • $formattedSize</div></td></tr></table></td></tr></table>"""
        insertTextAtCursor("\n$pdfSnippet\n")
        Toast.makeText(context, "Imported PDF \"$fileName\" & attached to email!", Toast.LENGTH_SHORT).show()
        showAssetsModal = false
      } catch (e: Exception) {
        Toast.makeText(context, "Failed to import PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
      }
    }
  }

  val htmlFilePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      try {
        val resolver = context.contentResolver
        val content = resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
        if (content.isNotBlank()) {
          val formatted = HtmlEmailGenerator.formatHtml(content)
          viewModel.setCustomHtml(formatted)
          textFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
          Toast.makeText(context, "HTML file imported into editor!", Toast.LENGTH_SHORT).show()
          showAssetsModal = false
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Failed to read HTML: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
      }
    }
  }

  fun insertTextAtCursor(textToInsert: String) {
    val sel = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
    val prefix = textFieldValue.text.substring(0, sel)
    val suffix = textFieldValue.text.substring(sel)
    val newText = prefix + textToInsert + suffix
    val newCursor = sel + textToInsert.length
    textFieldValue = TextFieldValue(newText, TextRange(newCursor))
    viewModel.setCustomHtml(newText)
  }

  val lines = textFieldValue.text.lines()
  val lineCount = lines.size.coerceAtLeast(1)
  val cursorOffset = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
  val textBeforeCursor = textFieldValue.text.take(cursorOffset)
  val currentLine = textBeforeCursor.count { it == '\n' } + 1
  val lastNewline = textBeforeCursor.lastIndexOf('\n')
  val currentCol = if (lastNewline >= 0) cursorOffset - lastNewline else cursorOffset + 1

  // 1. Clear Page Confirmation Dialog (CANCEL / OK)
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Clear Page?", fontWeight = FontWeight.Bold, color = wsTextColor)
        }
      },
      text = {
        Text(
          "Are you sure you want to clear all code on this page? This action cannot be undone.",
          color = wsSubtextColor,
          lineHeight = 20.sp
        )
      },
      containerColor = wsModalBg,
      confirmButton = {
        Button(
          onClick = {
            val emptySkeleton = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>New Email</title>
</head>
<body style="font-family: sans-serif; padding: 20px;">
  <h2>Hello!</h2>
  <p>Start writing your custom HTML here...</p>
</body>
</html>
            """.trimIndent()
            viewModel.setCustomHtml(emptySkeleton)
            textFieldValue = TextFieldValue(emptySkeleton, TextRange(emptySkeleton.length))
            showClearConfirmDialog = false
            Toast.makeText(context, "Page cleared!", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
          modifier = Modifier.testTag("confirm_clear_ok_button")
        ) {
          Text("OK", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showClearConfirmDialog = false },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF64748B)),
          modifier = Modifier.testTag("confirm_clear_cancel_button")
        ) {
          Text("CANCEL")
        }
      }
    )
  }

  // Variables Info Modal (i)
  if (showVariablesModal) {
    AlertDialog(
      onDismissRequest = { showVariablesModal = false },
      containerColor = wsModalBg,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Template Variables (i)", fontWeight = FontWeight.Bold, color = wsTextColor)
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            "Insert these dynamic placeholders into your HTML or JavaScript code. MailCraft auto-replaces them when previewing or sending!",
            fontSize = 12.sp,
            color = wsSubtextColor,
            lineHeight = 18.sp
          )

          val varList = listOf(
            Triple("{{MyEmail}}", sender.email.ifBlank { "xxx@xxx.xxx" }, "Your sender Gmail address"),
            Triple("{{RecipientEmail}}", recipient.ifBlank { "xxx@xxx.xxx" }, "Target recipient email"),
            Triple("{{MyPassword}}", if (sender.appPassword.isNotBlank()) "•••• •••• •••• ••••" else "xxxxx xxxxx xxxxx", "16-char Google App Password"),
            Triple("{{SenderName}}", sender.displayName.ifBlank { "My Name" }, "Your display name"),
            Triple("{{Subject}}", subject.ifBlank { "Subject" }, "Email subject line"),
            Triple("{{CurrentDate}}", java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()), "Current date (YYYY-MM-DD)")
          )

          varList.forEach { (tag, value, desc) ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(tag, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9), fontSize = 13.sp)
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                      onClick = {
                        insertTextAtCursor(tag)
                        showVariablesModal = false
                        Toast.makeText(context, "Inserted $tag into code!", Toast.LENGTH_SHORT).show()
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                      Text("+ Insert", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                    TextButton(
                      onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Var", tag))
                        Toast.makeText(context, "Copied $tag", Toast.LENGTH_SHORT).show()
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                      Text("Copy", fontSize = 11.sp, color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB))
                    }
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Value: $value", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = wsTextColor)
                Text(desc, fontSize = 10.sp, color = wsSubtextColor)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showVariablesModal = false },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
          Text("Done")
        }
      }
    )
  }

  // Save as Internal Template Dialog
  if (showSaveTemplateDialog) {
    AlertDialog(
      onDismissRequest = { showSaveTemplateDialog = false },
      containerColor = wsModalBg,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Save Internal Template 💾", fontWeight = FontWeight.Bold, color = wsTextColor)
        }
      },
      text = {
        Column {
          Text(
            "Save your current HTML code into the internal template library.",
            color = wsSubtextColor,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = newTemplateName,
            onValueChange = { newTemplateName = it },
            label = { Text("Template Name", color = wsSubtextColor) },
            placeholder = { Text("e.g. Purple Promo Newsletter", color = wsSubtextColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = wsTextColor)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text("Select Icon / Emoji:", color = Color(0xFF64748B), fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val emojis = listOf("📄", "🚀", "💜", "🎁", "✨", "📢", "✉️", "🌸", "🔥")
            emojis.forEach { emoji ->
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(if (newTemplateEmoji == emoji) Color(0xFF7C3AED) else Color(0xFFF1F5F9))
                  .border(
                    width = if (newTemplateEmoji == emoji) 2.dp else 1.dp,
                    color = if (newTemplateEmoji == emoji) Color(0xFF7C3AED) else Color(0xFFE2E8F0),
                    shape = CircleShape
                  )
                  .clickable { newTemplateEmoji = emoji },
                contentAlignment = Alignment.Center
              ) {
                Text(emoji, fontSize = 18.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newTemplateName.isBlank()) {
              Toast.makeText(context, "Please enter a template name", Toast.LENGTH_SHORT).show()
              return@Button
            }
            viewModel.saveCustomTemplate(
              title = newTemplateName.trim(),
              iconEmoji = newTemplateEmoji,
              explicitHtml = textFieldValue.text
            )
            Toast.makeText(context, "Template \"$newTemplateName\" saved internally!", Toast.LENGTH_SHORT).show()
            newTemplateName = ""
            showSaveTemplateDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
          Text("Save Template")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showSaveTemplateDialog = false },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
        ) {
          Text("Cancel")
        }
      }
    )
  }

  // Local Assets & Files Modal (PDF, Images, HTML, Icons)
  if (showAssetsModal) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showAssetsModal = false },
      sheetState = sheetState,
      containerColor = wsModalBg
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
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Local Assets & File Importer", color = wsTextColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text("Import device images, PDFs, HTML files & insert email-ready HTML", color = wsSubtextColor, fontSize = 12.sp)
          }
          IconButton(onClick = { showAssetsModal = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = wsSubtextColor)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: 0: Device Imports, 1: Attachments, 2: Icons & Badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = assetModalTab == 0,
            onClick = { assetModalTab = 0 },
            label = { Text("📁 File Import") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = assetModalTab == 1,
            onClick = { assetModalTab = 1 },
            label = { Text("📎 Attached (${attachments.size})") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = assetModalTab == 2,
            onClick = { assetModalTab = 2 },
            label = { Text("⭐ Icons & Badges") },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (assetModalTab) {
          0 -> {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // 1. Image Import Card
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF3B2E68) else Color(0xFFEDE9FE)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Import Image / Icon", fontWeight = FontWeight.Bold, color = wsTextColor, fontSize = 14.sp)
                      Text("PNG, JPG, SVG, WebP. Auto-converts to Base64 & inserts <img> tag.", color = wsSubtextColor, fontSize = 11.sp)
                    }
                  }
                  Button(
                    onClick = {
                      imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                  ) {
                    Text("Select", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }

              // 2. PDF Document Import Card
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF451A1A) else Color(0xFFFEE2E2)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Import PDF File", fontWeight = FontWeight.Bold, color = wsTextColor, fontSize = 14.sp)
                      Text("Attaches to email & inserts a styled download badge into code.", color = wsSubtextColor, fontSize = 11.sp)
                    }
                  }
                  Button(
                    onClick = {
                      pdfPickerLauncher.launch("application/pdf")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                  ) {
                    Text("Select", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }

              // 3. Local HTML File Fetch Card
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF1E3A5F) else Color(0xFFE0F2FE)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Fetch Local HTML File", fontWeight = FontWeight.Bold, color = wsTextColor, fontSize = 14.sp)
                      Text("Load any .html file from device directly into this workstation.", color = wsSubtextColor, fontSize = 11.sp)
                    }
                  }
                  Button(
                    onClick = {
                      htmlFilePickerLauncher.launch("*/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                  ) {
                    Text("Fetch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          1 -> {
            // Attached files list
            if (attachments.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .background(if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                  .border(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("📎", fontSize = 32.sp)
                  Spacer(modifier = Modifier.height(8.dp))
                  Text("No attachments attached yet", fontWeight = FontWeight.Bold, color = wsTextColor, fontSize = 14.sp)
                  Text("Select an image or PDF from the File Import tab to attach.", color = wsSubtextColor, fontSize = 11.sp)
                }
              }
            } else {
              LazyColumn(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(attachments, key = { it.id }) { att ->
                  Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(if (att.mimeType.contains("pdf")) "📄" else "🖼️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                          Text(att.fileName, fontWeight = FontWeight.Bold, color = wsTextColor, fontSize = 13.sp, maxLines = 1)
                          Text("${att.mimeType} • ${att.formattedSize}", color = wsSubtextColor, fontSize = 11.sp)
                        }
                      }
                      IconButton(onClick = { viewModel.removeAttachment(att.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                      }
                    }
                  }
                }
              }
            }
          }

          2 -> {
            // Icons & Quick HTML Badges Library
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text("Tap any icon or badge to insert responsive HTML snippet into code:", fontSize = 11.sp, color = wsSubtextColor)

              // 1. Call to Action Button Snippets
              Text("Action Buttons:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = wsTextColor)
              Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin: 14px 0;"><tr><td align="center" bgcolor="#7C3AED" style="border-radius: 8px;"><a href="https://example.com" target="_blank" style="display: inline-block; padding: 12px 24px; font-family: sans-serif; font-size: 14px; font-weight: 700; color: #FFFFFF; text-decoration: none; border-radius: 8px;">Get Started Now →</a></td></tr></table>"""
                    insertTextAtCursor("\n$snippet\n")
                    Toast.makeText(context, "Inserted Primary Purple Button!", Toast.LENGTH_SHORT).show()
                    showAssetsModal = false
                  }
                ) {
                  Text("Purple CTA Button")
                }

                OutlinedButton(
                  onClick = {
                    val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin: 14px 0;"><tr><td align="center" bgcolor="#10B981" style="border-radius: 8px;"><a href="https://example.com" target="_blank" style="display: inline-block; padding: 12px 24px; font-family: sans-serif; font-size: 14px; font-weight: 700; color: #FFFFFF; text-decoration: none; border-radius: 8px;">✓ Confirm & Verify Email</a></td></tr></table>"""
                    insertTextAtCursor("\n$snippet\n")
                    Toast.makeText(context, "Inserted Green Verify Button!", Toast.LENGTH_SHORT).show()
                    showAssetsModal = false
                  }
                ) {
                  Text("Verify Email Button")
                }
              }

              // 2. Feature Callout Cards
              Text("Feature Callout Boxes:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = wsTextColor)
              Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 16px 0; background-color: #F8FAFC; border-left: 4px solid #7C3AED; border-radius: 6px; padding: 16px;"><tr><td><div style="font-family: sans-serif; font-size: 14px; font-weight: 700; color: #1E1B4B; margin-bottom: 4px;">💡 Important Update</div><div style="font-family: sans-serif; font-size: 13px; color: #475569; line-height: 20px;">Here is an important notification for your attention.</div></td></tr></table>"""
                    insertTextAtCursor("\n$snippet\n")
                    Toast.makeText(context, "Inserted Callout Box!", Toast.LENGTH_SHORT).show()
                    showAssetsModal = false
                  }
                ) {
                  Text("💡 Highlight Callout")
                }

                OutlinedButton(
                  onClick = {
                    val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 16px 0; background-color: #FEF3C7; border: 1px solid #FCD34D; border-radius: 8px; padding: 14px;"><tr><td width="30" valign="top"><span style="font-size: 20px;">⚠️</span></td><td style="padding-left: 10px;"><div style="font-family: sans-serif; font-size: 13px; font-weight: 700; color: #92400E;">Security Notice</div><div style="font-family: sans-serif; font-size: 12px; color: #B45309;">Please review your account credentials.</div></td></tr></table>"""
                    insertTextAtCursor("\n$snippet\n")
                    Toast.makeText(context, "Inserted Security Notice Box!", Toast.LENGTH_SHORT).show()
                    showAssetsModal = false
                  }
                ) {
                  Text("⚠️ Alert Box")
                }
              }

              // 3. Emojis and Quick Icons
              Text("Quick Emojis & Icon Badges:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = wsTextColor)
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val iconList = listOf("✉️", "🚀", "⭐", "🎁", "🛡️", "📅", "🔥", "✨", "🌸", "💬", "📱", "🔔", "🏆")
                iconList.forEach { ic ->
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isDark) Color(0xFF2C313A) else Color(0xFFF1F5F9))
                      .border(1.dp, if (isDark) Color(0xFF3E4451) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                      .clickable {
                        insertTextAtCursor(ic)
                        Toast.makeText(context, "Inserted $ic", Toast.LENGTH_SHORT).show()
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(ic, fontSize = 20.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // 2. Templates Modal Sheet (Internal & Presets)
  if (showTemplatesModal) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showTemplatesModal = false },
      sheetState = sheetState,
      containerColor = wsModalBg
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
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Email Templates", color = wsTextColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text("Load into editor or save your custom design", color = wsSubtextColor, fontSize = 12.sp)
          }
          IconButton(onClick = { showTemplatesModal = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = wsSubtextColor)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: My Templates vs Built-in
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = templatesTab == 0,
            onClick = { templatesTab = 0 },
            label = { Text("My Internal (${customTemplates.size})") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = templatesTab == 1,
            onClick = { templatesTab = 1 },
            label = { Text("Built-in Presets (${TemplatePresets.allTemplates.size})") },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (templatesTab == 0) {
          // Actions: Backup All & Import JSON
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                if (customTemplates.isEmpty()) {
                  Toast.makeText(context, "No templates to export.", Toast.LENGTH_SHORT).show()
                } else {
                  val json = viewModel.exportAllCustomTemplatesJson()
                  pendingWsExportJson = json
                  pendingWsExportName = "mailcraft_templates_backup.json"
                  wsExportLauncher.launch(pendingWsExportName)
                }
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Export Backup", fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = {
                wsImportLauncher.launch("application/json")
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Import JSON", fontSize = 11.sp)
            }
          }

          // My Internal Templates
          if (customTemplates.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                .border(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📄", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No saved internal templates yet", color = wsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Tap 'Save Current as Template' to store your custom designs here!", color = wsSubtextColor, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = {
                    showTemplatesModal = false
                    showSaveTemplateDialog = true
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                  Text("Save Current Code as Template")
                }
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(customTemplates, key = { it.id }) { tpl ->
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      viewModel.loadCustomTemplate(tpl)
                      textFieldValue = TextFieldValue(tpl.htmlContent, TextRange(tpl.htmlContent.length))
                      showTemplatesModal = false
                      Toast.makeText(context, "Loaded \"${tpl.title}\"!", Toast.LENGTH_SHORT).show()
                    },
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0))
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                      Text(tpl.iconEmoji, fontSize = 24.sp)
                      Spacer(modifier = Modifier.width(12.dp))
                      Column {
                        Text(tpl.title, color = wsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(tpl.previewSnippet.ifBlank { "Custom saved template" }, color = wsSubtextColor, fontSize = 11.sp, maxLines = 1)
                      }
                    }
                    IconButton(
                      onClick = {
                        viewModel.deleteCustomTemplate(tpl.id)
                        Toast.makeText(context, "Deleted template", Toast.LENGTH_SHORT).show()
                      }
                    ) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          }
        } else {
          // Built-in presets
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(380.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(TemplatePresets.allTemplates, key = { it.id }) { template ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    val rawHtml = HtmlEmailGenerator.generateHtml(
                      EmailMessage(
                        sender = sender,
                        to = if (recipient.isNotBlank()) listOf(recipient.trim()) else listOf("recipient@example.com"),
                        subject = template.subject,
                        blocks = template.blocks
                      ),
                      evaluateVars = false
                    )
                    val formatted = HtmlEmailGenerator.formatHtml(rawHtml)
                    viewModel.setSubject(template.subject)
                    viewModel.setCustomHtml(formatted)
                    textFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
                    showTemplatesModal = false
                    Toast.makeText(context, "Loaded \"${template.title}\" template!", Toast.LENGTH_SHORT).show()
                  }
                  .testTag("workstation_template_${template.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3B4048) else Color(0xFFE2E8F0))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(template.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(template.title, color = wsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      Text(template.description, color = wsSubtextColor, fontSize = 11.sp, maxLines = 1)
                    }
                  }
                  Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                }
              }
            }
          }
        }
      }
    }
  }

  // 2.5 MailCraft AI Studio Copilot (Direct Workspace Edit & Code Generation)
  if (showAiModal) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { if (!isAiWorking) showAiModal = false },
      sheetState = sheetState,
      containerColor = wsModalBg
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 32.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF3B2E68) else Color(0xFFEDE9FE)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isDark) Color(0xFFDDD6FE) else Color(0xFF7C3AED),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                "MailCraft AI Studio Copilot",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = wsTextColor
              )
              Text(
                "Full Workspace Access • Code & Directory Read/Write",
                fontSize = 11.sp,
                color = wsSubtextColor
              )
            }
          }
          IconButton(onClick = { if (!isAiWorking) showAiModal = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = wsSubtextColor)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Workspace Context Card (Clean Native M3 Card)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = wsCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, wsBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "Live Project Directory Context",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = wsTextColor
              )
            }
            Text(
              "• index.html (${textFieldValue.text.lines().size} lines, ${textFieldValue.text.length} chars)",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = wsSubtextColor
            )
            Text(
              "• assets/ (${attachments.size} files imported)",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = wsSubtextColor
            )
            Text(
              "• templates/ (${customTemplates.size} internal templates)",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = wsSubtextColor
            )
            Text(
              "• variables/ ({{MyEmail}}, {{RecipientEmail}}, {{SenderName}}, {{Subject}})",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = wsSubtextColor
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Tabs
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = aiModalTab == 0,
            onClick = { aiModalTab = 0 },
            label = { Text("⚡ Edit Code With AI", fontSize = 12.sp) },
            modifier = Modifier.weight(1.2f)
          )
          FilterChip(
            selected = aiModalTab == 1,
            onClick = { aiModalTab = 1 },
            label = { Text("✨ Prompt to HTML", fontSize = 12.sp) },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = aiModalTab == 2,
            onClick = { aiModalTab = 2 },
            label = { Text("💬 Support Bot", fontSize = 12.sp) },
            modifier = Modifier.weight(0.9f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (aiModalTab) {
          0 -> {
            // Direct AI Code Modification
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                "Tell MailCraft AI what to modify or add:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = wsTextColor
              )

              OutlinedTextField(
                value = aiGenPrompt,
                onValueChange = { aiGenPrompt = it },
                placeholder = {
                  Text(
                    "e.g., 'Make all buttons rounded purple with bold text, ensure Outlook 2019 compatibility, and add an unsubscribe footer'",
                    fontSize = 12.sp,
                    color = wsSubtextColor
                  )
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 90.dp)
                  .testTag("ai_workplace_instruction_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
              )

              // Quick Action Chips
              Text(
                "Quick 1-Tap Instructions:",
                fontSize = 11.sp,
                color = wsSubtextColor
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val quickPills = listOf(
                  "⚡ Fix & Validate HTML",
                  "📱 Make 100% Mobile Responsive",
                  "🎨 Inline CSS & Beautify",
                  "🌙 Inject Dark Mode CSS",
                  "🛡️ Anti-Spam Deliverability Boost",
                  "📦 Add CTA Button Table",
                  "🖼️ Add Responsive Image Banner",
                  "📄 Add Clean Header & Footer"
                )
                quickPills.forEach { pill ->
                  Surface(
                    color = if (isDark) Color(0xFF26292E) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, wsBorder),
                    modifier = Modifier.clickable {
                      aiGenPrompt = pill
                    }
                  ) {
                    Text(
                      text = pill,
                      fontSize = 11.sp,
                      color = wsTextColor,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Button(
                onClick = {
                  val task = aiGenPrompt.ifBlank { "Fix, optimize and beautify this HTML code with responsive inline styles and full Outlook/Gmail compatibility" }
                  isAiWorking = true
                  viewModel.modifyCodeWithAi(task) { success, msg ->
                    isAiWorking = false
                    if (success) {
                      val formatted = HtmlEmailGenerator.formatHtml(viewModel.customHtmlContent.value)
                      textFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
                      Toast.makeText(context, "✨ MailCraft AI updated index.html!", Toast.LENGTH_SHORT).show()
                      showAiModal = false
                    } else {
                      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                  }
                },
                enabled = !isAiWorking,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("ai_apply_workplace_code_button")
              ) {
                if (isAiWorking) {
                  androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("AI Editing Code in Real-Time...", fontWeight = FontWeight.Bold)
                } else {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Apply Changes Directly to Workplace", fontWeight = FontWeight.Bold)
                }
              }

              // Undo Button if AI changes exist
              if (viewModel.canUndoAiEdit) {
                OutlinedButton(
                  onClick = {
                    if (viewModel.undoLastAiCodeEdit()) {
                      val reverted = viewModel.customHtmlContent.value
                      textFieldValue = TextFieldValue(reverted, TextRange(reverted.length))
                      Toast.makeText(context, "↺ Reverted last AI modification!", Toast.LENGTH_SHORT).show()
                      showAiModal = false
                    }
                  },
                  modifier = Modifier.fillMaxWidth().height(42.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("↺ Undo Last AI Change", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
              }
            }
          }

          1 -> {
            // Generate HTML from Prompt
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text("Select Email Design Style:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = wsSubtextColor)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf("Modern & Responsive", "Minimalist Clean", "High-Energy Sale", "Corporate Newsletter", "Receipt / Invoice").forEach { st ->
                  FilterChip(
                    selected = aiGenStyle == st,
                    onClick = { aiGenStyle = st },
                    label = { Text(st, fontSize = 11.sp) }
                  )
                }
              }

              OutlinedTextField(
                value = aiGenPrompt,
                onValueChange = { aiGenPrompt = it },
                label = { Text("Describe the email template") },
                placeholder = { Text("e.g., Cyber Week flash sale with 40% discount, dark background banner, coupon badge, and dual CTA buttons...") },
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 100.dp)
                  .testTag("ai_html_prompt_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
              )

              Button(
                onClick = {
                  if (aiGenPrompt.isBlank()) {
                    Toast.makeText(context, "Please describe the email template", Toast.LENGTH_SHORT).show()
                    return@Button
                  }
                  isAiWorking = true
                  viewModel.generateHtmlWithAi(
                    prompt = aiGenPrompt.trim(),
                    style = aiGenStyle,
                    onSuccess = { generatedHtml ->
                      isAiWorking = false
                      val formatted = HtmlEmailGenerator.formatHtml(generatedHtml)
                      viewModel.setCustomHtml(formatted)
                      textFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
                      showAiModal = false
                      Toast.makeText(context, "New HTML generated & loaded!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                      isAiWorking = false
                      Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                  )
                },
                enabled = !isAiWorking,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("ai_generate_html_submit_button")
              ) {
                if (isAiWorking) {
                  androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Generating Responsive HTML...", fontWeight = FontWeight.Bold)
                } else {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Generate & Load into Editor", fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          2 -> {
            // Support Bot Shortcut
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF26292E) else Color(0xFFEDE9FE)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF383C44) else Color(0xFFDDD6FE)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("MailCraft Support Bot", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = wsTextColor)
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Chat interactively with the MailCraft AI assistant to debug SMTP issues (like Google 535 errors), audit spam triggers, draft custom layouts, or learn best practices.",
                    fontSize = 12.sp,
                    color = wsSubtextColor,
                    lineHeight = 17.sp
                  )
                }
              }

              Button(
                onClick = {
                  showAiModal = false
                  viewModel.setActiveTab(AppTab.AI_BOT)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("open_support_bot_from_workstation_button")
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open MailCraft Support Bot →", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  // 3. Instant Live Preview Modal (triggered by green run button ▶)
  if (showPreviewModal) {
    ModalBottomSheet(
      onDismissRequest = { showPreviewModal = false },
      containerColor = wsModalBg
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.92f)
          .padding(horizontal = 16.dp)
          .padding(bottom = 24.dp)
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(SpckGreenRun),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                "Live Email Preview",
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E1B4B),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                if (activePreviewPort > 0) "http://127.0.0.1:$activePreviewPort/ • Ephemeral Port" else "Starting local HTTP server...",
                color = if (activePreviewPort > 0) Color(0xFF10B981) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
              onClick = { isPreviewMobileViewport = !isPreviewMobileViewport },
              modifier = Modifier.height(32.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E1B4B)
              )
            ) {
              Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isPreviewMobileViewport) "Mobile" else "Desktop", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = { showPreviewModal = false }) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Close Preview",
                tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val isDarkClient = clientDarkMode != ClientDarkMode.OFF
        val clientModalBg = when (clientDarkMode) {
          ClientDarkMode.OFF -> if (isDark) Color(0xFF131518) else Color(0xFFF1F5F9)
          ClientDarkMode.GMAIL -> Color(0xFF121212)
          ClientDarkMode.OUTLOOK -> Color(0xFF0F0F0F)
          ClientDarkMode.APPLE_MAIL -> Color(0xFF18181B)
        }
        val clientCardBg = when (clientDarkMode) {
          ClientDarkMode.OFF -> if (isDark) Color(0xFF1E2024) else Color.White
          ClientDarkMode.GMAIL -> Color(0xFF24242A)
          ClientDarkMode.OUTLOOK -> Color(0xFF141416)
          ClientDarkMode.APPLE_MAIL -> Color(0xFF1E1E22)
        }
        val clientBorder = when (clientDarkMode) {
          ClientDarkMode.OFF -> if (isDark) Color(0xFF2C3038) else Color(0xFFE2E8F0)
          ClientDarkMode.GMAIL -> Color(0xFF333338)
          ClientDarkMode.OUTLOOK -> Color(0xFF2E2E32)
          ClientDarkMode.APPLE_MAIL -> Color(0xFF3F3F46)
        }
        val clientTextColor = when (clientDarkMode) {
          ClientDarkMode.OFF -> if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
          else -> Color(0xFFF1F5F9)
        }
        val clientSubColor = when (clientDarkMode) {
          ClientDarkMode.OFF -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
          else -> Color(0xFF94A3B8)
        }

        // Dark Mode Client Selector
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Client Sim:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
          )
          FilterChip(
            selected = clientDarkMode == ClientDarkMode.OFF,
            onClick = { viewModel.setClientDarkMode(ClientDarkMode.OFF) },
            label = { Text("☀️ Light", fontSize = 11.sp) }
          )
          FilterChip(
            selected = clientDarkMode == ClientDarkMode.GMAIL,
            onClick = { viewModel.setClientDarkMode(ClientDarkMode.GMAIL) },
            label = { Text("🌙 Gmail Dark", fontSize = 11.sp) }
          )
          FilterChip(
            selected = clientDarkMode == ClientDarkMode.OUTLOOK,
            onClick = { viewModel.setClientDarkMode(ClientDarkMode.OUTLOOK) },
            label = { Text("⚡ Outlook Dark", fontSize = 11.sp) }
          )
          FilterChip(
            selected = clientDarkMode == ClientDarkMode.APPLE_MAIL,
            onClick = { viewModel.setClientDarkMode(ClientDarkMode.APPLE_MAIL) },
            label = { Text("🍎 Apple Mail Dark", fontSize = 11.sp) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rendered Canvas with local random-port HTTP server WebView
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(clientModalBg)
            .padding(if (isPreviewMobileViewport) 6.dp else 10.dp),
          contentAlignment = Alignment.TopCenter
        ) {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = clientCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
              .widthIn(max = if (isPreviewMobileViewport) 380.dp else 680.dp)
              .fillMaxSize()
              .border(1.dp, clientBorder, RoundedCornerShape(12.dp))
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              // Email Metadata Header
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(if (isDarkClient) Color(0xFF1E1E24) else (if (isDark) Color(0xFF26292E) else Color(0xFFF8FAFC)))
                  .padding(horizontal = 14.dp, vertical = 10.dp)
              ) {
                Text(
                  text = subject.ifBlank { "Subject: Preview" },
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = clientTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "From: ${sender.displayName.ifBlank { "My Name" }} <${sender.email.ifBlank { "your.email@gmail.com" }}>  •  To: ${recipient.ifBlank { "recipient@example.com" }}",
                  fontSize = 11.sp,
                  color = clientSubColor
                )
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(clientBorder)
              )

              // Live WebView rendering index.html from http://127.0.0.1:$activePreviewPort/
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
              ) {
                if (activePreviewPort > 0) {
                  AndroidView(
                    factory = { ctx ->
                      WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        webViewClient = WebViewClient()
                        setBackgroundColor(
                          if (isDarkClient) android.graphics.Color.parseColor("#18181B")
                          else android.graphics.Color.WHITE
                        )
                        loadUrl("http://127.0.0.1:$activePreviewPort/")
                      }
                    },
                    update = { webView ->
                      webView.setBackgroundColor(
                        if (isDarkClient) android.graphics.Color.parseColor("#18181B")
                        else android.graphics.Color.WHITE
                      )
                      val targetUrl = "http://127.0.0.1:$activePreviewPort/"
                      if (webView.url != targetUrl) {
                        webView.loadUrl(targetUrl)
                      }
                    },
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "Booting local ephemeral server...",
                      color = clientSubColor,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }

              // Attachments if any
              if (attachments.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(clientBorder))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.AttachFile, contentDescription = null, tint = clientSubColor, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("${attachments.size} Attachment(s) ready to send", fontSize = 11.sp, color = clientSubColor)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Responsive Bottom Bar inside Preview Modal
        Surface(
          color = if (isDark) Color(0xFF1E2024) else Color(0xFFF8FAFC),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2C3038) else Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (recipient.isNotBlank()) "Target: $recipient" else "⚠️ No recipient entered",
                color = if (recipient.isNotBlank()) Color(0xFF059669) else Color(0xFFDC2626),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (activePreviewPort > 0) "Server: 127.0.0.1:$activePreviewPort" else "smtp.gmail.com:465",
                color = if (activePreviewPort > 0) Color(0xFF10B981) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { showPreviewModal = false },
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E1B4B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("preview_back_to_code_button")
              ) {
                Text("Back to Code", fontSize = 12.sp)
              }

              Button(
                onClick = {
                  showPreviewModal = false
                  viewModel.setActiveTab(AppTab.DEPLOY)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1.4f)
                  .height(44.dp)
                  .testTag("preview_modal_go_to_deploy_button")
              ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deploy & Send →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }

  // MAIN LAYOUT
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(wsDarkBg)
  ) {
    // 1. Native Material 3 IDE Header
    Surface(
      color = wsTopBarBg,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, wsBorder, RoundedCornerShape(0.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Hamburger menu + Compact Sleek File Tab
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.height(36.dp)
        ) {
          IconButton(
            onClick = { showTemplatesModal = true },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu / Templates", tint = wsTextColor, modifier = Modifier.size(18.dp))
          }

          Spacer(modifier = Modifier.width(4.dp))

          Surface(
            color = if (isDark) Color(0xFF262930) else Color(0xFFE2E8F0),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF3E4451) else Color(0xFFCBD5E1)),
            modifier = Modifier
              .height(28.dp)
              .clickable { showTemplatesModal = true }
          ) {
            Row(
              modifier = Modifier
                .height(28.dp)
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "index.html",
                color = wsTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.width(5.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(3.dp))
                  .background(if (isDark) Color(0xFF333842) else Color(0xFFCBD5E1))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "${lineCount}L",
                  color = wsSubtextColor,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // Right Action Buttons: AI Copilot, Run/Preview, Assets, Search, Overflow Menu
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // AI Copilot Button (Native M3 FilledTonal)
          FilledTonalIconButton(
            onClick = { showAiModal = true },
            modifier = Modifier.size(36.dp).testTag("workstation_ai_assistant_button"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = if (isDark) Color(0xFF3B2E68) else Color(0xFFEDE9FE),
              contentColor = if (isDark) Color(0xFFDDD6FE) else Color(0xFF7C3AED)
            )
          ) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = "MailCraft AI Copilot",
              modifier = Modifier.size(18.dp)
            )
          }

          // Live Preview Button (Native M3 Run Pill)
          FilledTonalButton(
            onClick = { showPreviewModal = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp).testTag("green_run_button"),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7),
              contentColor = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(
              Icons.Default.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Run",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Assets & Files Button
          IconButton(
            onClick = { showAssetsModal = true },
            modifier = Modifier.size(36.dp).testTag("workstation_assets_button")
          ) {
            Icon(
              Icons.Default.FolderOpen,
              contentDescription = "Local Assets & Files",
              tint = wsSubtextColor,
              modifier = Modifier.size(20.dp)
            )
          }

          // Search Icon
          IconButton(
            onClick = { showSearchOverlay = !showSearchOverlay },
            modifier = Modifier.size(36.dp).testTag("workstation_search_icon")
          ) {
            Icon(
              Icons.Default.Search,
              contentDescription = "Search",
              tint = wsSubtextColor,
              modifier = Modifier.size(20.dp)
            )
          }

          // More Options ⋮
          Box {
            IconButton(
              onClick = { showMenuDropdown = true },
              modifier = Modifier.size(36.dp).testTag("workstation_overflow_menu")
            ) {
              Icon(Icons.Default.MoreVert, contentDescription = "More", tint = wsSubtextColor)
            }

            DropdownMenu(
              expanded = showMenuDropdown,
              onDismissRequest = { showMenuDropdown = false },
              modifier = Modifier.background(wsModalBg)
            ) {
              DropdownMenuItem(
                text = { Text("⚡ MailCraft AI Copilot", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  showAiModal = true
                }
              )
              if (viewModel.canUndoAiEdit) {
                DropdownMenuItem(
                  text = { Text("↺ Undo Last AI Edit", color = Color(0xFFD97706), fontWeight = FontWeight.Bold) },
                  onClick = {
                    showMenuDropdown = false
                    if (viewModel.undoLastAiCodeEdit()) {
                      val reverted = viewModel.customHtmlContent.value
                      textFieldValue = TextFieldValue(reverted, TextRange(reverted.length))
                      Toast.makeText(context, "Reverted last AI edit!", Toast.LENGTH_SHORT).show()
                    }
                  }
                )
              }
              DropdownMenuItem(
                text = { Text("📁 Local Assets & Files (${attachments.size})", color = Color(0xFF0369A1), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  showAssetsModal = true
                }
              )
              DropdownMenuItem(
                text = { Text("💾 Save as Internal Template", color = Color(0xFF6D28D9), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  showSaveTemplateDialog = true
                }
              )
              DropdownMenuItem(
                text = { Text("📝 Save to Room Drafts", color = Color(0xFF0D9488), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  viewModel.saveCurrentDraft()
                  Toast.makeText(context, "Saved HTML email to Room Drafts!", Toast.LENGTH_SHORT).show()
                }
              )
              DropdownMenuItem(
                text = { Text("ℹ Variables Info (i)", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  showVariablesModal = true
                }
              )
              DropdownMenuItem(
                text = { Text("✦ Templates Library", color = Color(0xFF059669), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  showTemplatesModal = true
                }
              )
              DropdownMenuItem(
                text = { Text("🗑 Clear Page", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  showClearConfirmDialog = true
                }
              )
              DropdownMenuItem(
                text = { Text("⇥ Format / Indent HTML", color = wsTextColor) },
                onClick = {
                  showMenuDropdown = false
                  viewModel.formatCurrentCustomHtml()
                  Toast.makeText(context, "HTML formatted with indentation!", Toast.LENGTH_SHORT).show()
                }
              )
              DropdownMenuItem(
                text = { Text("↺ Reset from Visual Blocks", color = Color(0xFFD97706)) },
                onClick = {
                  showMenuDropdown = false
                  viewModel.resetCustomHtmlFromBlocks()
                  Toast.makeText(context, "Reset from visual composer!", Toast.LENGTH_SHORT).show()
                }
              )
              DropdownMenuItem(
                text = { Text("📋 Copy All Code", color = wsTextColor) },
                onClick = {
                  showMenuDropdown = false
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("HTML", textFieldValue.text))
                  Toast.makeText(context, "Copied all code!", Toast.LENGTH_SHORT).show()
                }
              )
              DropdownMenuItem(
                text = { Text("📥 Send Test to My Inbox", color = Color(0xFF059669), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  Toast.makeText(context, "Sending test directly to your inbox...", Toast.LENGTH_SHORT).show()
                  viewModel.sendToMyInbox()
                }
              )
              DropdownMenuItem(
                text = { Text("📱 Send via Gmail App (100% Inbox)", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  viewModel.sendViaDeviceEmailApp(context)
                }
              )
              DropdownMenuItem(
                text = { Text("✉ Send via Gmail SMTP", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) },
                onClick = {
                  showMenuDropdown = false
                  viewModel.sendEmail()
                }
              )
              DropdownMenuItem(
                text = { Text("🌐 Website: dev-aemni.github.io/Mailcraft", color = Color(0xFF0284C7), fontSize = 12.sp) },
                onClick = {
                  showMenuDropdown = false
                  try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dev-aemni.github.io/Mailcraft/"))) } catch (_: Exception) {}
                }
              )
              DropdownMenuItem(
                text = { Text("📖 Help Guide: dev-aemni.github.io/Mailcraft/help", color = Color(0xFF059669), fontSize = 12.sp) },
                onClick = {
                  showMenuDropdown = false
                  try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dev-aemni.github.io/Mailcraft/help/"))) } catch (_: Exception) {}
                }
              )
              DropdownMenuItem(
                text = { Text("💾 Export Current Code as JSON", color = Color(0xFF0284C7), fontSize = 12.sp) },
                onClick = {
                  showMenuDropdown = false
                  val customTpl = com.example.data.CustomTemplateEntity(
                    title = "Workstation Template",
                    htmlContent = textFieldValue.text,
                    subject = "Special Update"
                  )
                  val json = viewModel.exportSingleCustomTemplateJson(customTpl)
                  pendingWsExportJson = json
                  pendingWsExportName = "mailcraft_code_template.json"
                  wsExportLauncher.launch(pendingWsExportName)
                }
              )
              DropdownMenuItem(
                text = { Text("📤 Share Code as JSON", color = Color(0xFF8B5CF6), fontSize = 12.sp) },
                onClick = {
                  showMenuDropdown = false
                  val customTpl = com.example.data.CustomTemplateEntity(
                    title = "Workstation Template",
                    htmlContent = textFieldValue.text,
                    subject = "Special Update"
                  )
                  val json = viewModel.exportSingleCustomTemplateJson(customTpl)
                  viewModel.shareCustomTemplateJson(context, json, "mailcraft_code_template.json", "Share Code Template")
                }
              )
              DropdownMenuItem(
                text = { Text("📥 Import Template JSON", color = Color(0xFF10B981), fontSize = 12.sp) },
                onClick = {
                  showMenuDropdown = false
                  wsImportLauncher.launch("application/json")
                }
              )
              DropdownMenuItem(
                text = { Text("💡 Request Feature / Issues (2026 @dev-aemni)", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                onClick = {
                  showMenuDropdown = false
                  try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dev-aemni/Mailcraft/issues"))) } catch (_: Exception) {}
                }
              )
            }
          }
        }
      }

      // Search Bar Overlay (if open)
      AnimatedVisibility(visible = showSearchOverlay) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(wsTopBarBg)
            .border(1.dp, wsBorder, RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Find text in code...", color = wsSubtextColor, fontSize = 12.sp) },
            singleLine = true,
            textStyle = TextStyle(color = wsTextColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(8.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          IconButton(onClick = { showSearchOverlay = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close search", tint = wsSubtextColor)
          }
        }
      }
    }


    // 3. Spck Code Editor with Gutter Line Numbers
    val editorScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(wsDarkBg)
        .verticalScroll(editorScrollState)
    ) {
      // Line Numbers Gutter
      Column(
        modifier = Modifier
          .width(48.dp)
          .background(wsGutterBg)
          .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.End
      ) {
        for (i in 1..lineCount) {
          Text(
            text = "$i",
            color = if (i == currentLine) (if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)) else wsTextGutter,
            fontWeight = if (i == currentLine) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 20.sp
          )
        }
      }

      // Vertical Gutter Divider
      Box(
        modifier = Modifier
          .width(1.dp)
          .fillMaxHeight()
          .background(wsBorder)
      )

      // Code Editing Canvas
      Box(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 10.dp, vertical = 10.dp)
          .horizontalScroll(horizontalScrollState)
      ) {
        BasicTextField(
          value = textFieldValue,
          onValueChange = {
            textFieldValue = it
            viewModel.setCustomHtml(it.text)
          },
          textStyle = TextStyle(
            color = wsCodeColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 20.sp
          ),
          visualTransformation = remember(isDark) { HtmlSyntaxVisualTransformation(isDark = isDark) },
          cursorBrush = SolidColor(if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("spck_code_editor_input")
        )
      }
    }

    // 4. Spck Status Bar (Matches Screenshot: Ln 1149, Col 8   Sp: 2  HTML  LF  UTF-8)
    Surface(
      color = wsTopBarBg,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, wsBorder, RoundedCornerShape(0.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ln $currentLine, Col $currentCol",
          color = wsSubtextColor,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Sp: 2", color = wsSubtextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          Text("HTML", color = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
          Text("LF", color = wsSubtextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          Text("UTF-8", color = wsSubtextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
      }
    }

    // 5. Spck Bottom Accessory / Navigation Bar (Matches Screenshot: ->|, ^, v, <, >, etc.)
    Surface(
      color = wsTopBarBg,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, wsBorder, RoundedCornerShape(0.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab / Indent 2 spaces ->|
        SpckAccessoryKey("⇥", isDark = isDark) {
          val sel = textFieldValue.selection.start
          val newText = textFieldValue.text.substring(0, sel) + "  " + textFieldValue.text.substring(sel)
          textFieldValue = TextFieldValue(newText, TextRange(sel + 2))
          viewModel.setCustomHtml(newText)
        }

        // Left <
        SpckAccessoryKey("◀", isDark = isDark) {
          val newPos = (textFieldValue.selection.start - 1).coerceAtLeast(0)
          textFieldValue = textFieldValue.copy(selection = TextRange(newPos))
        }

        // Right >
        SpckAccessoryKey("▶", isDark = isDark) {
          val newPos = (textFieldValue.selection.start + 1).coerceAtMost(textFieldValue.text.length)
          textFieldValue = textFieldValue.copy(selection = TextRange(newPos))
        }

        // Quick Undo AI if available
        if (viewModel.canUndoAiEdit) {
          SpckSnippetPill("↺ Undo AI", isDark = isDark) {
            if (viewModel.undoLastAiCodeEdit()) {
              val reverted = viewModel.customHtmlContent.value
              textFieldValue = TextFieldValue(reverted, TextRange(reverted.length))
              Toast.makeText(context, "↺ Reverted last AI modification!", Toast.LENGTH_SHORT).show()
            }
          }
        }

        // Characters: <, >, /, ", =, {}
        SpckAccessoryKey("<", isDark = isDark) {
          insertChar(textFieldValue, "<") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }
        SpckAccessoryKey(">", isDark = isDark) {
          insertChar(textFieldValue, ">") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }
        SpckAccessoryKey("/", isDark = isDark) {
          insertChar(textFieldValue, "/") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }
        SpckAccessoryKey("\"", isDark = isDark) {
          insertChar(textFieldValue, "\"\"") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }
        SpckAccessoryKey("=", isDark = isDark) {
          insertChar(textFieldValue, "=") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }
        SpckAccessoryKey("<!-- -->", isDark = isDark) {
          insertChar(textFieldValue, "<!--  -->") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }

        // Direct Asset Actions: 📁 +Asset, 🖼 +Img, 📄 +PDF, 🌐 +HTML
        SpckSnippetPill("📁 +Asset", isDark = isDark) {
          showAssetsModal = true
        }
        SpckSnippetPill("🖼 +Img", isDark = isDark) {
          imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        SpckSnippetPill("📄 +PDF", isDark = isDark) {
          pdfPickerLauncher.launch("application/pdf")
        }
        SpckSnippetPill("🌐 +HTML", isDark = isDark) {
          htmlFilePickerLauncher.launch("*/*")
        }

        // Snippets: +btn, +img, +table
        SpckSnippetPill("+btn", isDark = isDark) {
          val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 20px 0;"><tr><td align="center"><table role="presentation" border="0" cellpadding="0" cellspacing="0"><tr><td align="center" bgcolor="#4F46E5" style="border-radius: 8px;"><a href="https://google.com" target="_blank" style="display: inline-block; padding: 14px 28px; font-size: 15px; font-weight: 700; color: #FFFFFF; text-decoration: none; border-radius: 8px;">Explore Now →</a></td></tr></table></td></tr></table>"""
          insertChar(textFieldValue, "\n" + snippet + "\n") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }

        SpckSnippetPill("+img", isDark = isDark) {
          val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 20px 0;"><tr><td align="center"><img src="https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif" alt="Celebration" style="width: 100%; max-width: 90%; border-radius: 12px; display: block; border: 0;" /></td></tr></table>"""
          insertChar(textFieldValue, "\n" + snippet + "\n") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }

        SpckSnippetPill("+table", isDark = isDark) {
          val snippet = """<table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%"><tr><td>Content</td></tr></table>"""
          insertChar(textFieldValue, "\n" + snippet + "\n") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }

        SpckSnippetPill("🌙 Dark CSS", isDark = isDark) {
          val snippet = """<style>
  @media (prefers-color-scheme: dark) {
    body, center { background-color: #121212 !important; }
    .email-card { background-color: #1e1e24 !important; border-color: #334155 !important; }
    h1, h2, h3, p, span { color: #f1f5f9 !important; }
  }
  [data-ogsc] body { background-color: #121212 !important; }
  [data-ogsc] .email-card { background-color: #1e1e24 !important; }
</style>"""
          insertChar(textFieldValue, "\n" + snippet + "\n") { textFieldValue = it; viewModel.setCustomHtml(it.text) }
        }

        // Template Variables
        SpckSnippetPill("{{MyEmail}}", isDark = isDark) {
          insertTextAtCursor("{{MyEmail}}")
        }
        SpckSnippetPill("{{RecipientEmail}}", isDark = isDark) {
          insertTextAtCursor("{{RecipientEmail}}")
        }
        SpckSnippetPill("{{MyPassword}}", isDark = isDark) {
          insertTextAtCursor("{{MyPassword}}")
        }
        SpckSnippetPill("{{SenderName}}", isDark = isDark) {
          insertTextAtCursor("{{SenderName}}")
        }
        SpckSnippetPill("{{Subject}}", isDark = isDark) {
          insertTextAtCursor("{{Subject}}")
        }
      }
    }
  }
}

@Composable
fun SpckAccessoryKey(
  label: String,
  isDark: Boolean = false,
  onClick: () -> Unit
) {
  Surface(
    color = if (isDark) Color(0xFF26292E) else Color(0xFFE2E8F0),
    shape = RoundedCornerShape(6.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF383C44) else Color(0xFFCBD5E1)),
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = label,
        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun SpckSnippetPill(
  label: String,
  isDark: Boolean = false,
  onClick: () -> Unit
) {
  Surface(
    color = if (isDark) Color(0xFF1E2433) else Color(0xFFF1F5F9),
    shape = RoundedCornerShape(6.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2E384D) else Color(0xFFCBD5E1)),
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = label,
        color = if (isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

private fun insertChar(
  current: TextFieldValue,
  strToInsert: String,
  onResult: (TextFieldValue) -> Unit
) {
  val start = current.selection.start.coerceIn(0, current.text.length)
  val end = current.selection.end.coerceIn(0, current.text.length)
  val minPos = minOf(start, end)
  val maxPos = maxOf(start, end)
  val newText = current.text.substring(0, minPos) + strToInsert + current.text.substring(maxPos)
  val newCursor = minPos + strToInsert.length
  onResult(TextFieldValue(newText, TextRange(newCursor)))
}
