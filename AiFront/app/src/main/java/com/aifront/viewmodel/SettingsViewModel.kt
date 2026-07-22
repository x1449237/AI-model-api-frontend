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

    private val _customApiUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val customApiUrls: StateFlow<Map<String, String>> = _customApiUrls

    private val _aiNicknames = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiNicknames: StateFlow<Map<String, String>> = _aiNicknames

    init { loadAll() }

    private fun loadAll() {
        val keys = mutableMapOf<String, String>()
        val urls = mutableMapOf<String, String>()
        val nicknames = mutableMapOf<String, String>()
        VendorConfig.getAllVendors().forEach { vendor ->
            val key = prefs.getString("api_key_${vendor.id}", "") ?: ""
            if (key.isNotEmpty()) keys[vendor.id] = key
            val url = prefs.getString("api_url_${vendor.id}", "") ?: ""
            if (url.isNotEmpty()) urls[vendor.id] = url
            val nickname = prefs.getString("ai_nickname_${vendor.id}", "") ?: ""
            if (nickname.isNotEmpty()) nicknames[vendor.id] = nickname
        }
        _apiKeys.value = keys
        _customApiUrls.value = urls
        _aiNicknames.value = nicknames
    }

    fun setApiKey(vendorId: String, apiKey: String) {
        prefs.edit().putString("api_key_$vendorId", apiKey).apply()
        loadAll()
    }

    fun getApiKey(vendorId: String): String {
        return prefs.getString("api_key_$vendorId", "") ?: ""
    }

    fun removeApiKey(vendorId: String) {
        prefs.edit().remove("api_key_$vendorId").apply()
        loadAll()
    }

    fun hasApiKey(vendorId: String): Boolean {
        return getApiKey(vendorId).isNotEmpty()
    }

    fun setCustomApiUrl(vendorId: String, url: String) {
        prefs.edit().putString("api_url_$vendorId", url).apply()
        loadAll()
    }

    fun getCustomApiUrl(vendorId: String): String {
        return prefs.getString("api_url_$vendorId", "") ?: ""
    }

    fun getEffectiveApiUrl(vendorId: String): String {
        val custom = getCustomApiUrl(vendorId)
        if (custom.isNotEmpty()) return custom
        return VendorConfig.getVendorById(vendorId)?.apiBaseUrl ?: ""
    }

    fun setAiNickname(vendorId: String, nickname: String) {
        prefs.edit().putString("ai_nickname_$vendorId", nickname).apply()
        loadAll()
    }

    fun getAiNickname(vendorId: String): String {
        return prefs.getString("ai_nickname_$vendorId", "") ?: ""
    }

    fun getModelCount(vendorId: String): Int {
        return VendorConfig.getModelsByVendor(vendorId).size
    }

    fun getModelsByVendor(vendorId: String): List<AIModel> {
        return VendorConfig.getModelsByVendor(vendorId)
    }

    fun getVendorName(vendorId: String): String {
        return VendorConfig.getVendorById(vendorId)?.name ?: vendorId
    }

    fun getVendorNameEn(vendorId: String): String {
        return VendorConfig.getVendorById(vendorId)?.nameEn ?: vendorId
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