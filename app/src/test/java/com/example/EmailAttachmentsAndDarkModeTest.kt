package com.example

import com.example.model.ClientDarkMode
import com.example.model.EmailAttachment
import com.example.model.EmailMessage
import com.example.model.SenderAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailAttachmentsAndDarkModeTest {

  @Test
  fun testEmailAttachmentFormattedSize() {
    val smallAtt = EmailAttachment(
      uriString = "content://dummy/1",
      fileName = "notes.txt",
      mimeType = "text/plain",
      sizeBytes = 512
    )
    assertEquals("512 B", smallAtt.formattedSize)

    val mediumAtt = EmailAttachment(
      uriString = "content://dummy/2",
      fileName = "document.pdf",
      mimeType = "application/pdf",
      sizeBytes = 153600 // 150 KB
    )
    assertEquals("150.0 KB", mediumAtt.formattedSize)

    val largeAtt = EmailAttachment(
      uriString = "content://dummy/3",
      fileName = "dataset.zip",
      mimeType = "application/zip",
      sizeBytes = 5242880 // 5.0 MB
    )
    assertEquals("5.00 MB", largeAtt.formattedSize)
  }

  @Test
  fun testEmailAttachmentMimeDetection() {
    val pdfAtt = EmailAttachment(
      uriString = "content://dummy/pdf",
      fileName = "Quarterly_Report.pdf",
      mimeType = "application/pdf",
      sizeBytes = 1024
    )
    assertTrue(pdfAtt.isPdf)
    assertFalse(pdfAtt.isImage)

    val imageAtt = EmailAttachment(
      uriString = "content://dummy/img",
      fileName = "hero_banner.png",
      mimeType = "image/png",
      sizeBytes = 2048
    )
    assertTrue(imageAtt.isImage)
    assertFalse(imageAtt.isPdf)
  }

  @Test
  fun testEmailMessageWithAttachments() {
    val sender = SenderAccount(email = "test@gmail.com", displayName = "Tester")
    val att1 = EmailAttachment(uriString = "content://1", fileName = "test.pdf", mimeType = "application/pdf", sizeBytes = 1000)
    val att2 = EmailAttachment(uriString = "content://2", fileName = "test.png", mimeType = "image/png", sizeBytes = 2000)

    val msg = EmailMessage(
      sender = sender,
      to = listOf("recipient@example.com"),
      subject = "Test with attachments",
      blocks = emptyList(),
      attachments = listOf(att1, att2)
    )

    assertEquals(2, msg.attachments.size)
    assertEquals("test.pdf", msg.attachments[0].fileName)
    assertEquals("test.png", msg.attachments[1].fileName)
    assertEquals(3000L, msg.attachments.sumOf { it.sizeBytes })
  }

  @Test
  fun testClientDarkModeEnum() {
    assertEquals(4, ClientDarkMode.values().size)
    assertTrue(ClientDarkMode.GMAIL.label.contains("Gmail"))
    assertTrue(ClientDarkMode.OUTLOOK.label.contains("Outlook"))
    assertTrue(ClientDarkMode.APPLE_MAIL.label.contains("Apple"))
  }
}
