package com.aifront.data.api

import com.aifront.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class ApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val gson = Gson()

    fun chatStream(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<MessageContent>,
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        maxTokens: Int = 4096,
        apiType: ApiType = ApiType.OPENAI_COMPATIBLE
    ): Flow<StreamResponse> = flow {
        val request = buildChatRequest(baseUrl, apiKey, model, messages, temperature, topP, maxTokens, apiType)

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            emit(StreamResponse("Error: ${response.code} ${response.message}", true))
            return@flow
        }

        val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: return@flow))
        var fullContent = StringBuilder()

        reader.useLines { lines ->
            for (line in lines) {
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ")
                    if (data == "[DONE]") {
                        emit(StreamResponse(fullContent.toString(), true))
                        return@useLines
                    }
                    try {
                        val json = JsonParser.parseString(data).asJsonObject
                        val choices = json.getAsJsonArray("choices")
                        if (choices != null && choices.size() > 0) {
                            val delta = choices[0].asJsonObject.getAsJsonObject("delta")
                            val content = delta?.get("content")?.asString ?: ""
                            if (content.isNotEmpty()) {
                                fullContent.append(content)
                                emit(StreamResponse(content, false))
                            }
                        }
                    } catch (e: Exception) { }
                }
            }
        }
        emit(StreamResponse(fullContent.toString(), true))
    }.flowOn(Dispatchers.IO)

    suspend fun chatSync(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<MessageContent>,
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        maxTokens: Int = 4096,
        apiType: ApiType = ApiType.OPENAI_COMPATIBLE
    ): String = withContext(Dispatchers.IO) {
        val jsonBody = buildChatRequestBody(model, messages, temperature, topP, maxTokens, false)
        val url = buildChatUrl(baseUrl, apiType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return@withContext "Error: ${response.code} ${response.message}"
        }

        val body = response.body?.string() ?: return@withContext "Empty response"
        try {
            val json = JsonParser.parseString(body).asJsonObject
            val choices = json.getAsJsonArray("choices")
            choices?.get(0)?.asJsonObject?.getAsJsonObject("message")?.get("content")?.asString ?: "No content"
        } catch (e: Exception) {
            body
        }
    }

    suspend fun generateImage(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        n: Int = 1,
        size: String = "1024x1024"
    ): ImageGenerationResponse = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(
            mapOf(
                "model" to model,
                "prompt" to prompt,
                "n" to n,
                "size" to size
            )
        )

        val url = "${baseUrl.trimEnd('/')}/images/generations"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return@withContext ImageGenerationResponse(emptyList())
        }

        val body = response.body?.string() ?: return@withContext ImageGenerationResponse(emptyList())
        try {
            val json = JsonParser.parseString(body).asJsonObject
            val data = json.getAsJsonArray("data")
            val images = data?.map {
                ImageData(
                    url = it.asJsonObject.get("url")?.asString ?: "",
                    revisedPrompt = it.asJsonObject.get("revised_prompt")?.asString
                )
            } ?: emptyList()
            ImageGenerationResponse(images)
        } catch (e: Exception) {
            ImageGenerationResponse(emptyList())
        }
    }

    private fun buildChatRequest(
        baseUrl: String, apiKey: String, model: String,
        messages: List<MessageContent>, temperature: Float, topP: Float,
        maxTokens: Int, apiType: ApiType
    ): Request {
        val jsonBody = buildChatRequestBody(model, messages, temperature, topP, maxTokens, true)
        val url = buildChatUrl(baseUrl, apiType)
        return Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun buildChatRequestBody(
        model: String, messages: List<MessageContent>,
        temperature: Float, topP: Float, maxTokens: Int, stream: Boolean
    ): String {
        return gson.toJson(
            mapOf(
                "model" to model,
                "messages" to messages.map { mapOf("role" to it.role, "content" to it.content) },
                "temperature" to temperature,
                "top_p" to topP,
                "max_tokens" to maxTokens,
                "stream" to stream
            )
        )
    }

    private fun buildChatUrl(baseUrl: String, apiType: ApiType): String {
        return when (apiType) {
            ApiType.ANTHROPIC -> "${baseUrl.trimEnd('/')}/messages"
            ApiType.GOOGLE -> "${baseUrl.trimEnd('/')}/models/generateContent"
            else -> "${baseUrl.trimEnd('/')}/chat/completions"
        }
    }
}