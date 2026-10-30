package com.example.network

import com.example.model.SenderAccount
import com.example.model.SmtpLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Direct IMAP client for Gmail (imap.gmail.com:993).
 *
 * Why this is crucial:
 * When sending via standard SMTP to yourself (or self-addressed test emails),
 * Google's SMTP server automatically archives the email to 'Sent' and removes the 'INBOX' label.
 *
 * By calling [appendToInbox], MailCraft uploads a clean copy of the email directly into the
 * Gmail "INBOX" folder with the (\Flagged) and unread flags.
 *
 * This ensures:
 * 1. The email appears immediately in the user's Primary Inbox and "All inboxes".
 * 2. In the "Sent" list, the thread is tagged with the official [Inbox] badge (matching Discord/system messages).
 * 3. The email is marked as Important.
 */
object GmailImapClient {

  private const val GMAIL_IMAP_HOST = "imap.gmail.com"
  private const val GMAIL_IMAP_PORT = 993
  private const val TIMEOUT_MS = 15000

  suspend fun appendToInbox(
    account: SenderAccount,
    mimePayload: String,
    logs: MutableList<SmtpLog>? = null
  ): Boolean = withContext(Dispatchers.IO) {
    var socket: SSLSocket? = null
    try {
      val user = account.email.trim()
      val cleanPassword = account.appPassword.replace(" ", "").trim()

      if (user.isBlank() || cleanPassword.isBlank()) {
        return@withContext false
      }

      logs?.add(SmtpLog(message = "Direct Inbox Injection: Connecting to $GMAIL_IMAP_HOST:$GMAIL_IMAP_PORT via TLS..."))

      val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
      socket = factory.createSocket() as SSLSocket
      socket.connect(InetSocketAddress(GMAIL_IMAP_HOST, GMAIL_IMAP_PORT), TIMEOUT_MS)
      socket.soTimeout = TIMEOUT_MS
      socket.startHandshake()

      val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
      val outStream: OutputStream = socket.outputStream

      // Read greeting line: * OK Gimap ready for requests...
      val greeting = readLine(reader)
      logs?.add(SmtpLog(message = "< IMAP: $greeting"))
      if (!greeting.startsWith("* OK")) {
        logs?.add(SmtpLog(message = "IMAP server not ready: $greeting", isError = true))
        return@withContext false
      }

      // 1. LOGIN
      val loginCmd = "A01 LOGIN \"$user\" \"$cleanPassword\"\r\n"
      outStream.write(loginCmd.toByteArray(Charsets.UTF_8))
      outStream.flush()
      logs?.add(SmtpLog(message = "> IMAP: A01 LOGIN \"$user\" ******"))

      val loginResp = readUntilTag(reader, "A01", logs)
      if (!loginResp.contains("OK", ignoreCase = true)) {
        logs?.add(SmtpLog(message = "IMAP Login failed: $loginResp", isError = true))
        return@withContext false
      }
      logs?.add(SmtpLog(message = "IMAP Authenticated successfully with Gmail."))

      // 2. APPEND to "INBOX"
      // Flags: (\Flagged) marks as Important/Starred, unread so it triggers Inbox visibility
      val payloadBytes = mimePayload.toByteArray(Charsets.UTF_8)
      val appendCmd = "A02 APPEND \"INBOX\" (\\Flagged) {${payloadBytes.size}}\r\n"
      outStream.write(appendCmd.toByteArray(Charsets.UTF_8))
      outStream.flush()
      logs?.add(SmtpLog(message = "> IMAP: A02 APPEND \"INBOX\" (\\Flagged) {${payloadBytes.size}}"))

      // Wait for continuation: + go ahead or + Ready
      val contResp = readLine(reader)
      logs?.add(SmtpLog(message = "< IMAP: $contResp"))
      if (!contResp.startsWith("+")) {
        logs?.add(SmtpLog(message = "IMAP APPEND rejected literal: $contResp", isError = true))
        return@withContext false
      }

      // Stream payload bytes
      outStream.write(payloadBytes)
      outStream.write("\r\n".toByteArray(Charsets.UTF_8))
      outStream.flush()
      logs?.add(SmtpLog(message = "> IMAP: [Payload streamed directly into INBOX folder]"))

      val appendResp = readUntilTag(reader, "A02", logs)
      if (appendResp.contains("OK", ignoreCase = true)) {
        logs?.add(SmtpLog(message = "✅ Successfully tagged with [Inbox] & Important marker in Gmail!"))
        // Logout gracefully
        try {
          outStream.write("A03 LOGOUT\r\n".toByteArray(Charsets.UTF_8))
          outStream.flush()
          readUntilTag(reader, "A03", null)
        } catch (_: Exception) {}
        return@withContext true
      } else {
        logs?.add(SmtpLog(message = "IMAP APPEND failed: $appendResp", isError = true))
        return@withContext false
      }

    } catch (e: Exception) {
      logs?.add(SmtpLog(message = "IMAP Inbox sync notice: ${e.localizedMessage ?: e.javaClass.simpleName}"))
      return@withContext false
    } finally {
      try {
        socket?.close()
      } catch (_: Exception) {}
    }
  }

  private fun readLine(reader: BufferedReader): String {
    return reader.readLine() ?: ""
  }

  private fun readUntilTag(reader: BufferedReader, tag: String, logs: MutableList<SmtpLog>?): String {
    val full = StringBuilder()
    while (true) {
      val line = reader.readLine() ?: break
      full.append(line).append("\n")
      if (logs != null && line.isNotBlank()) {
        logs.add(SmtpLog(message = "< IMAP: $line"))
      }
      if (line.startsWith("$tag ") || line.startsWith("$tag\t")) {
        break
      }
    }
    return full.toString().trim()
  }
}
