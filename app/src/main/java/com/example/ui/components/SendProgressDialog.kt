package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.SendState

@Composable
fun SendProgressDialog(
  sendState: SendState,
  onDismiss: () -> Unit,
  onOpenSettings: () -> Unit,
  onSendViaDeviceApp: (() -> Unit)? = null
) {
  if (sendState is SendState.Idle) return

  val context = LocalContext.current
  var showLogs by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = {
      if (sendState !is SendState.Sending) onDismiss()
    },
    properties = DialogProperties(
      dismissOnBackPress = sendState !is SendState.Sending,
      dismissOnClickOutside = sendState !is SendState.Sending,
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.surface),
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        when (sendState) {
          is SendState.Sending -> {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
              )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
              text = "Dispatching via Gmail SMTP...",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Streaming multipart HTML, buttons & media payload over TLS 465",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                .padding(12.dp)
                .heightIn(max = 140.dp)
                .verticalScroll(rememberScrollState())
            ) {
              sendState.logs.forEach { log ->
                Text(
                  text = log,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = Color(0xFF334155)
                )
              }
            }
          }

          is SendState.Success -> {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
              text = "Email Dispatched to Server! 🚀",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF15803D)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = sendState.message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Self-Send or Destination Guidance Box
            if (sendState.isSelfSend) {
              Spacer(modifier = Modifier.height(12.dp))
              Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sent to yourself (${sendState.recipient})?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    "In Gmail, messages sent from your own account to your own account are saved in your 'Sent' and 'All Mail' folders (or inside an existing thread) rather than an unread Inbox notification. Check your 'Sent' folder in Gmail!",
                    fontSize = 11.sp,
                    color = Color(0xFF78350F),
                    lineHeight = 15.sp
                  )
                }
              }
            } else if (sendState.recipient.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Recipient: ${sendState.recipient} • Check Spam/Promotions if not visible in Primary Inbox.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Open Gmail directly to check Sent / Inbox
            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                  addCategory(Intent.CATEGORY_APP_EMAIL)
                  flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                  context.startActivity(intent)
                } catch (_: Exception) {
                  val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com"))
                  context.startActivity(webIntent)
                }
              },
              modifier = Modifier.fillMaxWidth().testTag("open_gmail_after_send_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Open Gmail (Check Sent / Inbox)", fontSize = 12.sp)
            }

            // Expandable transmission logs
            if (sendState.logs.isNotEmpty()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (showLogs) "Hide Server Logs ▲" else "View Server Transmission Logs ▼",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                  .clickable { showLogs = !showLogs }
                  .padding(4.dp)
              )
              if (showLogs) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState())
                ) {
                  sendState.logs.forEach { log ->
                    Text(
                      text = log,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.sp,
                      color = Color(0xFF334155)
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onDismiss,
              modifier = Modifier.fillMaxWidth().testTag("dismiss_success_button"),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
              Text("Done")
            }
          }

          is SendState.Error -> {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEE2E2)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
              text = "Email Delivery Failed",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFB91C1C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = sendState.error,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
            if (sendState.logs.isNotEmpty()) {
              Spacer(modifier = Modifier.height(12.dp))
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                  .padding(10.dp)
                  .heightIn(max = 120.dp)
                  .verticalScroll(rememberScrollState())
              ) {
                sendState.logs.forEach { log ->
                  Text(
                    text = log,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFFB91C1C)
                  )
                }
              }
            }
            if (onSendViaDeviceApp != null) {
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = {
                  onDismiss()
                  onSendViaDeviceApp()
                },
                modifier = Modifier.fillMaxWidth().testTag("send_via_device_fallback_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send via Gmail App Instead (100% Inbox)", fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
              ) {
                Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Button(
                onClick = {
                  onDismiss()
                  onOpenSettings()
                },
                modifier = Modifier.weight(1f).testTag("fix_credentials_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Text("Fix Settings")
              }
            }
          }

          else -> {}
        }
      }
    }
  }
}
