package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.EmailBlock
import com.example.model.EmailMessage
import com.example.model.SenderAccount
import com.example.ui.components.highlightHtmlSyntax
import com.example.util.HtmlEmailGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MailCraft", appName)
  }

  @Test
  fun `generate html email with buttons and media`() {
    val message = EmailMessage(
      sender = SenderAccount(email = "sender@domain.com", displayName = "MailCraft Dev"),
      to = listOf("recipient@example.com"),
      subject = "Test Launch",
      blocks = listOf(
        EmailBlock.Header(id = "1", text = "Hello World"),
        EmailBlock.Button(id = "2", label = "Click Me", url = "https://example.com"),
        EmailBlock.GifImage(id = "3", imageUrl = "https://example.com/test.gif")
      )
    )

    val html = HtmlEmailGenerator.generateHtml(message)
    assertTrue(html.contains("Hello World"))
    assertTrue(html.contains("Click Me"))
    assertTrue(html.contains("https://example.com/test.gif"))

    val script = HtmlEmailGenerator.generateNodemailerScript(message)
    assertTrue(script.contains("nodemailer.createTransport"))
    assertTrue(script.contains("sender@domain.com"))
  }

  @Test
  fun `syntax highlighting produces annotated string`() {
    val rawHtml = """<table class="btn"><tr><td><!-- comment -->Hello</td></tr></table>"""
    val annotated = highlightHtmlSyntax(rawHtml)
    assertEquals(rawHtml, annotated.text)
    assertTrue(annotated.spanStyles.isNotEmpty())
  }

  @Test
  fun `formatHtml indents tags cleanly`() {
    val unformatted = "<div><p>Hello</p></div>"
    val formatted = HtmlEmailGenerator.formatHtml(unformatted)
    assertTrue(formatted.contains("\n"))
    assertTrue(formatted.contains("Hello"))
  }
}
