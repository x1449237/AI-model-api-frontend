package com.aiaggregator.app.data

import androidx.room.*

@Entity(tableName = "messages", foreignKeys = [ForeignKey(
    entity = ConversationEntity::class,
    parentColumns = ["id"],
    childColumns = ["conversation_id"],
    onDelete = ForeignKey.CASCADE
)])
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "model_id") val modelId: String,
    @ColumnInfo(name = "model_name") val modelName: String,
    @ColumnInfo(name = "vendor_name") val vendorName: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "response_time_ms") val responseTimeMs: Long = 0,
    @ColumnInfo(name = "token_count") val tokenCount: Int = 0
)