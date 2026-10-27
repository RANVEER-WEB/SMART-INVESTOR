import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.smartinvestor.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.smartinvestor.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use {
                localProperties.load(it)
            }
        }

        val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: (project.findProperty("SUPABASE_URL") as String? ?: "")
        val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: (project.findProperty("SUPABASE_ANON_KEY") as String? ?: "")
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: (project.findProperty("GEMINI_API_KEY") as String? ?: "")
        val finnhubApiKey = localProperties.getProperty("FINNHUB_API_KEY") ?: (project.findProperty("FINNHUB_API_KEY") as String? ?: "")

        // Inject API keys into BuildConfig
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "FINNHUB_API_KEY", "\"$finnhubApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// ══════════════════════════════════════════════════════════════
// DEPENDENCY STRATEGY — Ktor Version
//
// Supabase 3.0.2 uses Ktor 3.x (pulled in transitively via BOM).
// FinnhubApi uses Ktor 2.x directly in source code.
// Gemini AI now uses OkHttp directly (GeminiApi.kt was rewritten)
// — the generativeai SDK has been removed to eliminate the
// Ktor 2.x vs 3.x conflict permanently.
//
// ⚠️  If you ever re-add the generativeai SDK, ensure it supports
//     Ktor 3.x before upgrading Supabase or vice versa.
// ══════════════════════════════════════════════════════════════

dependencies {
    // ── Compose BOM ──
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ── Compose UI ──
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ── Core AndroidX ──
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ── Navigation ──
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ── Hilt (Dependency Injection) ──
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ── Supabase (restored to 3.0.2 — its correct stable version) ──
    // Uses Ktor 3.x transitively. Gemini AI now uses OkHttp directly,
    // so there is no longer a Ktor version conflict.
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    // realtime-kt removed — WebSocket init contributed to ANR. Re-add in Phase 2.

    // ── Ktor (for FinnhubApi) ──
    // Pinned to 3.0.1 to match Supabase 3.0.2 transitive dependencies
    // to prevent mixed 2.x and 3.x Ktor libraries on the classpath.
    implementation("io.ktor:ktor-client-android:3.0.1")
    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
    implementation("io.ktor:ktor-client-logging:3.0.1")

    // ── OkHttp (for GeminiApi direct REST calls) ──
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ── Kotlinx Serialization ──
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ── Google Generative AI (Gemini) ──
    // Removed generativeai SDK to avoid Ktor conflict. Using OkHttp instead.

    // ── Room Database (Offline Cache) ──
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── DataStore (Preferences) ──
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Biometric Authentication ──
    implementation("androidx.biometric:biometric:1.1.0")

    // ── Lottie Animations ──
    implementation("com.airbnb.android:lottie-compose:6.6.2")

    // ── Vico Charts (Stock Graphs) ──
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.3")

    // ── Coil (Image Loading) ──
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ── Testing ──
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
