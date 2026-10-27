# Smart Investor ProGuard Rules

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.smartinvestor.app.**$$serializer { *; }
-keepclassmembers class com.smartinvestor.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.smartinvestor.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Supabase
-keep class io.github.jan.supabase.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }

# Keep Room entities
-keep class com.smartinvestor.app.data.local.entities.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
