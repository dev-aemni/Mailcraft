package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeuAccent

/**
 * Standard Footer across MailCraft screens: Home (Compose), Deploy, AI, Drafts, History, Templates.
 * Displays:
 * - 2026 @dev-aemni
 * - "To Request features/update" create an issue at https://github.com/dev-aemni/Mailcraft/issues
 * - Official Website: https://dev-aemni.github.io/Mailcraft/
 * - Help & Documentation: https://dev-aemni.github.io/Mailcraft/help/
 * - Releases & APK Download: https://github.com/dev-aemni/Mailcraft/releases
 */
@Composable
fun MailCraftFooter(
  isDark: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val websiteUrl = "https://dev-aemni.github.io/Mailcraft/"
  val helpUrl = "https://dev-aemni.github.io/Mailcraft/help/"
  val issuesUrl = "https://github.com/dev-aemni/Mailcraft/issues"
  val releasesUrl = "https://github.com/dev-aemni/Mailcraft/releases"

  val openUrl = { url: String ->
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 18.dp),
    color = if (isDark) Color(0xFF1B1D22) else Color(0xFFF1F5F9),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isDark) Color(0xFF2E323A) else Color(0xFFE2E8F0)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header: Project Branding & Creator
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "✉️ MailCraft",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "• 2026 @dev-aemni",
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = NeuAccent
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Native Android HTML Email Studio • Client-Side SMTP Gateway",
        fontSize = 11.sp,
        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Important URLs Section
      Text(
        text = "IMPORTANT PROJECT LINKS & DOCS",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9),
        letterSpacing = 0.5.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 1. Official Website Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { openUrl(websiteUrl) }
          .testTag("footer_website_button"),
        color = if (isDark) Color(0xFF242730) else Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isDark) Color(0xFF383E4C) else Color(0xFFE2E8F0)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              Icons.Default.Language,
              contentDescription = null,
              tint = Color(0xFF0284C7),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Official Webpage & Showcase",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E1B4B)
              )
              Text(
                text = "dev-aemni.github.io/Mailcraft",
                fontSize = 10.sp,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open Link",
            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Help & Documentation Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { openUrl(helpUrl) }
          .testTag("footer_help_button"),
        color = if (isDark) Color(0xFF242730) else Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isDark) Color(0xFF383E4C) else Color(0xFFE2E8F0)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              Icons.Default.HelpOutline,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Help & App Passwords Setup Guide",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E1B4B)
              )
              Text(
                text = "dev-aemni.github.io/Mailcraft/help",
                fontSize = 10.sp,
                color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF059669),
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open Link",
            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. GitHub Issue Tracker Card (Feature Requests & Updates)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { openUrl(issuesUrl) }
          .testTag("footer_github_issues_button"),
        color = if (isDark) Color(0xFF242730) else Color(0xFFEDE9FE),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isDark) Color(0xFF383E4C) else Color(0xFFDDD6FE)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              Icons.Default.BugReport,
              contentDescription = null,
              tint = NeuAccent,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "\"To Request features / update\":",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E1B4B)
              )
              Text(
                text = "Create an issue at github.com/dev-aemni/Mailcraft/issues",
                fontSize = 10.sp,
                color = NeuAccent,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open Link",
            tint = NeuAccent,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Download Release APK Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { openUrl(releasesUrl) }
          .testTag("footer_releases_button"),
        color = if (isDark) Color(0xFF242730) else Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isDark) Color(0xFF383E4C) else Color(0xFFE2E8F0)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              Icons.Default.CloudDownload,
              contentDescription = null,
              tint = Color(0xFF8B5CF6),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Releases & APK Downloads",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E1B4B)
              )
              Text(
                text = "github.com/dev-aemni/Mailcraft/releases",
                fontSize = 10.sp,
                color = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open Link",
            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Version 1.1.1 • All data stored locally on device • © 2026 @dev-aemni",
        fontSize = 10.sp,
        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
      )
    }
  }
}
