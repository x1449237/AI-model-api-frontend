# ── 通用 ──
-keepattributes Signature
-keepattributes *Annotation*

# ── 数据模型 ──
-keep class com.aifront.data.model.** { *; }

# ── Gson ──
-keepattributes SerializedName
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

# ── OkHttp / Retrofit ──
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# ── Room ──
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ── Kotlin Coroutines ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Kotlin ──
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }