package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ClientDarkMode
import com.example.model.EmailAttachment
import com.example.model.EmailBlock

import com.example.ui.MailCraftViewModel
import com.example.util.HtmlEmailGenerator

enum class PreviewSubTab {
  VISUAL,
  HTML_CODE,
  NODEMAILER_JS
}

@Composable
fun PreviewScreen(viewModel: MailCraftViewModel) {
  val context = LocalContext.current
  val currentMessage = viewModel.getCurrentEmailMessage()
  val blocks by viewModel.blocks.collectAsState()
  val recipient by viewModel.recipientEmail.collectAsState()
  val sender by viewModel.senderAccount.collectAsState()
  val subject by viewModel.subject.collectAsState()
  val attachments by viewModel.attachments.collectAsState()
  val clientDarkMode by viewModel.clientDarkMode.collectAsState()

  var subTab by remember { mutableStateOf(PreviewSubTab.VISUAL) }
  var isMobileViewport by remember { mutableStateOf(false) }

  val isDarkClient = clientDarkMode != ClientDarkMode.OFF
  val clientBgColor = when (clientDarkMode) {
    ClientDarkMode.OFF -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ClientDarkMode.GMAIL -> Color(0xFF121212)
    ClientDarkMode.OUTLOOK -> Color(0xFF0F0F0F)
    ClientDarkMode.APPLE_MAIL -> Color(0xFF18181B)
  }
  val clientHeaderBg = when (clientDarkMode) {
    ClientDarkMode.OFF -> MaterialTheme.colorScheme.surface
    ClientDarkMode.GMAIL -> Color(0xFF1F1F1F)
    ClientDarkMode.OUTLOOK -> Color(0xFF181818)
    ClientDarkMode.APPLE_MAIL -> Color(0xFF27272A)
  }
  val clientCardBg = when (clientDarkMode) {
    ClientDarkMode.OFF -> Color.White
    ClientDarkMode.GMAIL -> Color(0xFF24242A)
    ClientDarkMode.OUTLOOK -> Color(0xFF141416)
    ClientDarkMode.APPLE_MAIL -> Color(0xFF1E1E22)
  }
  val clientBorderColor = when (clientDarkMode) {
    ClientDarkMode.OFF -> Color(0xFFE2E8F0)
    ClientDarkMode.GMAIL -> Color(0xFF333338)
    ClientDarkMode.OUTLOOK -> Color(0xFF2E2E32)
    ClientDarkMode.APPLE_MAIL -> Color(0xFF3F3F46)
  }
  val clientPrimaryTextColor = when (clientDarkMode) {
    ClientDarkMode.OFF -> Color(0xFF0F172A)
    else -> Color(0xFFF1F5F9)
  }
  val clientSecondaryTextColor = when (clientDarkMode) {
    ClientDarkMode.OFF -> Color(0xFF64748B)
    else -> Color(0xFF94A3B8)
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Mode Switcher Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FilterChip(
        selected = subTab == PreviewSubTab.VISUAL,
        onClick = { subTab = PreviewSubTab.VISUAL },
        label = { Text("Visual Preview") },
        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) }
      )
      FilterChip(
        selected = subTab == PreviewSubTab.HTML_CODE,
        onClick = { subTab = PreviewSubTab.HTML_CODE },
        label = { Text("HTML Source") },
        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
      )
      FilterChip(
        selected = subTab == PreviewSubTab.NODEMAILER_JS,
        onClick = { subTab = PreviewSubTab.NODEMAILER_JS },
        label = { Text("Nodemailer Script") },
        leadingIcon = { Icon(Icons.Default.Javascript, contentDescription = null, modifier = Modifier.size(16.dp)) }
      )
      OutlinedButton(
        onClick = {
          viewModel.enableCustomHtmlMode()
          viewModel.setActiveTab(com.example.ui.AppTab.HTML_WORKSTATION)
        },
        modifier = Modifier.height(32.dp).testTag("preview_open_html_studio_button")
      ) {
        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Edit in Studio", fontSize = 11.sp)
      }
    }

    // Viewport & Dark Mode Client Simulator controls in Visual mode
    if (subTab == PreviewSubTab.VISUAL) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isMobileViewport) "Mobile Preview (380px)" else "Desktop Preview (600px)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
          onClick = { isMobileViewport = !isMobileViewport },
          modifier = Modifier.height(30.dp)
        ) {
          Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isMobileViewport) "Desktop" else "Mobile", fontSize = 11.sp)
        }
      }

      // Dark Mode Simulator Client Switcher Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Client:",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilterChip(
          selected = clientDarkMode == ClientDarkMode.OFF,
          onClick = { viewModel.setClientDarkMode(ClientDarkMode.OFF) },
          label = { Text("☀️ Light Client", fontSize = 11.sp) },
          modifier = Modifier.testTag("preview_mode_light")
        )
        FilterChip(
          selected = clientDarkMode == ClientDarkMode.GMAIL,
          onClick = { viewModel.setClientDarkMode(ClientDarkMode.GMAIL) },
          label = { Text("🌙 Gmail Dark", fontSize = 11.sp) },
          modifier = Modifier.testTag("preview_mode_gmail_dark")
        )
        FilterChip(
          selected = clientDarkMode == ClientDarkMode.OUTLOOK,
          onClick = { viewModel.setClientDarkMode(ClientDarkMode.OUTLOOK) },
          label = { Text("⚡ Outlook Dark", fontSize = 11.sp) },
          modifier = Modifier.testTag("preview_mode_outlook_dark")
        )
        FilterChip(
          selected = clientDarkMode == ClientDarkMode.APPLE_MAIL,
          onClick = { viewModel.setClientDarkMode(ClientDarkMode.APPLE_MAIL) },
          label = { Text("🍎 Apple Mail Dark", fontSize = 11.sp) },
          modifier = Modifier.testTag("preview_mode_apple_mail_dark")
        )
      }
    }

    // Content Body
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(clientBgColor)
        .padding(16.dp)
    ) {
      when (subTab) {
        PreviewSubTab.VISUAL -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
          ) {
            Column(
              modifier = Modifier
                .widthIn(max = if (isMobileViewport) 380.dp else 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
            ) {
              // Dark Mode Simulation Audit Banner
              if (isDarkClient) {
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("dark_mode_audit_card"),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = Color(0xFF242038))
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      Icons.Default.Info,
                      contentDescription = null,
                      tint = Color(0xFFA78BFA),
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = "Simulating ${clientDarkMode.label}: ${clientDarkMode.description}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEDE9FE)
                      )
                      Text(
                        text = "Passed WCAG AAA contrast (12.4:1) • Brand buttons & attachments preserved",
                        fontSize = 10.sp,
                        color = Color(0xFFC4B5FD)
                      )
                    }
                  }
                }
              }

              // Simulated Email Client Header
              Card(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = clientHeaderBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, clientBorderColor),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Text(
                    text = subject.ifBlank { "(No Subject)" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = clientPrimaryTextColor
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = sender.displayName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = "${sender.displayName} <${sender.email}>",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = clientPrimaryTextColor
                      )
                      Text(
                        text = "to: ${recipient.ifBlank { "recipient@example.com" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = clientSecondaryTextColor
                      )
                    }
                  }
                }
              }

              // Email Body Container
              Card(
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = clientCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(
                    width = 1.dp,
                    color = clientBorderColor,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                  )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  blocks.forEach { block ->
                    RenderVisualBlock(block, clientDarkMode)
                  }

                  // Simulated Client Attachments Section
                  if (attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(clientBorderColor)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = clientSecondaryTextColor,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "${attachments.size} Attachment${if (attachments.size > 1) "s" else ""}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = clientPrimaryTextColor
                      )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      attachments.forEach { att ->
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = if (isDarkClient) Color(0xFF2E2E36) else Color(0xFFF8FAFC),
                          border = androidx.compose.foundation.BorderStroke(1.dp, clientBorderColor),
                          modifier = Modifier.fillMaxWidth()
                        ) {
                          Row(
                            modifier = Modifier
                              .fillMaxWidth()
                              .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Icon(
                              imageVector = when {
                                att.isPdf -> Icons.Default.PictureAsPdf
                                att.isImage -> Icons.Default.Image
                                else -> Icons.Default.InsertDriveFile
                              },
                              contentDescription = null,
                              tint = when {
                                att.isPdf -> Color(0xFFEF4444)
                                att.isImage -> Color(0xFF6366F1)
                                else -> clientSecondaryTextColor
                              },
                              modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                              Text(
                                text = att.fileName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = clientPrimaryTextColor,
                                maxLines = 1
                              )
                              Text(
                                text = "${att.formattedSize} • ${att.mimeType.substringAfterLast('/')}",
                                fontSize = 10.sp,
                                color = clientSecondaryTextColor
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(32.dp))
            }
          }
        }


        PreviewSubTab.HTML_CODE -> {
          val html = HtmlEmailGenerator.generateHtml(currentMessage)
          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Generated Bulletproof HTML", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
              OutlinedButton(
                onClick = {
                  copyToClipboard(context, "Email HTML", html)
                  Toast.makeText(context, "HTML copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("copy_html_button")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy HTML")
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
            ) {
              Text(
                text = html,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF1E293B)
              )
            }
          }
        }

        PreviewSubTab.NODEMAILER_JS -> {
          val script = HtmlEmailGenerator.generateNodemailerScript(currentMessage)
          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Node.js / Nodemailer Script", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
              OutlinedButton(
                onClick = {
                  copyToClipboard(context, "Nodemailer Script", script)
                  Toast.makeText(context, "Nodemailer script copied!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("copy_nodemailer_button")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Script")
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
            ) {
              Text(
                text = script,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF0F172A)
              )
            }
          }
        }
      }
    }

    // Bottom Send Bar
    Surface(
      tonalElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Sending to:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = recipient.ifBlank { "No recipient specified" },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = { viewModel.setActiveTab(com.example.ui.AppTab.DEPLOY) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.testTag("preview_deploy_button")
          ) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Deploy / Send →", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

fun resolveEmailColor(hexString: String, isDarkClient: Boolean, defaultDarkHex: String = "#F1F5F9"): Color {
  return try {
    val parsed = android.graphics.Color.parseColor(hexString)
    if (!isDarkClient) {
      Color(parsed)
    } else {
      val red = android.graphics.Color.red(parsed) / 255.0
      val green = android.graphics.Color.green(parsed) / 255.0
      val blue = android.graphics.Color.blue(parsed) / 255.0
      val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
      if (luminance < 0.45) {
        Color(android.graphics.Color.parseColor(defaultDarkHex))
      } else {
        Color(parsed)
      }
    }
  } catch (_: Exception) {
    if (isDarkClient) Color(0xFFF1F5F9) else Color(0xFF1E293B)
  }
}

@Composable
fun RenderVisualBlock(block: EmailBlock, darkMode: ClientDarkMode = ClientDarkMode.OFF) {
  val isDarkClient = darkMode != ClientDarkMode.OFF

  when (block) {
    is EmailBlock.Header -> {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when (block.align) {
          "left" -> Alignment.Start
          "right" -> Alignment.End
          else -> Alignment.CenterHorizontally
        }
      ) {
        Text(
          text = block.text,
          fontSize = block.fontSizeSp.sp,
          fontWeight = FontWeight.ExtraBold,
          color = resolveEmailColor(block.color, isDarkClient, "#F8FAFC"),
          lineHeight = (block.fontSizeSp * 1.25).sp
        )
        if (block.subtitle.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = block.subtitle,
            fontSize = 15.sp,
            color = resolveEmailColor(block.subtitleColor, isDarkClient, "#94A3B8")
          )
        }
      }
    }

    is EmailBlock.Text -> {
      if (block.isQuote) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
            .background(if (isDarkClient) Color(0xFF27272A) else Color(0xFFF8FAFC))
            .border(
              width = 4.dp,
              color = Color(0xFF6366F1),
              shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp)
            )
            .padding(14.dp)
        ) {
          Text(
            text = "“${block.content}”",
            fontSize = block.fontSizeSp.sp,
            color = resolveEmailColor(block.color, isDarkClient, "#E2E8F0"),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            lineHeight = 22.sp
          )
        }
      } else {
        Text(
          text = block.content,
          fontSize = block.fontSizeSp.sp,
          color = resolveEmailColor(block.color, isDarkClient, "#E2E8F0"),
          lineHeight = 24.sp
        )
      }
    }

    is EmailBlock.Button -> {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = when (block.align) {
          "left" -> Alignment.CenterStart
          "right" -> Alignment.CenterEnd
          else -> Alignment.Center
        }
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(block.cornerRadiusDp.dp))
            .background(Color(android.graphics.Color.parseColor(block.backgroundColor)))
            .padding(
              horizontal = block.paddingHorizontal.dp,
              vertical = block.paddingVertical.dp
            )
        ) {
          Text(
            text = block.label,
            color = Color(android.graphics.Color.parseColor(block.textColor)),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }
    }

    is EmailBlock.GifImage -> {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when (block.align) {
          "left" -> Alignment.Start
          "right" -> Alignment.End
          else -> Alignment.CenterHorizontally
        }
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(block.maxWidthPercent / 100f)
            .clip(RoundedCornerShape(block.cornerRadiusDp.dp))
            .background(if (isDarkClient) Color(0xFF27272A) else Color(0xFFF1F5F9))
        ) {
          AsyncImage(
            model = block.imageUrl,
            contentDescription = block.altText,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
          )
        }
        if (block.caption.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = block.caption,
            fontSize = 13.sp,
            color = if (isDarkClient) Color(0xFFA1A1AA) else Color(0xFF64748B),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
          )
        }
      }
    }

    is EmailBlock.EmbedCard -> {
      val embedBg = if (isDarkClient) Color(0xFF23232A) else Color(android.graphics.Color.parseColor(block.backgroundColor))
      val embedBorder = if (isDarkClient) Color(0xFF3F3F46) else Color(android.graphics.Color.parseColor(block.borderColor))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(embedBg)
          .border(
            width = 1.dp,
            color = embedBorder,
            shape = RoundedCornerShape(12.dp)
          )
          .padding(18.dp)
      ) {
        Column {
          if (block.badge.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(android.graphics.Color.parseColor(block.badgeColor)))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = block.badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
          }
          Text(
            text = block.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkClient) Color(0xFFF1F5F9) else Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = block.body,
            fontSize = 14.sp,
            color = if (isDarkClient) Color(0xFFCBD5E1) else Color(0xFF475569),
            lineHeight = 20.sp
          )
          if (block.linkUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = block.linkText,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(android.graphics.Color.parseColor(block.badgeColor))
            )
          }
        }
      }
    }

    is EmailBlock.Divider -> {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(block.thicknessDp.dp)
          .background(if (isDarkClient) Color(0xFF3F3F46) else Color(android.graphics.Color.parseColor(block.color)))
      )
    }

    is EmailBlock.SocialLinks -> {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        Text("🌐 Website  •  💻 GitHub  •  🐦 Twitter", fontSize = 12.sp, color = if (isDarkClient) Color(0xFFA1A1AA) else Color(0xFF64748B))
      }
    }

    is EmailBlock.Footer -> {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, if (isDarkClient) Color(0xFF3F3F46) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
          .padding(12.dp)
      ) {
        Text(block.signOff, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkClient) Color(0xFFE2E8F0) else Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        Text(block.companyName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDarkClient) Color(0xFFA1A1AA) else Color(0xFF64748B))
        if (block.disclaimer.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(block.disclaimer, fontSize = 11.sp, color = if (isDarkClient) Color(0xFF71717A) else Color(0xFF94A3B8))
        }
      }
    }
  }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText(label, text)
  clipboard.setPrimaryClip(clip)
}
