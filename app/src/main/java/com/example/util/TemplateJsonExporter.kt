package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.CustomTemplateEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Utility for exporting and importing custom HTML email templates as local JSON files
 * for backup, offline storage, or inter-device sharing.
 */
object TemplateJsonExporter {

  private const val APP_IDENTIFIER = "MailCraft"
  private const val CURRENT_VERSION = "1.1.1"

  /**
   * Serializes a list of custom templates into a structured, human-readable JSON backup string.
   */
  fun exportAllTemplates(templates: List<CustomTemplateEntity>): String {
    val root = JSONObject().apply {
      put("app", APP_IDENTIFIER)
      put("version", CURRENT_VERSION)
      put("type", "templates_backup")
      put("exportTimestamp", System.currentTimeMillis())
      put("count", templates.size)

      val array = JSONArray()
      templates.forEach { template ->
        array.put(templateToJsonObject(template))
      }
      put("templates", array)
    }
    return root.toString(2)
  }

  /**
   * Serializes a single custom template into a JSON string.
   */
  fun exportSingleTemplate(template: CustomTemplateEntity): String {
    val root = JSONObject().apply {
      put("app", APP_IDENTIFIER)
      put("version", CURRENT_VERSION)
      put("type", "single_template")
      put("exportTimestamp", System.currentTimeMillis())
      put("template", templateToJsonObject(template))
    }
    return root.toString(2)
  }

  private fun templateToJsonObject(template: CustomTemplateEntity): JSONObject {
    return JSONObject().apply {
      put("id", template.id)
      put("title", template.title)
      put("subject", template.subject)
      put("htmlContent", template.htmlContent)
      put("previewSnippet", template.previewSnippet)
      put("iconEmoji", template.iconEmoji)
      put("createdTimestamp", template.createdTimestamp)
    }
  }

  /**
   * Parses JSON string into a list of CustomTemplateEntity objects.
   * Flexibly handles full backup objects, single template objects, and bare JSON arrays.
   */
  fun parseTemplatesFromJson(jsonString: String): List<CustomTemplateEntity> {
    val trimmed = jsonString.trim()
    val parsedList = mutableListOf<CustomTemplateEntity>()

    if (trimmed.startsWith("[")) {
      val array = JSONArray(trimmed)
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i) ?: continue
        parseTemplateFromObject(obj)?.let { parsedList.add(it) }
      }
    } else {
      val root = JSONObject(trimmed)
      if (root.has("templates")) {
        val array = root.optJSONArray("templates")
        if (array != null) {
          for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            parseTemplateFromObject(obj)?.let { parsedList.add(it) }
          }
        }
      } else if (root.has("template")) {
        val tObj = root.optJSONObject("template")
        if (tObj != null) {
          parseTemplateFromObject(tObj)?.let { parsedList.add(it) }
        }
      } else if (root.has("htmlContent")) {
        // Direct template object
        parseTemplateFromObject(root)?.let { parsedList.add(it) }
      }
    }

    return parsedList
  }

  private fun parseTemplateFromObject(obj: JSONObject): CustomTemplateEntity? {
    val htmlContent = obj.optString("htmlContent", "").trim()
    if (htmlContent.isBlank()) return null

    val rawTitle = obj.optString("title", "").trim()
    val title = if (rawTitle.isNotBlank()) rawTitle else "Imported Template"
    val subject = obj.optString("subject", "Special Update").ifBlank { "Special Update" }
    val iconEmoji = obj.optString("iconEmoji", "📄").ifBlank { "📄" }
    val rawSnippet = obj.optString("previewSnippet", "")
    val snippet = if (rawSnippet.isNotBlank()) {
      rawSnippet
    } else {
      htmlContent.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim().take(120)
    }
    val timestamp = obj.optLong("createdTimestamp", System.currentTimeMillis())

    return CustomTemplateEntity(
      id = 0L, // Fresh insertion ID
      title = title,
      htmlContent = htmlContent,
      subject = subject,
      previewSnippet = snippet,
      iconEmoji = iconEmoji,
      createdTimestamp = timestamp
    )
  }

  /**
   * Saves JSON text to a local destination URI (Storage Access Framework).
   */
  fun writeJsonToUri(context: Context, uri: Uri, jsonContent: String): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.write(jsonContent.toByteArray(Charsets.UTF_8))
        stream.flush()
      }
      true
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Reads JSON text from a selected URI (Storage Access Framework).
   */
  fun readJsonFromUri(context: Context, uri: Uri): String? {
    return try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        stream.bufferedReader(Charsets.UTF_8).readText()
      }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Creates a shareable temporary JSON file and triggers the Android share sheet.
   */
  fun shareTemplateJson(context: Context, jsonContent: String, fileName: String, chooserTitle: String) {
    try {
      val templatesDir = File(context.cacheDir, "templates")
      if (!templatesDir.exists()) {
        templatesDir.mkdirs()
      }
      val safeFileName = if (fileName.endsWith(".json")) fileName else "$fileName.json"
      val file = File(templatesDir, safeFileName)
      file.writeText(jsonContent, Charsets.UTF_8)

      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, safeFileName)
        putExtra(Intent.EXTRA_TEXT, "Here is a custom email template exported from MailCraft Studio (v$CURRENT_VERSION).")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, chooserTitle)
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (_: Exception) {
      // Fallback: Share as direct text if file provider fails
      try {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_SUBJECT, fileName)
          putExtra(Intent.EXTRA_TEXT, jsonContent)
        }
        val chooser = Intent.createChooser(fallbackIntent, chooserTitle)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
      } catch (_: Exception) {}
    }
  }
}
