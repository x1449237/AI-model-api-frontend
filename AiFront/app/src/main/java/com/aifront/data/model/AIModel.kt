package com.aifront.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_models")
data class AIModel(
    @PrimaryKey val id: String,
    val vendorId: String,
    val name: String,
    val displayName: String,
    val description: String,
    val contextLength: Int,
    val supportsCode: Boolean,
    val supportsImage: Boolean,
    val isLatest: Boolean,
    val isBestPerformance: Boolean,
    val modelType: ModelType,
    val apiEndpoint: String,
    val maxTokens: Int = 4096
)

enum class ModelType {
    TEXT,
    IMAGE,
    MULTIMODAL
}