package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.SenderAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("mailcraft_prefs", Context.MODE_PRIVATE)

  private val _senderAccount = MutableStateFlow(loadSenderAccount())
  val senderAccount: StateFlow<SenderAccount> = _senderAccount.asStateFlow()

  fun loadSenderAccount(): SenderAccount {
    var savedEmail = prefs.getString("sender_email", "") ?: ""
    // Ensure user's private email is never returned as default if previously cached
    if (savedEmail.equals("aemniacc@gmail.com", ignoreCase = true)) {
      savedEmail = ""
      prefs.edit().putString("sender_email", "").apply()
    }

    var savedName = prefs.getString("sender_display_name", "") ?: ""
    if (savedName.equals("Aemni", ignoreCase = true)) {
      savedName = ""
      prefs.edit().putString("sender_display_name", "").apply()
    }

    return SenderAccount(
      email = savedEmail,
      displayName = savedName,
      appPassword = prefs.getString("sender_app_password", "") ?: "",
      smtpHost = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com",
      smtpPort = prefs.getInt("smtp_port", 465),
      useSsl = prefs.getBoolean("use_ssl", true),
      avatarPreset = prefs.getString("sender_avatar_preset", "avatar_purple_orb") ?: "avatar_purple_orb",
      avatarUrl = prefs.getString("sender_avatar_url", "") ?: ""
    )
  }

  fun saveSenderAccount(account: SenderAccount) {
    prefs.edit()
      .putString("sender_email", account.email.trim())
      .putString("sender_display_name", account.displayName.trim())
      .putString("sender_app_password", account.appPassword.replace(" ", "").trim())
      .putString("smtp_host", account.smtpHost.trim())
      .putInt("smtp_port", account.smtpPort)
      .putBoolean("use_ssl", account.useSsl)
      .putString("sender_avatar_preset", account.avatarPreset)
      .putString("sender_avatar_url", account.avatarUrl.trim())
      .apply()
    _senderAccount.value = account
  }

  fun getRecentRecipients(): List<String> {
    val raw = prefs.getString("recent_recipients", "") ?: ""
    return if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
  }

  fun addRecentRecipient(email: String) {
    if (email.isBlank() || !email.contains("@")) return
    val current = getRecentRecipients().toMutableList()
    current.remove(email)
    current.add(0, email)
    val trimmed = current.take(10).joinToString(",")
    prefs.edit().putString("recent_recipients", trimmed).apply()
  }

  fun isDarkMode(defaultSystemDark: Boolean = false): Boolean {
    if (!prefs.contains("app_dark_mode")) {
      return defaultSystemDark
    }
    return prefs.getBoolean("app_dark_mode", defaultSystemDark)
  }

  fun saveDarkMode(isDark: Boolean) {
    prefs.edit().putBoolean("app_dark_mode", isDark).apply()
  }
}
