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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalDensity
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
import com.example.ui.screens.AiBotScreen
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
      isDark = isDark,
      onToggleDarkMode = { viewModel.toggleAppDarkMode() },
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
      val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
      if (!isImeVisible) {
        NavigationBar(
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
        NavigationBarItem(
          selected = activeTab == AppTab.COMPOSE,
          onClick = { viewModel.setActiveTab(AppTab.COMPOSE) },
          alwaysShowLabel = false,
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
          modifier = Modifier.testTag("nav_compose_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.HTML_WORKSTATION,
          onClick = {
            viewModel.enableCustomHtmlMode()
            viewModel.setActiveTab(AppTab.HTML_WORKSTATION)
          },
          alwaysShowLabel = false,
          icon = { Icon(Icons.Default.Code, contentDescription = "HTML") },
          modifier = Modifier.testTag("nav_html_workstation_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.AI_BOT,
          onClick = { viewModel.setActiveTab(AppTab.AI_BOT) },
          alwaysShowLabel = false,
          icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Bot") },
          modifier = Modifier.testTag("nav_ai_bot_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.PREVIEW,
          onClick = { viewModel.setActiveTab(AppTab.PREVIEW) },
          alwaysShowLabel = false,
          icon = { Icon(Icons.Default.Visibility, contentDescription = "Preview") },
          modifier = Modifier.testTag("nav_preview_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.DRAFTS,
          onClick = { viewModel.setActiveTab(AppTab.DRAFTS) },
          alwaysShowLabel = false,
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
          modifier = Modifier.testTag("nav_drafts_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.DEPLOY,
          onClick = { viewModel.setActiveTab(AppTab.DEPLOY) },
          alwaysShowLabel = false,
          icon = { Icon(Icons.Default.RocketLaunch, contentDescription = "Deploy") },
          modifier = Modifier.testTag("nav_deploy_tab")
        )

        NavigationBarItem(
          selected = activeTab == AppTab.HISTORY,
          onClick = { viewModel.setActiveTab(AppTab.HISTORY) },
          alwaysShowLabel = false,
          icon = { Icon(Icons.Default.History, contentDescription = "Sent") },
          modifier = Modifier.testTag("nav_history_tab")
        )
      }
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
        AppTab.AI_BOT -> AiBotScreen(viewModel = viewModel)
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
