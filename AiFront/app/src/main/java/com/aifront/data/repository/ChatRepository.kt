package com.aifront.data.repository

import android.content.Context
import com.aifront.data.api.ApiService
import com.aifront.data.local.AppDatabase
import com.aifront.data.local.dao.ChatMessageDao
import com.aifront.data.local.dao.ConversationDao
import com.aifront.data.model.*
import kotlinx.coroutines.flow.Flow

class ChatRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val conversationDao: ConversationDao = db.conversationDao()
    private val messageDao: ChatMessageDao = db.chatMessageDao()
    private val apiService = ApiService()

    fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAllConversations()
    fun getConversationsByType(type: ConversationType): Flow<List<Conversation>> =
        conversationDao.getConversationsByType(type.name)

    suspend fun getConversationById(id: Long): Conversation? = conversationDao.getConversationById(id)
    suspend fun createConversation(conversation: Conversation): Long = conversationDao.insertConversation(conversation)
    suspend fun updateConversation(conversation: Conversation) = conversationDao.updateConversation(conversation)
    suspend fun deleteConversation(id: Long) = conversationDao.deleteConversationById(id)

    fun getMessagesByConversation(conversationId: Long): Flow<List<ChatMessage>> =
        messageDao.getMessagesByConversation(conversationId)

    suspend fun getMessagesList(conversationId: Long): List<ChatMessage> =
        messageDao.getMessagesListByConversation(conversationId)

    suspend fun insertMessage(message: ChatMessage): Long = messageDao.insertMessage(message)
    suspend fun updateMessage(message: ChatMessage) = messageDao.updateMessage(message)

    fun chatStream(
        baseUrl: String, apiKey: String, model: AIModel,
        messages: List<MessageContent>, temperature: Float, topP: Float,
        maxTokens: Int, apiType: ApiType
    ): Flow<StreamResponse> = apiService.chatStream(
        baseUrl, apiKey, model.apiEndpoint, messages, temperature, topP, maxTokens, apiType
    )

    suspend fun chatSync(
        baseUrl: String, apiKey: String, model: AIModel,
        messages: List<MessageContent>, temperature: Float, topP: Float,
        maxTokens: Int, apiType: ApiType
    ): String = apiService.chatSync(
        baseUrl, apiKey, model.apiEndpoint, messages, temperature, topP, maxTokens, apiType
    )

    suspend fun generateImage(
        baseUrl: String, apiKey: String, model: AIModel,
        prompt: String, n: Int, size: String
    ): ImageGenerationResponse = apiService.generateImage(
        baseUrl, apiKey, model.apiEndpoint, prompt, n, size
    )
}