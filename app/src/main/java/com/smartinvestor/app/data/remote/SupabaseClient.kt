package com.smartinvestor.app.data.remote

import com.smartinvestor.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

// ══════════════════════════════════════════════════════════
// Smart Investor — Supabase Client
// Keys injected from local.properties via BuildConfig
// Realtime removed to prevent WebSocket connection at init
// ══════════════════════════════════════════════════════════

object SupabaseClient {

    /**
     * Sanitize the URL from BuildConfig.
     * local.properties escapes the colon in https: producing "https\://"
     * which Gradle reads as-is. We strip any backslash before the colon.
     */
    private val sanitizedUrl: String by lazy {
        BuildConfig.SUPABASE_URL
            .replace("\\:", ":")   // fix escaped colon
            .replace("\\\\:", ":") // fix double-escaped colon
            .trim()
    }

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = sanitizedUrl,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            // Realtime removed — not needed at launch and it opens
            // a persistent WebSocket that can block/ANR during init.
        }
    }

    val auth get() = client.auth
    val db get() = client.postgrest
}
