package com.example.ui.screens

import com.example.ui.components.MailCraftFooter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TemplatePresets
import com.example.ui.AppTab
import com.example.ui.MailCraftViewModel
import com.example.ui.theme.PurpleOutline
import com.example.ui.theme.PurplePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TemplatesScreen(viewModel: MailCraftViewModel) {
  val context = LocalContext.current
  val customTemplates by viewModel.customTemplates.collectAsState()
  val isDark by viewModel.isAppDarkMode.collectAsState()
  val builtInTemplates = TemplatePresets.allTemplates

  var selectedTab by remember { mutableStateOf(0) } // 0 = Internal, 1 = Built-in

  var pendingExportJson by remember { mutableStateOf<String?>(null) }
  var pendingExportFileName by remember { mutableStateOf("mailcraft_templates_backup.json") }

  val exportFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    uri?.let { destUri ->
      val json = pendingExportJson ?: return@let
      val success = viewModel.writeJsonToUri(context, destUri, json)
      if (success) {
        Toast.makeText(context, "✅ Template JSON saved to storage!", Toast.LENGTH_LONG).show()
      } else {
        Toast.makeText(context, "Failed to write JSON to storage.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val importFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let { sourceUri ->
      val json = viewModel.readJsonFromUri(context, sourceUri)
      if (!json.isNullOrBlank()) {
        viewModel.importTemplatesFromJson(json) { count, msg ->
          Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
      } else {
        Toast.makeText(context, "Failed to read selected JSON file.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header & Description
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Templates Library",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Internally saved designs & designer email presets",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs: My Saved Templates vs Built-in Presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            label = {
              Text("My Internal Templates (${customTemplates.size})", fontWeight = FontWeight.Bold)
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f).testTag("tab_internal_templates")
          )
          FilterChip(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            label = {
              Text("Built-in Presets (${builtInTemplates.size})", fontWeight = FontWeight.Bold)
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f).testTag("tab_builtin_templates")
          )
        }
      }
    }

    if (selectedTab == 0) {
      // 1. My Internal Saved Templates
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth().border(1.dp, PurpleOutline, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Save Current Email to Library",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Stores your active editor design in the local database.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Button(
                onClick = {
                  viewModel.saveCustomTemplate("Custom Email Design")
                  Toast.makeText(context, "Saved current design as template!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Now", fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // JSON Backup, Local Export & Import Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Export All Backup Button
              Button(
                onClick = {
                  if (customTemplates.isEmpty()) {
                    Toast.makeText(context, "No custom templates to export.", Toast.LENGTH_SHORT).show()
                  } else {
                    val json = viewModel.exportAllCustomTemplatesJson()
                    pendingExportJson = json
                    pendingExportFileName = "mailcraft_templates_backup.json"
                    exportFileLauncher.launch(pendingExportFileName)
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("btn_export_all_templates")
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Backup", fontSize = 11.5.sp)
              }

              // Share All Button
              OutlinedButton(
                onClick = {
                  if (customTemplates.isEmpty()) {
                    Toast.makeText(context, "No custom templates to share.", Toast.LENGTH_SHORT).show()
                  } else {
                    val json = viewModel.exportAllCustomTemplatesJson()
                    viewModel.shareCustomTemplateJson(
                      context,
                      json,
                      "mailcraft_templates_backup.json",
                      "Share All MailCraft Templates"
                    )
                  }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("btn_share_all_templates")
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", fontSize = 11.5.sp)
              }

              // Import JSON Button
              OutlinedButton(
                onClick = {
                  importFileLauncher.launch("application/json")
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("btn_import_templates")
              ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import JSON", fontSize = 11.5.sp)
              }
            }
          }
        }
      }

      if (customTemplates.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().border(1.dp, PurpleOutline, RoundedCornerShape(16.dp))
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("📄", fontSize = 40.sp)
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "No Internally Saved Templates Yet",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "You can design emails in HTML Studio or Visual Composer and save them here anytime!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = {
                  viewModel.enableCustomHtmlMode()
                  viewModel.setActiveTab(AppTab.HTML_WORKSTATION)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
              ) {
                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open HTML Studio to Create")
              }
            }
          }
        }
      } else {
        items(customTemplates, key = { it.id }) { template ->
          val dateStr = remember(template.createdTimestamp) {
            SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(template.createdTimestamp))
          }

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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(template.iconEmoji, fontSize = 20.sp)
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = template.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = dateStr,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }

                // Delete Button
                IconButton(
                  onClick = {
                    viewModel.deleteCustomTemplate(template.id)
                    Toast.makeText(context, "Deleted template", Toast.LENGTH_SHORT).show()
                  }
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = template.previewSnippet.ifBlank { "Custom saved internal HTML template." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Actions: Edit in HTML Studio, Deploy, Export, Share
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = {
                    viewModel.loadCustomTemplate(template)
                    viewModel.setActiveTab(AppTab.HTML_WORKSTATION)
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("HTML Studio", fontSize = 11.sp)
                }

                Button(
                  onClick = {
                    viewModel.loadCustomTemplate(template)
                    viewModel.setActiveTab(AppTab.DEPLOY)
                  },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Deploy / Send", fontSize = 11.sp)
                }

                // Export Single Template to Local JSON File
                IconButton(
                  onClick = {
                    val json = viewModel.exportSingleCustomTemplateJson(template)
                    val safeName = template.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").lowercase()
                    pendingExportJson = json
                    pendingExportFileName = "template_${safeName}.json"
                    exportFileLauncher.launch(pendingExportFileName)
                  }
                ) {
                  Icon(
                    Icons.Default.FileDownload,
                    contentDescription = "Export JSON File",
                    tint = Color(0xFF0284C7)
                  )
                }

                // Share Single Template JSON
                IconButton(
                  onClick = {
                    val json = viewModel.exportSingleCustomTemplateJson(template)
                    val safeName = template.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").lowercase()
                    viewModel.shareCustomTemplateJson(
                      context,
                      json,
                      "template_${safeName}.json",
                      "Share \"${template.title}\" Template"
                    )
                  }
                ) {
                  Icon(
                    Icons.Default.Share,
                    contentDescription = "Share Template JSON",
                    tint = Color(0xFF7C3AED)
                  )
                }

                // Copy HTML to Clipboard
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Template HTML", template.htmlContent))
                    Toast.makeText(context, "Copied template HTML to clipboard!", Toast.LENGTH_SHORT).show()
                  }
                ) {
                  Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy HTML",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    } else {
      // 2. Built-in Templates
      items(builtInTemplates, key = { it.id }) { template ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Text(template.iconEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${template.blocks.size} customizable blocks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = template.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Default Subject: \"${template.subject}\"",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.loadTemplate(template)
                  viewModel.enableCustomHtmlMode()
                  viewModel.setActiveTab(AppTab.HTML_WORKSTATION)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("In HTML Studio", fontSize = 12.sp)
              }

              Button(
                onClick = {
                  viewModel.loadTemplate(template)
                  viewModel.setActiveTab(AppTab.COMPOSE)
                },
                modifier = Modifier.weight(1f).testTag("use_template_${template.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("In Visual Composer", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }

    item {
      MailCraftFooter(isDark = isDark)
    }
  }
}
