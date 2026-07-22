package com.aifront.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aifront.data.model.*
import com.aifront.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ImageGenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)

    private val _generatedImages = MutableStateFlow<List<String>>(emptyList())
    val generatedImages: StateFlow<List<String>> = _generatedImages

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _selectedRatio = MutableStateFlow("1:1")
    val selectedRatio: StateFlow<String> = _selectedRatio

    private val _imageCount = MutableStateFlow(1)
    val imageCount: StateFlow<Int> = _imageCount

    val imageRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:4")

    val ratioToSize = mapOf(
        "1:1" to "1024x1024",
        "16:9" to "1792x1024",
        "9:16" to "1024x1792",
        "4:3" to "1280x960",
        "3:4" to "960x1280"
    )

    fun setRatio(ratio: String) { _selectedRatio.value = ratio }
    fun setImageCount(count: Int) { _imageCount.value = count.coerceIn(1, 4) }

    fun generateImage(
        prompt: String, model: AIModel, vendor: Vendor, apiKey: String, customBaseUrl: String = ""
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generatedImages.value = emptyList()

            try {
                val size = ratioToSize[_selectedRatio.value] ?: "1024x1024"
                val baseUrl = customBaseUrl.ifEmpty { vendor.apiBaseUrl }
                val response = repository.generateImage(
                    baseUrl, apiKey, model, prompt, _imageCount.value, size
                )
                _generatedImages.value = response.images.map { it.url }
            } catch (e: Exception) {
                _generatedImages.value = emptyList()
            } finally {
                _isGenerating.value = false
            }
        }
    }
}