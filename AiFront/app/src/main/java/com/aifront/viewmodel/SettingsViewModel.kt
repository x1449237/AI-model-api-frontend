package com.aifront.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aifront.data.model.AIModel
import com.aifront.data.model.VendorConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("aifront_settings", 0)

    private val _apiKeys = MutableStateFlow<Map<String, String>>(emptyMap())
    val apiKeys: StateFlow<Map<String, String>> = _apiKeys

    init { loadApiKeys() }

    private fun loadApiKeys() {
        val keys = mutableMapOf<String, String>()
        VendorConfig.getAllVendors().forEach { vendor ->
            val key = prefs.getString("api_key_${vendor.id}", "") ?: ""
            if (key.isNotEmpty()) keys[vendor.id] = key
        }
        _apiKeys.value = keys
    }

    fun setApiKey(vendorId: String, apiKey: String) {
        prefs.edit().putString("api_key_$vendorId", apiKey).apply()
        loadApiKeys()
    }

    fun getApiKey(vendorId: String): String {
        return prefs.getString("api_key_$vendorId", "") ?: ""
    }

    fun removeApiKey(vendorId: String) {
        prefs.edit().remove("api_key_$vendorId").apply()
        loadApiKeys()
    }

    fun hasApiKey(vendorId: String): Boolean {
        return getApiKey(vendorId).isNotEmpty()
    }
}

class ClusterViewModel(application: Application) : AndroidViewModel(application) {
    private val _selectedModels = MutableStateFlow<List<AIModel>>(emptyList())
    val selectedModels: StateFlow<List<AIModel>> = _selectedModels

    private val _isClusterMode = MutableStateFlow(false)
    val isClusterMode: StateFlow<Boolean> = _isClusterMode

    private val _clusterResults = MutableStateFlow<Map<String, String>>(emptyMap())
    val clusterResults: StateFlow<Map<String, String>> = _clusterResults

    fun toggleClusterMode() {
        _isClusterMode.value = !_isClusterMode.value
        if (!_isClusterMode.value) {
            _selectedModels.value = emptyList()
            _clusterResults.value = emptyMap()
        }
    }

    fun toggleModel(model: AIModel) {
        val current = _selectedModels.value.toMutableList()
        if (current.any { it.id == model.id }) {
            current.removeAll { it.id == model.id }
        } else {
            if (current.size < 5) current.add(model)
        }
        _selectedModels.value = current
    }

    fun clearSelection() {
        _selectedModels.value = emptyList()
        _clusterResults.value = emptyMap()
    }

    fun isModelSelected(modelId: String): Boolean {
        return _selectedModels.value.any { it.id == modelId }
    }

    fun getEnabledModels(): List<AIModel> {
        return VendorConfig.getTextModels()
    }
}