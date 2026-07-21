package com.aiaggregator.app.data

import androidx.room.*

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "model_id") val modelId: String,
    @ColumnInfo(name = "model_name") val modelName: String,
    @ColumnInfo(name = "vendor_name") val vendorName: String,
    @ColumnInfo(name = "type") val type: String, // "text", "code", "image", "cluster"
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)