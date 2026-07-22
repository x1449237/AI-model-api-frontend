package com.aifront.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelId: String? = null,
    val modelName: String? = null,
    val vendorName: String? = null,
    val conversationType: ConversationType = ConversationType.TEXT,
    val isClusterMode: Boolean = false,
    val clusterModelIds: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

enum class ConversationType {
    TEXT,
    CODE,
    IMAGE,
    CLUSTER
}