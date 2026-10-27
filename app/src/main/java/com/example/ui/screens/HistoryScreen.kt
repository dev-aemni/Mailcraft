package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MailCraftViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MailCraftViewModel) {
  val sentEmails by viewModel.sentEmails.collectAsState()

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sent History & Logs",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "Track real-time Gmail SMTP transmissions and delivery receipts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (sentEmails.isNotEmpty()) {
          OutlinedButton(
            onClick = { viewModel.clearSentHistory() },
            modifier = Modifier.testTag("clear_history_button")
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear", fontSize = 12.sp)
          }
        }
      }
    }

    if (sentEmails.isEmpty()) {
      item {
        Spacer(modifier = Modifier.height(60.dp))
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Mail,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "No Sent Emails Yet",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Emails sent via Gmail SMTP will appear here with delivery receipts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(sentEmails, key = { it.id }) { item ->
        val isDelivered = item.status == "DELIVERED"
        val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(item.sentTimestamp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
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
                    .background(if (isDelivered) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    if (isDelivered) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isDelivered) Color(0xFF16A34A) else Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isDelivered) "Delivered via Gmail" else "Delivery Failed",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDelivered) Color(0xFF15803D) else Color(0xFFB91C1C)
                )
              }

              IconButton(
                onClick = { viewModel.deleteSentEmail(item.id) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  Icons.Default.Delete,
                  contentDescription = "Delete record",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = item.subject,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "To: ${item.toEmail}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )

            if (item.previewSnippet.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = item.previewSnippet,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )
            }

            if (!isDelivered && item.errorMessage != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Error: ${item.errorMessage}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB91C1C)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = dateStr,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }
      }
    }
  }
}
