package com.smartinvestor.app.data.remote

import android.util.Log
import com.smartinvestor.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ══════════════════════════════════════════════════════════════
// Smart Investor — Gemini AI Client (OkHttp-based)
//
// WHY OkHttp instead of the generativeai SDK:
//   The SDK (0.9.0) uses Ktor 2.x internally. Supabase 3.x and
//   FinnhubApi both require Ktor 3.x. The two Ktor major versions
//   are binary-incompatible — only one can be on the classpath.
//   Using OkHttp directly removes the SDK's Ktor dependency entirely,
//   letting Supabase + FinnhubApi run on Ktor 3.x without conflict.
//
// API: Gemini 1.5 Flash via REST (v1)
// Developer: ranveermandal183@gmail.com
// ══════════════════════════════════════════════════════════════

@Singleton
class GeminiApi @Inject constructor() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // ── Multi-turn chat history (role: "user" | "model") ──
    private val chatHistory = mutableListOf<JsonObject>()

    private val baseUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    /**
     * Send a message to the AI Advisor and get a response.
     * Maintains conversation history for multi-turn chat.
     */
    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        // Prepend system instruction on the very first user turn
        val effectiveMessage = if (chatHistory.isEmpty()) {
            "$SYSTEM_PROMPT\n\n---\n\nUser: $userMessage"
        } else {
            userMessage
        }

        chatHistory.add(buildContent("user", effectiveMessage))

        val requestJson = buildJsonObject {
            put("contents", buildJsonArray {
                chatHistory.forEach { add(it) }
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.7)
                put("topK", 40)
                put("topP", 0.95)
                put("maxOutputTokens", 2048)
            })
        }

        val request = Request.Builder()
            .url("$baseUrl?key=${BuildConfig.GEMINI_API_KEY}")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val responseBody = httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Empty response from Gemini")
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ${response.code}: $body")
                // Remove failed user message from history to keep it consistent
                chatHistory.removeLastOrNull()
                throw Exception("Gemini API error ${response.code}: $body")
            }
            body
        }

        val text = parseGeminiResponse(responseBody)
        Log.d(TAG, "Gemini responded successfully")
        chatHistory.add(buildContent("model", text))
        text
    }

    /**
     * Get a focused stock analysis from Gemini (single-turn, no history).
     */
    suspend fun analyzeStock(
        symbol: String,
        currentPrice: Double,
        change: Double,
        changePercent: Double
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the stock $symbol for an Indian retail investor:
            - Current Price: ₹$currentPrice
            - Change: ₹$change ($changePercent%)
            
            Provide:
            1. Current market sentiment (Bullish / Bearish / Neutral)
            2. Key factors to watch
            3. Risk level (Low / Medium / High)
            
            Be concise and actionable. Remind the user this is for educational purposes only.
        """.trimIndent()

        val requestJson = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildContent("user", prompt))
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.4)
                put("maxOutputTokens", 512)
            })
        }

        val request = Request.Builder()
            .url("$baseUrl?key=${BuildConfig.GEMINI_API_KEY}")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val responseBody = httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Empty response from Gemini")
            if (!response.isSuccessful) {
                throw Exception("Gemini API error ${response.code}: $body")
            }
            body
        }

        parseGeminiResponse(responseBody)
    }

    /**
     * Reset the chat session (start a new conversation).
     */
    fun resetChat() {
        chatHistory.clear()
    }

    // ── Private Helpers ──────────────────────────────────────

    private fun buildContent(role: String, text: String): JsonObject =
        buildJsonObject {
            put("role", role)
            put("parts", buildJsonArray {
                add(buildJsonObject { put("text", text) })
            })
        }

    private fun parseGeminiResponse(responseBody: String): String {
        return try {
            json.parseToJsonElement(responseBody)
                .jsonObject["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: "I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            "I couldn't generate a response. Please try again."
        }
    }

    companion object {
        private const val TAG = "GeminiApi"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val SYSTEM_PROMPT = """
You are Smart Investor AI, a professional financial advisor built into the Smart Investor app.

Guidelines:
- You specialize in the Indian stock market (NSE/BSE).
- Always mention that your advice is for educational purposes only, not financial advice.
- Use simple language that a beginner investor can understand.
- When discussing stocks, mention key metrics like P/E ratio, market cap, and sector performance.
- Be concise but thorough. Use bullet points for clarity.
- If asked about specific buy/sell decisions, remind users to consult a SEBI-registered advisor.
- Be encouraging and supportive of learning about investing.

Developer: Ranveer Mandal (ranveermandal183@gmail.com)"""
    }
}
