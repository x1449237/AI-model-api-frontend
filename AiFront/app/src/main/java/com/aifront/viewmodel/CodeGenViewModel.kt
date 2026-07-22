package com.aifront.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aifront.data.model.*
import com.aifront.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CodeGenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)

    private val _selectedLanguage = MutableStateFlow("Python")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    private val _generatedCode = MutableStateFlow("")
    val generatedCode: StateFlow<String> = _generatedCode

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _streamingCode = MutableStateFlow("")
    val streamingCode: StateFlow<String> = _streamingCode

    val supportedLanguages = listOf(
        "C++", "C", "Java", "Python", "JavaScript", "PHP", "SQL", "JSON",
        "HTML/CSS", "Go", "Rust", "Swift", "Kotlin", "Shell", "XML", "YAML"
    )

    val fileExtensions = mapOf(
        "C++" to ".cpp", "C" to ".c", "Java" to ".java", "Python" to ".py",
        "JavaScript" to ".js", "PHP" to ".php", "SQL" to ".sql", "JSON" to ".json",
        "HTML/CSS" to ".html", "Go" to ".go", "Rust" to ".rs", "Swift" to ".swift",
        "Kotlin" to ".kt", "Shell" to ".sh", "XML" to ".xml", "YAML" to ".yaml"
    )

    fun setLanguage(language: String) { _selectedLanguage.value = language }

    fun generateCode(
        prompt: String, model: AIModel, vendor: Vendor, apiKey: String
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generatedCode.value = ""
            _streamingCode.value = ""

            val language = _selectedLanguage.value
            val codePrompt = buildString {
                append("你是一个专业的代码生成助手。请根据以下需求生成${language}代码。\n")
                append("要求：\n")
                append("1. 只输出代码，不要包含任何解释说明\n")
                append("2. 代码要完整可运行\n")
                append("3. 包含必要的注释\n\n")
                append("需求：$prompt")
            }

            val messages = listOf(
                MessageContent("system", "你是一个专业的代码生成助手，只输出代码不输出解释。"),
                MessageContent("user", codePrompt)
            )

            var fullContent = ""
            repository.chatStream(
                vendor.apiBaseUrl, apiKey, model, messages,
                0.3f, 0.9f, 4096, vendor.apiType
            ).collect { response ->
                if (response.isFinished) {
                    fullContent = response.content
                    _isGenerating.value = false
                } else {
                    _streamingCode.value = _streamingCode.value + response.content
                }
            }

            _generatedCode.value = fullContent.ifEmpty { _streamingCode.value }
            _streamingCode.value = ""
        }
    }

    fun getFileExtension(language: String): String {
        return fileExtensions[language] ?: ".txt"
    }
}