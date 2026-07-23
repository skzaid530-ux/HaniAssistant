# Keep our model classes
-keep class com.hani.assistant.** { *; }
# Porcupine
-keep class ai.picovoice.** { *; }
# Retrofit / Gson
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
# Keep BuildConfig for API key
-keep class com.hani.assistant.BuildConfig { *; }
# Keep parcelable
-keep class * implements android.os.Parcelable { *; }
