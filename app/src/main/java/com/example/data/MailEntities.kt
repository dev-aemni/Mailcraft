package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sent_emails")
data class SentEmailEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val toEmail: String,
  val subject: String,
  val previewSnippet: String,
  val sentTimestamp: Long = System.currentTimeMillis(),
  val status: String, // "DELIVERED", "FAILED"
  val fullHtml: String,
  val errorMessage: String? = null
)

@Entity(tableName = "saved_drafts")
data class SavedDraftEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val recipient: String,
  val cc: String = "",
  val bcc: String = "",
  val subject: String,
  val blocksJson: String,
  val customHtml: String = "",
  val isCustomHtml: Boolean = false,
  val previewSnippet: String = "",
  val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_templates")
data class CustomTemplateEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val htmlContent: String,
  val subject: String = "Special Update",
  val previewSnippet: String = "",
  val iconEmoji: String = "📄",
  val createdTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface MailDao {
  @Query("SELECT * FROM sent_emails ORDER BY sentTimestamp DESC")
  fun getAllSentEmails(): Flow<List<SentEmailEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSentEmail(email: SentEmailEntity): Long

  @Query("DELETE FROM sent_emails WHERE id = :id")
  suspend fun deleteSentEmail(id: Long)

  @Query("DELETE FROM sent_emails")
  suspend fun clearSentHistory()

  @Query("SELECT * FROM saved_drafts ORDER BY updatedTimestamp DESC")
  fun getAllDrafts(): Flow<List<SavedDraftEntity>>

  @Query("SELECT * FROM saved_drafts WHERE id = :id")
  suspend fun getDraftById(id: Long): SavedDraftEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDraft(draft: SavedDraftEntity): Long

  @Query("DELETE FROM saved_drafts WHERE id = :id")
  suspend fun deleteDraft(id: Long)

  @Query("DELETE FROM saved_drafts")
  suspend fun clearAllDrafts()

  @Query("SELECT * FROM custom_templates ORDER BY createdTimestamp DESC")
  fun getAllCustomTemplates(): Flow<List<CustomTemplateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomTemplate(template: CustomTemplateEntity): Long

  @Query("DELETE FROM custom_templates WHERE id = :id")
  suspend fun deleteCustomTemplate(id: Long)
}

@Database(entities = [SentEmailEntity::class, SavedDraftEntity::class, CustomTemplateEntity::class], version = 3, exportSchema = false)
abstract class MailCraftDatabase : RoomDatabase() {
  abstract fun mailDao(): MailDao
}
