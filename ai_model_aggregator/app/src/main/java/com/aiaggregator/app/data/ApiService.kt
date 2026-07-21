package com.aiaggregator.app.data

import android.content.Context
import android.content.SharedPreferences
import com.aiaggregator.app.models.AiModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("api_keys", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun getApiKey(vendorId: String): String? = prefs.getString("api_key_$vendorId", null)

    fun setApiKey(vendorId: String, key: String) {
        prefs.edit().putString("api_key_$vendorId", key).apply()
    }

    fun sendMessage(
        model: AiModel,
        prompt: String,
        temperature: Double = 0.7,
        topP: Double = 0.9,
        maxTokens: Int = 2048,
        onChunk: ((String) -> Unit)? = null,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val apiKey = getApiKey(model.vendorId) ?: ""
        val vendor = VendorConfig.getVendorById(model.vendorId)

        try {
            val messages = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", prompt)
                })
            }

            val body = JsonObject().apply {
                addProperty("model", model.id)
                add("messages", messages)
                addProperty("temperature", temperature)
                addProperty("top_p", topP)
                addProperty("max_tokens", maxTokens)
                addProperty("stream", onChunk != null)
            }

            val headers = Headers.Builder().apply {
                add("Content-Type", "application/json")
                when (model.vendorId) {
                    "anthropic" -> {
                        add("x-api-key", apiKey)
                        add("anthropic-version", "2023-06-01")
                    }
                    else -> add("Authorization", "Bearer $apiKey")
                }
            }.build()

            val request = Request.Builder()
                .url(vendor?.baseUrl ?: "https://api.openai.com/v1/chat/completions")
                .headers(headers)
                .post(body.toString().toRequestBody(JSON))
                .build()

            if (onChunk != null) {
                // Streaming
                val sb = StringBuilder()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError("网络请求失败: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.body?.charStream()?.use { reader ->
                            reader.forEachLine { line ->
                                if (line.startsWith("data: ") && line.length > 6) {
                                    val data = line.substring(6)
                                    if (data == "[DONE]") return@forEachLine
                                    try {
                                        val json = gson.fromJson(data, JsonObject::class.java)
                                        val content = json
                                            ?.getAsJsonArray("choices")
                                            ?.get(0)?.asJsonObject
                                            ?.getAsJsonObject("delta")
                                            ?.get("content")?.asString ?: ""
                                        if (content.isNotEmpty()) {
                                            sb.append(content)
                                            onChunk(content)
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                        onComplete(sb.toString())
                    }
                })
            } else {
                // Non-streaming
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError("网络请求失败: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            try {
                                val json = gson.fromJson(body, JsonObject::class.java)
                                val content = when (model.vendorId) {
                                    "anthropic" -> json?.getAsJsonArray("content")?.get(0)?.asJsonObject?.get("text")?.asString ?: ""
                                    "google" -> json?.getAsJsonArray("candidates")?.get(0)?.asJsonObject
                                        ?.getAsJsonObject("content")?.getAsJsonArray("parts")?.get(0)?.asJsonObject
                                        ?.get("text")?.asString ?: ""
                                    else -> json?.getAsJsonArray("choices")?.get(0)?.asJsonObject
                                        ?.getAsJsonObject("message")?.get("content")?.asString ?: ""
                                }
                                onComplete(content)
                            } catch (e: Exception) {
                                onComplete(body)
                            }
                        } else {
                            onError("HTTP ${response.code}: $body")
                        }
                    }
                })
            }
        } catch (e: Exception) {
            onError("请求异常: ${e.message}")
        }
    }
}