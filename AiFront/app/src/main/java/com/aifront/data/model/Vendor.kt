package com.aifront.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey val id: String,
    val name: String,
    val nameEn: String,
    val country: String,
    val description: String,
    val website: String,
    val apiBaseUrl: String,
    val apiType: ApiType
)

enum class ApiType {
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    GOOGLE,
    CUSTOM
}