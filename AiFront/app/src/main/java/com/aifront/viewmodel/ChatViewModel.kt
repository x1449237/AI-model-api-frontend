package com.aifront.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aifront.data.model.*
import com.aifront.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)

    private val _selectedModel = MutableStateFlow<AIModel?>(null)
    val selectedModel: StateFlow<AIModel?> = _selectedModel

    private val _selectedVendor = MutableStateFlow<Vendor?>(null)
    val selectedVendor: StateFlow<Vendor?> = _selectedVendor

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature

    private val _topP = MutableStateFlow(0.9f)
    val topP: StateFlow<Float> = _topP

    private val _maxTokens = MutableStateFlow(4096)
    val maxTokens: StateFlow<Int> = _maxTokens

    val allConversations: StateFlow<List<Conversation>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectModel(model: AIModel) {
        _selectedModel.value = model
        _selectedVendor.value = VendorConfig.getVendorById(model.vendorId)
    }

    fun selectVendor(vendor: Vendor) { _selectedVendor.value = vendor }

    fun setTemperature(value: Float) { _temperature.value = value }
    fun setTopP(value: Float) { _topP.value = value }
    fun setMaxTokens(value: Int) { _maxTokens.value = value }

    fun createNewConversation(type: ConversationType = ConversationType.TEXT) {
        viewModelScope.launch {
            val conv = Conversation(
                title = "新对话",
                conversationType = type,
                modelId = _selectedModel.value?.id,
                modelName = _selectedModel.value?.displayName,
                vendorName = _selectedVendor.value?.name
            )
            val id = repository.createConversation(conv)
            _currentConversation.value = conv.copy(id = id)
            _messages.value = emptyList()
            _streamingContent.value = ""
            _error.value = null
        }
    }

    fun loadConversation(conversationId: Long) {
        viewModelScope.launch {
            val conv = repository.getConversationById(conversationId)
            if (conv != null) {
                _currentConversation.value = conv
                val msgs = repository.getMessagesList(conversationId)
                _messages.value = msgs
            }
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            if (_currentConversation.value?.id == conversationId) {
                _currentConversation.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun sendMessage(
        content: String,
        apiKey: String,
        customBaseUrl: String = "",
        isClusterMode: Boolean = false,
        clusterModels: List<AIModel> = emptyList()
    ) {
        val model = _selectedModel.value ?: return
        val vendor = _selectedVendor.value ?: return

        viewModelScope.launch {
            _error.value = null
            var conv = _currentConversation.value
            if (conv == null) {
                val newConv = Conversation(
                    title = content.take(30),
                    conversationType = ConversationType.TEXT,
                    modelId = model.id,
                    modelName = model.displayName,
                    vendorName = vendor.name,
                    isClusterMode = isClusterMode,
                    clusterModelIds = if (isClusterMode) clusterModels.joinToString(",") { it.id } else null
                )
                val id = repository.createConversation(newConv)
                conv = newConv.copy(id = id)
                _currentConversation.value = conv
            }

            val userMessage = ChatMessage(
                conversationId = conv.id,
                role = "user",
                content = content,
                modelId = model.id,
                modelName = model.displayName,
                vendorName = vendor.name
            )
            repository.insertMessage(userMessage)
            _messages.value = _messages.value + userMessage

            val historyMessages = buildMessageHistory(content)

            val effectiveBaseUrl = customBaseUrl.ifEmpty { vendor.apiBaseUrl }

            if (isClusterMode && clusterModels.isNotEmpty()) {
                sendClusterMessages(conv.id, historyMessages, apiKey, effectiveBaseUrl, clusterModels)
            } else {
                sendSingleMessage(conv.id, model, vendor, effectiveBaseUrl, historyMessages, apiKey)
            }
        }
    }

    private suspend fun sendSingleMessage(
        conversationId: Long, model: AIModel, vendor: Vendor,
        baseUrl: String, messages: List<MessageContent>, apiKey: String
    ) {
        _isStreaming.value = true
        _streamingContent.value = ""
        val startTime = System.currentTimeMillis()

        var fullContent = ""

        repository.chatStream(
            baseUrl, apiKey, model, messages,
            _temperature.value, _topP.value, _maxTokens.value, vendor.apiType
        ).collect { response ->
            if (response.isFinished) {
                fullContent = response.content
                _streamingContent.value = ""
                _isStreaming.value = false
            } else {
                _streamingContent.value = _streamingContent.value + response.content
            }
        }

        val responseTime = System.currentTimeMillis() - startTime
        val assistantMessage = ChatMessage(
            conversationId = conversationId,
            role = "assistant",
            content = fullContent.ifEmpty { _streamingContent.value },
            modelId = model.id,
            modelName = model.displayName,
            vendorName = vendor.name,
            responseTimeMs = responseTime
        )
        repository.insertMessage(assistantMessage)
        _messages.value = _messages.value + assistantMessage
        _streamingContent.value = ""

        repository.updateConversation(
            _currentConversation.value!!.copy(
                updatedAt = System.currentTimeMillis(),
                messageCount = _messages.value.size
            )
        )
    }

    private suspend fun sendClusterMessages(
        conversationId: Long, messages: List<MessageContent>,
        apiKey: String, baseUrl: String, clusterModels: List<AIModel>
    ) {
        _isStreaming.value = true
        _streamingContent.value = ""
        val results = mutableMapOf<String, String>()

        val jobs = clusterModels.map { model ->
            val vendor = VendorConfig.getVendorById(model.vendorId) ?: return@map null
            viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                var fullContent = ""

                try {
                    repository.chatStream(
                        baseUrl, apiKey, model, messages,
                        _temperature.value, _topP.value, _maxTokens.value, vendor.apiType
                    ).collect { response ->
                        if (response.isFinished) {
                            fullContent = response.content
                        } else {
                            results[model.id] = (results[model.id] ?: "") + response.content
                        }
                    }
                } catch (e: Exception) {
                    fullContent = "Error: ${e.message}"
                }

                val responseTime = System.currentTimeMillis() - startTime
                val finalContent = fullContent.ifEmpty { results[model.id] ?: "无响应" }
                val formattedContent = "【${vendor.name} - ${model.displayName}】\n响应时间: ${responseTime}ms\n\n$finalContent"

                val assistantMessage = ChatMessage(
                    conversationId = conversationId,
                    role = "assistant",
                    content = formattedContent,
                    modelId = model.id,
                    modelName = model.displayName,
                    vendorName = vendor.name,
                    responseTimeMs = responseTime
                )
                repository.insertMessage(assistantMessage)
                _messages.value = _messages.value + assistantMessage
            }
        }
        jobs.filterNotNull().let { joinAll(*it.toTypedArray()) }
        _isStreaming.value = false
    }

    private fun buildMessageHistory(currentContent: String): List<MessageContent> {
        val history = mutableListOf<MessageContent>()
        history.add(MessageContent("system", "你是一个有帮助的AI助手。"))
        for (msg in _messages.value) {
            history.add(MessageContent(msg.role, msg.content))
        }
        history.add(MessageContent("user", currentContent))
        return history
    }

    fun clearError() { _error.value = null }
}