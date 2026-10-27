package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import com.example.ui.screens.DraftsScreen
import com.example.ui.theme.NeuIconButton
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.MailCraftViewModel
import com.example.ui.components.AccountSettingsDialog
import com.example.ui.components.SendProgressDialog
import com.example.ui.screens.ComposeScreen
import com.example.ui.screens.DeployScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HtmlWorkstationScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MailCraftViewModel = viewModel()
      val isDark by viewModel.isAppDarkMode.collectAsState()
      MyApplicationTheme(darkTheme = isDark) {
        MailCraftApp(viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailCraftApp(viewModel: MailCraftViewModel = viewModel()) {
  val activeTab by viewModel.activeTab.collectAsState()
  val senderAccount by viewModel.senderAccount.collectAsState()
  val sendState by viewModel.sendState.collectAsState()
  val isTestingConnection by viewModel.isTestingConnection.collectAsState()
  val testConnectionResult by viewModel.testConnectionResult.collectAsState()
  val blocks by viewModel.blocks.collectAsState()
  val drafts by viewModel.savedDrafts.collectAsState()
  val isDark by viewModel.isAppDarkMode.collectAsState()

  val context = LocalContext.current
  var showSettingsDialog by remember { mutableStateOf(false) }

  // Account Settings Dialog
  if (showSettingsDialog) {
    AccountSettingsDialog(
      currentAccount = senderAccount,
      isTesting = isTestingConnection,
      testResult = testConnectionResult,
      onDismiss = {
        showSettingsDialog = false
        viewModel.clearTestConnectionResult()
      },
      onSave = { updated ->
        viewModel.updateSenderAccount(updated)
      },
      onTestConnection = { account ->
        viewModel.testGmailConnection(account)
      },
      onClearTestResult = {
        viewModel.clearTestConnectionResult()
      }
    )
  }

  // Real-time Send Progress Dialog
  SendProgressDialog(
    sendState = sendState,
    onDismiss = { viewModel.resetSendState() },
    onOpenSettings = { showSettingsDialog = true },
    onSendViaDeviceApp = { viewModel.sendViaDeviceEmailApp(context) }
  )

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_mail_craft_logo),
              contentDescription = "MailCraft Logo",
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "MailCraft",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp
            )
          }
        },
        actions = {
          // Neumorphic Dark Mode Toggle
          NeuIconButton(
            onClick = { viewModel.toggleAppDarkMode() },
            isDark = isDark,
            modifier = Modifier.size(34.dp).testTag("dark_mode_toggle_button")
          ) {
            Icon(
              if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle Dark Mode",
              tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF6366F1),
              modifier = Modifier.size(17.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Saved Drafts Quick Button with Live Count Badge
          NeuIconButton(
            onClick = { viewModel.setActiveTab(AppTab.DRAFTS) },
            isDark = isDark,
            modifier = Modifier.size(34.dp).testTag("top_bar_drafts_button")
          ) {
            BadgedBox(
              badge = {
                if (drafts.isNotEmpty()) {
                  Badge { Text("${drafts.size}") }
                }
              }
            ) {
              Icon(
                Icons.Default.Drafts,
                contentDescription = "Drafts",
                tint = if (activeTab == AppTab.DRAFTS) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Gmail Connection Status Pill
          val hasPassword = senderAccount.appPassword.isNotBlank()
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (hasPassword) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
              .clickable { showSettingsDialog = true }
              .padding(horizontal = 9.dp, vertical = 6.dp)
              .testTag("status_account_badge")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (hasPassword) Color(0xFF16A34A) else Color(0xFFD97706))
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = if (hasPassword) "Ready" else "Setup",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasPassword) Color(0xFF15803D) else Color(0xFFB45309)
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // PFP (Profile Picture) Avatar Button
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { showSettingsDialog = true }
              .testTag("top_bar_pfp_button"),
            contentAlignment = Alignment.Center
          ) {
            val preset = senderAccount.avatarPreset
            val customUrl = senderAccount.avatarUrl
            if (preset == "avatar_custom" && customUrl.isNotBlank()) {
              AsyncImage(
                model = customUrl,
                contentDescription = "PFP Avatar",
                modifier = Modifier.size(34.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
              )
            } else if (preset == "avatar_monogram") {
              val letter = (senderAccount.displayName.ifBlank { senderAccount.email }).take(1).uppercase()
              Text(
                text = letter.ifBlank { "U" },
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
              )
            } else if (preset == "avatar_rocket") {
              Icon(Icons.Default.RocketLaunch, contentDescription = "PFP", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            } else if (preset == "avatar_code") {
              Icon(Icons.Default.Code, contentDescription = "PFP", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            } else if (preset == "avatar_heart") {
              Text("❤️", fontSize = 14.sp)
            } else if (preset == "avatar_sparkles") {
              Text("✨", fontSize = 14.sp)
            } else {
              Text("🟣", fontSize = 14.sp)
            }
          }

          IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier.testTag("top_bar_settings_button")
          ) {
            Icon(
              Icons.Default.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          selected = activeTab == AppTab.COMPOSE,
          onClick = { viewModel.setActiveTab(AppTab.COMPOSE) },
          icon = {
            BadgedBox(
              badge = {
                if (blocks.isNotEmpty()) {
                  Badge { Text("${blocks.size}") }
                }
              }
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Visual")
            }
          },
          label = { Text("Visual") },
          modifier = Modifier.testTag("nav_compose_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.HTML_WORKSTATION,
          onClick = {
            viewModel.enableCustomHtmlMode()
            viewModel.setActiveTab(AppTab.HTML_WORKSTATION)
          },
          icon = { Icon(Icons.Default.Code, contentDescription = "HTML Studio") },
          label = { Text("HTML") },
          modifier = Modifier.testTag("nav_html_workstation_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.PREVIEW,
          onClick = { viewModel.setActiveTab(AppTab.PREVIEW) },
          icon = { Icon(Icons.Default.Visibility, contentDescription = "Preview") },
          label = { Text("Preview") },
          modifier = Modifier.testTag("nav_preview_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.DRAFTS,
          onClick = { viewModel.setActiveTab(AppTab.DRAFTS) },
          icon = {
            BadgedBox(
              badge = {
                if (drafts.isNotEmpty()) {
                  Badge { Text("${drafts.size}") }
                }
              }
            ) {
              Icon(Icons.Default.Drafts, contentDescription = "Drafts")
            }
          },
          label = { Text("Drafts") },
          modifier = Modifier.testTag("nav_drafts_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.DEPLOY,
          onClick = { viewModel.setActiveTab(AppTab.DEPLOY) },
          icon = { Icon(Icons.Default.RocketLaunch, contentDescription = "Deploy") },
          label = { Text("Deploy") },
          modifier = Modifier.testTag("nav_deploy_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.HISTORY,
          onClick = { viewModel.setActiveTab(AppTab.HISTORY) },
          icon = { Icon(Icons.Default.History, contentDescription = "Sent Log") },
          label = { Text("Sent") },
          modifier = Modifier.testTag("nav_history_tab")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (activeTab) {
        AppTab.COMPOSE -> ComposeScreen(
          viewModel = viewModel,
          onOpenSettings = { showSettingsDialog = true }
        )
        AppTab.HTML_WORKSTATION -> HtmlWorkstationScreen(viewModel = viewModel)
        AppTab.PREVIEW -> PreviewScreen(viewModel = viewModel)
        AppTab.DRAFTS -> DraftsScreen(viewModel = viewModel)
        AppTab.DEPLOY -> DeployScreen(
          viewModel = viewModel,
          onOpenSettings = { showSettingsDialog = true }
        )
        AppTab.TEMPLATES -> TemplatesScreen(viewModel = viewModel)
        AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
      }
    }
  }
}
