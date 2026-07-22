# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.aifront.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**