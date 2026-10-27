package com.example.ui.screens

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedDraftEntity
import com.example.ui.AppTab
import com.example.ui.MailCraftViewModel
import com.example.ui.theme.NeuAccent
import com.example.ui.theme.NeuButton
import com.example.ui.theme.NeuCard
import com.example.ui.theme.NeuInsetCard

@Composable
fun DraftsScreen(viewModel: MailCraftViewModel) {
  val context = LocalContext.current
  val drafts by viewModel.savedDrafts.collectAsState()
  val isDark by viewModel.isAppDarkMode.collectAsState()
  val currentDraftId by viewModel.currentDraftId.collectAsState()

  var showSaveDialog by remember { mutableStateOf(false) }
  var draftTitleInput by remember { mutableStateOf("") }
  var draftToDelete by remember { mutableStateOf<SavedDraftEntity?>(null) }

  // Save Dialog
  if (showSaveDialog) {
    AlertDialog(
      onDismissRequest = { showSaveDialog = false },
      title = {
        Text("Save Current Email as Draft", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Column {
          Text("Give your draft a recognizable title to resume editing later:", fontSize = 13.sp)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = draftTitleInput,
            onValueChange = { draftTitleInput = it },
            label = { Text("Draft Title") },
            placeholder = { Text("e.g. October Newsletter") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.saveCurrentDraft(draftTitleInput) {
              Toast.makeText(context, "Draft saved to Room storage!", Toast.LENGTH_SHORT).show()
            }
            showSaveDialog = false
            draftTitleInput = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeuAccent)
        ) {
          Text("Save Draft")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSaveDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Confirm Dialog
  draftToDelete?.let { draft ->
    AlertDialog(
      onDismissRequest = { draftToDelete = null },
      title = { Text("Delete Draft?", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to delete \"${draft.title}\"? This cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteDraft(draft.id)
            Toast.makeText(context, "Draft deleted", Toast.LENGTH_SHORT).show()
            draftToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { draftToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Neumorphic Header Card
    item {
      NeuCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark,
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(22.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(if (isDark) Color(0xFF312E81) else Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Drafts,
                  contentDescription = null,
                  tint = NeuAccent,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Saved Drafts",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                  text = "${drafts.size} draft(s) stored locally in Room SQLite",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Save Current Draft Button
            NeuButton(
              onClick = { showSaveDialog = true },
              isPrimary = true,
              isDark = isDark,
              modifier = Modifier.weight(1f).testTag("save_draft_action_button")
            ) {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save Current")
            }

            // Start New Draft Button
            NeuButton(
              onClick = {
                viewModel.createNewDraft()
                Toast.makeText(context, "Started fresh blank email draft!", Toast.LENGTH_SHORT).show()
              },
              isDark = isDark,
              modifier = Modifier.weight(1f).testTag("new_blank_draft_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("New Blank")
            }
          }
        }
      }
    }

    if (drafts.isEmpty()) {
      item {
        NeuInsetCard(
          modifier = Modifier.fillMaxWidth(),
          isDark = isDark,
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(36.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.Drafts,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No Saved Drafts Yet",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap 'Save Current' or the draft icon in the editor to store your email and resume later.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp,
              modifier = Modifier.padding(horizontal = 16.dp)
            )
          }
        }
      }
    } else {
      items(drafts, key = { it.id }) { draft ->
        val isCurrent = draft.id == currentDraftId
        val relativeTime = DateUtils.getRelativeTimeSpanString(
          draft.updatedTimestamp,
          System.currentTimeMillis(),
          DateUtils.MINUTE_IN_MILLIS
        ).toString()

        NeuCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("draft_item_${draft.id}"),
          isDark = isDark,
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(18.dp)
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (draft.isCustomHtml) Color(0xFFE44D26).copy(alpha = 0.15f) else Color(0xFF6366F1).copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    if (draft.isCustomHtml) Icons.Default.Code else Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (draft.isCustomHtml) Color(0xFFE44D26) else Color(0xFF6366F1),
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = draft.title,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = MaterialTheme.colorScheme.onBackground
                    )
                    if (isCurrent) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .background(Color(0xFF10B981))
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = "ACTIVE",
                          fontSize = 9.sp,
                          color = Color.White,
                          fontWeight = FontWeight.ExtraBold
                        )
                      }
                    }
                  }
                  Text(
                    text = "Saved $relativeTime",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(
                onClick = { draftToDelete = draft },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  Icons.Default.DeleteOutline,
                  contentDescription = "Delete Draft",
                  tint = Color(0xFFEF4444),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details pill container
            NeuInsetCard(
              modifier = Modifier.fillMaxWidth(),
              isDark = isDark,
              cornerRadius = 14.dp,
              contentPadding = PaddingValues(10.dp)
            ) {
              Column {
                if (draft.recipient.isNotBlank()) {
                  Text(
                    text = "To: ${draft.recipient}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                  )
                }
                Text(
                  text = "Subject: ${draft.subject.ifBlank { "(No Subject)" }}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onBackground
                )
                if (draft.previewSnippet.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = draft.previewSnippet,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resume Editing Button
            NeuButton(
              onClick = {
                viewModel.loadDraft(draft)
                Toast.makeText(context, "Loaded \"${draft.title}\"! Resume editing now.", Toast.LENGTH_SHORT).show()
              },
              isPrimary = true,
              isDark = isDark,
              modifier = Modifier.fillMaxWidth().testTag("resume_draft_button_${draft.id}")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Resume Editing Draft →", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
