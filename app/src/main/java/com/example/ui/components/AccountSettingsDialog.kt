package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.SenderAccount
import com.example.model.SmtpSendResult
import com.example.ui.theme.PurpleOutline
import com.example.ui.theme.PurplePrimary

data class AvatarOption(
  val id: String,
  val label: String,
  val emoji: String? = null,
  val icon: ImageVector? = null,
  val backgroundBrush: Brush? = null
)

@Composable
fun AccountSettingsDialog(
  currentAccount: SenderAccount,
  isTesting: Boolean,
  testResult: SmtpSendResult?,
  onDismiss: () -> Unit,
  onSave: (SenderAccount) -> Unit,
  onTestConnection: (SenderAccount) -> Unit,
  onClearTestResult: () -> Unit
) {
  val context = LocalContext.current
  var email by remember { mutableStateOf(currentAccount.email) }
  var displayName by remember { mutableStateOf(currentAccount.displayName) }
  var appPassword by remember { mutableStateOf(currentAccount.appPassword) }
  var isPasswordVisible by remember { mutableStateOf(false) }

  var avatarPreset by remember { mutableStateOf(currentAccount.avatarPreset) }
  var avatarUrl by remember { mutableStateOf(currentAccount.avatarUrl) }

  androidx.compose.runtime.LaunchedEffect(currentAccount) {
    email = currentAccount.email
    displayName = currentAccount.displayName
    appPassword = currentAccount.appPassword
    avatarPreset = currentAccount.avatarPreset
    avatarUrl = currentAccount.avatarUrl
  }

  val avatarOptions = remember {
    listOf(
      AvatarOption("avatar_purple_orb", "Purple Orb", emoji = "🟣"),
      AvatarOption("avatar_rocket", "Rocket", icon = Icons.Default.RocketLaunch),
      AvatarOption("avatar_sparkles", "Sparkles", icon = Icons.Default.Star),
      AvatarOption("avatar_code", "Dev", icon = Icons.Default.Code),
      AvatarOption("avatar_heart", "Heart", icon = Icons.Default.Favorite),
      AvatarOption("avatar_monogram", "Initial", emoji = "🔤"),
      AvatarOption("avatar_custom", "Custom URL", icon = Icons.Default.Language)
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.surface),
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Account & Credentials",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Configure Gmail SMTP and Profile",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Picture (PFP) Selector Section
        Text(
          text = "Profile Picture (PFP)",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Large PFP Preview
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            if (avatarPreset == "avatar_custom" && avatarUrl.isNotBlank()) {
              AsyncImage(
                model = avatarUrl,
                contentDescription = "Custom PFP",
                modifier = Modifier.size(60.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
              )
            } else if (avatarPreset == "avatar_monogram") {
              val letter = (displayName.ifBlank { email }).take(1).uppercase()
              Text(
                text = letter.ifBlank { "U" },
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
              )
            } else {
              val currentOption = avatarOptions.find { it.id == avatarPreset }
              if (currentOption?.emoji != null) {
                Text(currentOption.emoji, fontSize = 28.sp)
              } else if (currentOption?.icon != null) {
                Icon(currentOption.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
              } else {
                Text("🟣", fontSize = 28.sp)
              }
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          // Avatar Preset Chips (Scrollable)
          Row(
            modifier = Modifier
              .weight(1f)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            avatarOptions.forEach { opt ->
              val isSelected = opt.id == avatarPreset
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else PurpleOutline,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { avatarPreset = opt.id }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (opt.emoji != null) {
                    Text(opt.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                  } else if (opt.icon != null) {
                    Icon(opt.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF6B21A8))
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = opt.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }

        // Custom URL input if selected
        if (avatarPreset == "avatar_custom") {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = avatarUrl,
            onValueChange = { avatarUrl = it },
            label = { Text("PFP Image URL (HTTPS)") },
            placeholder = { Text("https://example.com/avatar.jpg") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_pfp_url_input"),
            shape = RoundedCornerShape(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Explanation Card: "What is that password type thing?"
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "What is the \"Password-Type Thing\"?",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Google requires a 16-character App Password (not your standard Google login password) to allow Nodemailer / SMTP apps to send emails securely via Gmail without compromising your main account.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = {
                val intent = Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse("https://myaccount.google.com/apppasswords")
                )
                context.startActivity(intent)
              },
              modifier = Modifier.fillMaxWidth().testTag("open_app_passwords_button")
            ) {
              Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Generate App Password at Google", fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Variables Info Card (i)
        Card(
          colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F3FF)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().border(1.dp, PurpleOutline, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Template Variables for Code & HTML (i)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PurplePrimary
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("• {{MyEmail}} = Your sender email (${email.ifBlank { "xxx@xxx.xxx" }})", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF334155))
            Text("• {{RecipientEmail}} = Target recipient email", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF334155))
            Text("• {{MyPassword}} = Google App Password (protected)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF334155))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sender Email Field
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Your Gmail Address (Sender)") },
          placeholder = { Text("your.email@gmail.com") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          modifier = Modifier.fillMaxWidth().testTag("sender_email_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Display Name Field
        OutlinedTextField(
          value = displayName,
          onValueChange = { displayName = it },
          label = { Text("Display Name") },
          placeholder = { Text("e.g. Alex (or My Company)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("display_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // App Password Field
        OutlinedTextField(
          value = appPassword,
          onValueChange = { appPassword = it },
          label = { Text("16-Character Google App Password") },
          placeholder = { Text("e.g. abcd efgh ijkl mnop") },
          singleLine = true,
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
              )
            }
          },
          modifier = Modifier.fillMaxWidth().testTag("app_password_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Test Connection Result banner
        if (testResult != null) {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (testResult.success) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  if (testResult.success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = if (testResult.success) Color(0xFF16A34A) else Color(0xFFDC2626),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (testResult.success) "SMTP Connection Verified!" else "Connection Diagnostic Failed",
                  fontWeight = FontWeight.Bold,
                  color = if (testResult.success) Color(0xFF15803D) else Color(0xFFB91C1C)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = testResult.message,
                style = MaterialTheme.typography.bodySmall,
                color = if (testResult.success) Color(0xFF166534) else Color(0xFF991B1B)
              )
              if (testResult.logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState())
                ) {
                  testResult.logs.forEach { log ->
                    Text(
                      text = log.message,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = if (log.isError) Color(0xFFB91C1C) else Color(0xFF334155)
                    )
                  }
                }
              }
            }
          }
        }

        // Actions: Test & Save
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              onClearTestResult()
              onTestConnection(
                SenderAccount(
                  email = email,
                  displayName = displayName,
                  appPassword = appPassword,
                  avatarPreset = avatarPreset,
                  avatarUrl = avatarUrl
                )
              )
            },
            enabled = !isTesting && email.isNotBlank(),
            modifier = Modifier.weight(1f).testTag("test_smtp_button")
          ) {
            if (isTesting) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Testing...")
            } else {
              Text("Test SMTP")
            }
          }

          Button(
            onClick = {
              onSave(
                SenderAccount(
                  email = email,
                  displayName = displayName,
                  appPassword = appPassword,
                  avatarPreset = avatarPreset,
                  avatarUrl = avatarUrl
                )
              )
              onDismiss()
            },
            modifier = Modifier.weight(1f).testTag("save_account_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Text("Save Credentials")
          }
        }
      }
    }
  }
}
