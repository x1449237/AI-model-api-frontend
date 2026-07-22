package com.aifront.data.model

data class ApiKeyConfig(
    val vendorId: String,
    val apiKey: String
)

data class StreamResponse(
    val content: String,
    val isFinished: Boolean,
    val tokensUsed: Int = 0
)

data class ChatRequest(
    val model: String,
    val messages: List<MessageContent>,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val maxTokens: Int = 4096,
    val stream: Boolean = true
)

data class MessageContent(
    val role: String,
    val content: String
)

data class ImageGenerationRequest(
    val prompt: String,
    val model: String,
    val n: Int = 1,
    val size: String = "1024x1024",
    val quality: String = "standard"
)

data class ImageGenerationResponse(
    val images: List<ImageData>
)

data class ImageData(
    val url: String,
    val revisedPrompt: String? = null
)