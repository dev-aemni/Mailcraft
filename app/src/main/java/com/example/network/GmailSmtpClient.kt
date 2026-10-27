package com.example.network

import android.util.Base64
import com.example.model.EmailMessage
import com.example.model.SenderAccount
import com.example.model.SmtpLog
import com.example.model.SmtpSendResult
import com.example.util.HtmlEmailGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object GmailSmtpClient {

  private const val GMAIL_SMTP_HOST = "smtp.gmail.com"
  private const val GMAIL_SSL_PORT = 465
  private const val TIMEOUT_MS = 15000

  suspend fun testConnection(account: SenderAccount): SmtpSendResult = withContext(Dispatchers.IO) {
    val logs = mutableListOf<SmtpLog>()
    var socket: SSLSocket? = null
    try {
      logs.add(SmtpLog(message = "Initiating TLS/SSL connection to $GMAIL_SMTP_HOST:$GMAIL_SSL_PORT..."))
      
      val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
      socket = factory.createSocket() as SSLSocket
      socket.connect(InetSocketAddress(GMAIL_SMTP_HOST, GMAIL_SSL_PORT), TIMEOUT_MS)
      socket.soTimeout = TIMEOUT_MS
      socket.startHandshake()

      logs.add(SmtpLog(message = "SSL handshake successful (${socket.session.protocol}, ${socket.session.cipherSuite})"))

      val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
      val writer = BufferedWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8))

      val banner = readResponse(reader, logs)
      if (!banner.startsWith("220")) {
        return@withContext SmtpSendResult(false, "Server did not return ready banner: $banner", logs)
      }

      sendCommand(writer, "EHLO mailcraft.gmail.com", logs)
      readResponse(reader, logs)

      // Test Auth
      val cleanPassword = account.appPassword.replace(" ", "").trim()
      if (cleanPassword.isBlank()) {
        return@withContext SmtpSendResult(
          false,
          "App Password is empty. Please generate a 16-character App Password at myaccount.google.com/apppasswords",
          logs
        )
      }

      sendCommand(writer, "AUTH LOGIN", logs)
      val authResp1 = readResponse(reader, logs)
      if (!authResp1.startsWith("334")) {
        return@withContext SmtpSendResult(false, "AUTH LOGIN challenge failed: $authResp1", logs)
      }

      // Send Base64 Username
      val encodedUser = Base64.encodeToString(account.email.trim().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
      sendCommand(writer, encodedUser, logs, maskInLog = false)
      val authResp2 = readResponse(reader, logs)
      if (!authResp2.startsWith("334")) {
        return@withContext SmtpSendResult(false, "Username challenge failed: $authResp2", logs)
      }

      // Send Base64 App Password
      val encodedPass = Base64.encodeToString(cleanPassword.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
      sendCommand(writer, encodedPass, logs, maskInLog = true)
      val authResult = readResponse(reader, logs)

      if (authResult.startsWith("235")) {
        logs.add(SmtpLog(message = "Authentication successful! Gmail accepted credentials."))
        sendCommand(writer, "QUIT", logs)
        readResponse(reader, logs)
        return@withContext SmtpSendResult(
          true,
          "Connected and authenticated successfully with Gmail SMTP!",
          logs
        )
      } else {
        val errorExplanation = if (authResult.contains("535")) {
          "Authentication failed (535). Google rejected the password. Ensure you are using a 16-character Google App Password (not your normal Google password) generated at myaccount.google.com/apppasswords with 2-Step Verification enabled."
        } else {
          "Authentication rejected: $authResult"
        }
        return@withContext SmtpSendResult(false, errorExplanation, logs)
      }
    } catch (e: Exception) {
      val errMsg = "Connection failed: ${e.localizedMessage ?: e.javaClass.simpleName}"
      logs.add(SmtpLog(message = errMsg, isError = true))
      return@withContext SmtpSendResult(false, errMsg, logs)
    } finally {
      try {
        socket?.close()
      } catch (_: Exception) {}
    }
  }

  suspend fun sendEmail(
    message: EmailMessage,
    contentResolver: android.content.ContentResolver? = null
  ): SmtpSendResult = withContext(Dispatchers.IO) {
    val logs = mutableListOf<SmtpLog>()
    var socket: SSLSocket? = null

    try {
      val sender = message.sender
      val cleanPassword = sender.appPassword.replace(" ", "").trim()

      if (sender.email.isBlank()) {
        return@withContext SmtpSendResult(false, "Sender email cannot be blank", logs)
      }
      if (cleanPassword.isBlank()) {
        return@withContext SmtpSendResult(
          false,
          "Google App Password is required. Visit myaccount.google.com/apppasswords to generate one.",
          logs
        )
      }
      if (message.to.isEmpty() || message.to.all { it.isBlank() }) {
        return@withContext SmtpSendResult(false, "At least one recipient email address is required.", logs)
      }

      logs.add(SmtpLog(message = "Connecting to $GMAIL_SMTP_HOST:$GMAIL_SSL_PORT via TLS..."))

      val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
      socket = factory.createSocket() as SSLSocket
      socket.connect(InetSocketAddress(GMAIL_SMTP_HOST, GMAIL_SSL_PORT), TIMEOUT_MS)
      socket.soTimeout = TIMEOUT_MS
      socket.startHandshake()

      logs.add(SmtpLog(message = "Secure connection established with Gmail SMTP."))

      val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
      val writer = BufferedWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8))

      val banner = readResponse(reader, logs)
      if (!banner.startsWith("220")) {
        return@withContext SmtpSendResult(false, "Unexpected server banner: $banner", logs)
      }

      sendCommand(writer, "EHLO mailcraft.gmail.com", logs)
      readResponse(reader, logs)

      // AUTH LOGIN
      sendCommand(writer, "AUTH LOGIN", logs)
      val auth1 = readResponse(reader, logs)
      if (!auth1.startsWith("334")) {
        return@withContext SmtpSendResult(false, "AUTH LOGIN failed: $auth1", logs)
      }

      val encodedUser = Base64.encodeToString(sender.email.trim().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
      sendCommand(writer, encodedUser, logs, maskInLog = false)
      val auth2 = readResponse(reader, logs)
      if (!auth2.startsWith("334")) {
        return@withContext SmtpSendResult(false, "Username challenge failed: $auth2", logs)
      }

      val encodedPass = Base64.encodeToString(cleanPassword.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
      sendCommand(writer, encodedPass, logs, maskInLog = true)
      val authRes = readResponse(reader, logs)
      if (!authRes.startsWith("235")) {
        val detail = if (authRes.contains("535")) {
          "Gmail rejected credentials (535). Please ensure you generated a 16-character App Password at myaccount.google.com/apppasswords with 2-Step Verification enabled. Or use 'Send via Device Gmail App' for instant passwordless delivery!"
        } else {
          "Auth error: $authRes"
        }
        return@withContext SmtpSendResult(false, detail, logs)
      }

      logs.add(SmtpLog(message = "Authenticated as ${sender.email}"))

      // MAIL FROM
      sendCommand(writer, "MAIL FROM:<${sender.email.trim()}>", logs)
      val mailFromRes = readResponse(reader, logs)
      if (!mailFromRes.startsWith("250")) {
        return@withContext SmtpSendResult(false, "MAIL FROM failed: $mailFromRes", logs)
      }

      // RCPT TO for each recipient
      val allRecipients = (message.to + message.cc + message.bcc).filter { it.isNotBlank() }.distinct()
      for (rcpt in allRecipients) {
        sendCommand(writer, "RCPT TO:<${rcpt.trim()}>", logs)
        val rcptRes = readResponse(reader, logs)
        if (!rcptRes.startsWith("250")) {
          return@withContext SmtpSendResult(false, "Recipient rejected ($rcpt): $rcptRes", logs)
        }
      }

      // DATA
      sendCommand(writer, "DATA", logs)
      val dataRes = readResponse(reader, logs)
      if (!dataRes.startsWith("354")) {
        return@withContext SmtpSendResult(false, "DATA command failed: $dataRes", logs)
      }

      // Prepare MIME Body
      val attachments = message.attachments
      val hasAttachments = attachments.isNotEmpty()
      logs.add(
        SmtpLog(
          message = if (hasAttachments) {
            "Generating RFC 2046 multipart/mixed email with HTML body and ${attachments.size} file attachment(s)..."
          } else {
            "Generating multipart MIME email with Primary Inbox priority headers..."
          }
        )
      )

      val rfcDate = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(Date())
      val messageId = "<${System.currentTimeMillis()}.${UUID.randomUUID().toString().take(12)}@mail.gmail.com>"

      val encodedSubject = "=?UTF-8?B?" + Base64.encodeToString(
        message.subject.toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP
      ) + "?="

      val htmlContent = HtmlEmailGenerator.generateHtml(message)
      val plainTextContent = HtmlEmailGenerator.generatePlainText(message)

      val sb = StringBuilder()
      sb.append("Date: $rfcDate\r\n")
      sb.append("From: \"${sender.displayName.replace("\"", "")}\" <${sender.email.trim()}>\r\n")
      sb.append("To: ${message.to.joinToString(", ") { it.trim() }}\r\n")
      if (message.cc.isNotEmpty()) {
        sb.append("Cc: ${message.cc.joinToString(", ") { it.trim() }}\r\n")
      }
      sb.append("Reply-To: \"${sender.displayName.replace("\"", "")}\" <${sender.email.trim()}>\r\n")
      sb.append("Subject: $encodedSubject\r\n")
      sb.append("Message-ID: $messageId\r\n")
      sb.append("MIME-Version: 1.0\r\n")
      sb.append("Precedence: personal\r\n")
      sb.append("X-Priority: 3\r\n")
      sb.append("Priority: normal\r\n")
      sb.append("Importance: normal\r\n")
      sb.append("X-Mailer: MailCraft Mobile Studio v2.5 (Direct Inbox Priority)\r\n")
      sb.append("Auto-Submitted: no\r\n")

      if (!hasAttachments) {
        val boundary = "==_MailCraft_" + UUID.randomUUID().toString().replace("-", "")
        sb.append("Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n")
        sb.append("\r\n")

        // Plain text part
        sb.append("--$boundary\r\n")
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n")
        sb.append("Content-Transfer-Encoding: base64\r\n")
        sb.append("\r\n")
        sb.append(Base64.encodeToString(plainTextContent.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        sb.append("\r\n")

        // HTML part
        sb.append("--$boundary\r\n")
        sb.append("Content-Type: text/html; charset=UTF-8\r\n")
        sb.append("Content-Transfer-Encoding: base64\r\n")
        sb.append("\r\n")
        sb.append(Base64.encodeToString(htmlContent.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        sb.append("\r\n")

        sb.append("--$boundary--\r\n")
      } else {
        // RFC 2046 multipart/mixed: alternative subpart + attachment parts
        val mixedBoundary = "==_MailCraftMixed_" + UUID.randomUUID().toString().replace("-", "")
        val altBoundary = "==_MailCraftAlt_" + UUID.randomUUID().toString().replace("-", "")

        sb.append("Content-Type: multipart/mixed; boundary=\"$mixedBoundary\"\r\n")
        sb.append("\r\n")

        // Subpart 1: multipart/alternative (text + html)
        sb.append("--$mixedBoundary\r\n")
        sb.append("Content-Type: multipart/alternative; boundary=\"$altBoundary\"\r\n")
        sb.append("\r\n")

        sb.append("--$altBoundary\r\n")
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n")
        sb.append("Content-Transfer-Encoding: base64\r\n")
        sb.append("\r\n")
        sb.append(Base64.encodeToString(plainTextContent.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        sb.append("\r\n")

        sb.append("--$altBoundary\r\n")
        sb.append("Content-Type: text/html; charset=UTF-8\r\n")
        sb.append("Content-Transfer-Encoding: base64\r\n")
        sb.append("\r\n")
        sb.append(Base64.encodeToString(htmlContent.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        sb.append("\r\n")

        sb.append("--$altBoundary--\r\n")

        // Subparts 2..N: attachments
        for (att in attachments) {
          try {
            logs.add(SmtpLog(message = "Reading attachment: ${att.fileName} (${att.formattedSize})..."))
            val uri = android.net.Uri.parse(att.uriString)
            val bytes = contentResolver?.openInputStream(uri)?.use { inputStream ->
              inputStream.readBytes()
            } ?: ByteArray(0)

            val safeName = att.fileName.replace("\"", "").replace("\r", "").replace("\n", "").ifBlank { "attachment.bin" }
            val mime = att.mimeType.ifBlank { "application/octet-stream" }
            val encodedAttData = Base64.encodeToString(bytes, Base64.DEFAULT)

            sb.append("\r\n--$mixedBoundary\r\n")
            sb.append("Content-Type: $mime; name=\"$safeName\"\r\n")
            sb.append("Content-Disposition: attachment; filename=\"$safeName\"\r\n")
            sb.append("Content-Transfer-Encoding: base64\r\n")
            sb.append("\r\n")
            sb.append(encodedAttData)
            sb.append("\r\n")
            logs.add(SmtpLog(message = "Streamed attachment: $safeName (${bytes.size} bytes)"))
          } catch (attEx: Exception) {
            logs.add(SmtpLog(message = "Warning: Failed to attach ${att.fileName}: ${attEx.localizedMessage}", isError = true))
          }
        }

        sb.append("\r\n--$mixedBoundary--\r\n")
      }

      sb.append("\r\n.\r\n")

      // Write raw message
      writer.write(sb.toString())
      writer.flush()
      logs.add(SmtpLog(message = "Email data streamed to Gmail SMTP. Awaiting server acknowledgement..."))

      val sendResult = readResponse(reader, logs)
      if (sendResult.startsWith("250")) {
        logs.add(SmtpLog(message = "Server confirmed delivery: $sendResult"))
        sendCommand(writer, "QUIT", logs)
        readResponse(reader, logs)
        return@withContext SmtpSendResult(
          true,
          "Email dispatched successfully via Gmail! ($sendResult)",
          logs,
          messageId
        )
      } else {
        return@withContext SmtpSendResult(false, "Delivery error: $sendResult", logs)
      }

    } catch (e: Exception) {
      val errorMsg = "Transmission error: ${e.localizedMessage ?: e.javaClass.simpleName}"
      logs.add(SmtpLog(message = errorMsg, isError = true))
      return@withContext SmtpSendResult(false, errorMsg, logs)
    } finally {
      try {
        socket?.close()
      } catch (_: Exception) {}
    }
  }

  private fun sendCommand(
    writer: BufferedWriter,
    command: String,
    logs: MutableList<SmtpLog>,
    maskInLog: Boolean = false
  ) {
    writer.write(command)
    writer.write("\r\n")
    writer.flush()
    val display = if (maskInLog) "****** [Base64 App Password]" else command
    logs.add(SmtpLog(message = "> $display"))
  }

  private fun readResponse(reader: BufferedReader, logs: MutableList<SmtpLog>): String {
    val fullResponse = StringBuilder()
    var line: String?

    while (true) {
      line = reader.readLine() ?: break
      fullResponse.append(line).append("\n")
      logs.add(SmtpLog(message = "< $line"))
      // If the 4th character is space or the line is just 3 digits, this is the last line of the response
      if (line.length >= 4 && line[3] == ' ') break
      if (line.length == 3) break
    }
    return fullResponse.toString().trim()
  }
}
