package com.aiaggregator.app.models

data class Vendor(
    val id: String,
    val name: String,
    val nameEn: String,
    val category: String, // "domestic" or "international"
    val baseUrl: String
)

data class AiModel(
    val id: String,
    val name: String,
    val vendorId: String,
    val vendorName: String,
    val contextLength: Int,
    val supportsCode: Boolean = true,
    val supportsImage: Boolean = false,
    val isLatest: Boolean = false,
    val isBestPerformance: Boolean = false,
    val description: String? = null
)

data class ChatMessage(
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val modelId: String,
    val modelName: String,
    val vendorName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val responseTimeMs: Long = 0,
    val tokenCount: Int = 0
)

data class Conversation(
    val id: String,
    val title: String,
    val modelId: String,
    val modelName: String,
    val vendorName: String,
    val type: String, // "text", "code", "image", "cluster"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clusterModelIds: List<String>? = null
)