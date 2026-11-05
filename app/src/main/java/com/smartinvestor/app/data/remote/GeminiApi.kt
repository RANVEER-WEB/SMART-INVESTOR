package com.smartinvestor.app.data.remote

import android.util.Log
import com.smartinvestor.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
// Target Model: gemini-3.8-flash (Gemini 3 generation)
// Developer: ranveermandal183@gmail.com
// ══════════════════════════════════════════════════════════════

/**
 * Custom domain exceptions for Gemini API communication.
 */
sealed class GeminiApiException(
    message: String,
    val statusCode: Int? = null,
    val isRetryable: Boolean = false,
    cause: Throwable? = null
) : Exception(message, cause) {
    class InvalidRequestException(msg: String, code: Int = 400) : GeminiApiException(msg, code, false)
    class AuthenticationException(msg: String, code: Int = 401) : GeminiApiException(msg, code, false)
    class ModelUnavailableException(msg: String, code: Int = 404) : GeminiApiException(msg, code, false)
    class RateLimitExceededException(msg: String, code: Int = 429) : GeminiApiException(msg, code, true)
    class ServerUnavailableException(msg: String, code: Int = 503) : GeminiApiException(msg, code, true)
    class NetworkException(msg: String, cause: Throwable? = null) : GeminiApiException(msg, null, true, cause)
    class MalformedResponseException(msg: String) : GeminiApiException(msg, null, false)
}

@Singleton
class GeminiApi @Inject constructor(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    // ── Multi-turn chat history (role: "user" | "model") ──
    private val chatHistory = mutableListOf<JsonObject>()

    /**
     * Send a message to the AI Advisor and get a response.
     * Maintains conversation history for multi-turn chat.
     */
    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            throw GeminiApiException.AuthenticationException(
                "Gemini API key is not configured in local.properties"
            )
        }

        // Sliding context window: prune older history to keep at most last 10 turns
        if (chatHistory.size > MAX_HISTORY_TURNS) {
            val toRemove = chatHistory.size - MAX_HISTORY_TURNS
            repeat(toRemove) { chatHistory.removeAt(0) }
        }

        // Add user turn to history
        chatHistory.add(buildContent("user", userMessage))

        val requestJson = buildJsonObject {
            put("systemInstruction", buildJsonObject {
                put("parts", buildJsonArray {
                    add(buildJsonObject { put("text", SYSTEM_PROMPT) })
                })
            })
            put("contents", buildJsonArray {
                chatHistory.forEach { add(it) }
            })
            put("generationConfig", buildJsonObject {
                // Gemini 3.8 Flash uses thinkingLevel (LOW, MEDIUM, HIGH)
                // "MINIMAL" is not supported. Legacy sampling (topK, topP, candidateCount) removed.
                put("thinkingConfig", buildJsonObject {
                    put("thinkingLevel", "MEDIUM")
                    put("includeThoughts", false)
                })
                put("maxOutputTokens", 4096)
            })
        }

        val request = Request.Builder()
            .url(ENDPOINT_URL)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val responseBody = try {
            executeWithRetry(request)
        } catch (e: Exception) {
            // Remove user message from chatHistory on failure to avoid desync
            chatHistory.removeLastOrNull()
            throw e
        }

        val text = parseGeminiResponse(responseBody)
        Log.d(TAG, "Gemini 3.8 Flash responded successfully (${text.length} chars)")
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
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            throw GeminiApiException.AuthenticationException(
                "Gemini API key is not configured in local.properties"
            )
        }

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
            put("systemInstruction", buildJsonObject {
                put("parts", buildJsonArray {
                    add(buildJsonObject { put("text", SYSTEM_PROMPT) })
                })
            })
            put("contents", buildJsonArray {
                add(buildContent("user", prompt))
            })
            put("generationConfig", buildJsonObject {
                put("thinkingConfig", buildJsonObject {
                    put("thinkingLevel", "LOW")
                    put("includeThoughts", false)
                })
                put("maxOutputTokens", 1024)
            })
        }

        val request = Request.Builder()
            .url(ENDPOINT_URL)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val responseBody = executeWithRetry(request)
        parseGeminiResponse(responseBody)
    }

    /**
     * Reset the chat session (start a new conversation).
     */
    fun resetChat() {
        chatHistory.clear()
    }

    // ── Network Execution & Resilient Retry Engine ───────────

    private suspend fun executeWithRetry(request: Request): String {
        var lastException: Exception? = null
        var delayMs = 1000L

        for (attempt in 1..MAX_RETRIES) {
            try {
                return executeSingleCall(request)
            } catch (e: GeminiApiException) {
                lastException = e
                if (!e.isRetryable || attempt == MAX_RETRIES) {
                    throw e
                }
                Log.w(TAG, "Retryable error on attempt $attempt/$MAX_RETRIES (${e.message}). Retrying in ${delayMs}ms...")
                delay(delayMs)
                delayMs *= 2 // Exponential backoff (1s -> 2s -> 4s)
            } catch (e: IOException) {
                val netEx = when (e) {
                    is SocketTimeoutException -> GeminiApiException.NetworkException("Request timed out connecting to Gemini AI.", e)
                    is UnknownHostException -> GeminiApiException.NetworkException("Unable to resolve Gemini host. Please check your internet connection.", e)
                    else -> GeminiApiException.NetworkException("Network error: ${e.message}", e)
                }
                lastException = netEx
                if (attempt == MAX_RETRIES) throw netEx
                Log.w(TAG, "Network failure on attempt $attempt/$MAX_RETRIES. Retrying in ${delayMs}ms...")
                delay(delayMs)
                delayMs *= 2
            }
        }

        throw lastException ?: GeminiApiException.ServerUnavailableException("Failed to communicate with Gemini after $MAX_RETRIES attempts")
    }

    private fun executeSingleCall(request: Request): String {
        val response = httpClient.newCall(request).execute()
        return response.use { res ->
            val body = res.body?.string().orEmpty()
            val code = res.code

            if (!res.isSuccessful) {
                Log.e(TAG, "Gemini API HTTP $code: $body")
                val parsedErrorMessage = extractErrorMessage(body) ?: "HTTP $code error from Gemini"

                when (code) {
                    400 -> throw GeminiApiException.InvalidRequestException(parsedErrorMessage, code)
                    401, 403 -> throw GeminiApiException.AuthenticationException(
                        "Authentication error ($code): Please verify GEMINI_API_KEY in local.properties. $parsedErrorMessage",
                        code
                    )
                    404 -> throw GeminiApiException.ModelUnavailableException(
                        "Model or endpoint unavailable (404): $parsedErrorMessage",
                        code
                    )
                    429 -> throw GeminiApiException.RateLimitExceededException(
                        "Rate limit exceeded (429): $parsedErrorMessage",
                        code
                    )
                    500, 502, 503, 504 -> throw GeminiApiException.ServerUnavailableException(
                        "Gemini service unavailable ($code): $parsedErrorMessage",
                        code
                    )
                    else -> throw GeminiApiException.InvalidRequestException(
                        "Gemini API error ($code): $parsedErrorMessage",
                        code
                    )
                }
            }

            if (body.isBlank()) {
                throw GeminiApiException.MalformedResponseException("Received empty response body from Gemini API")
            }
            body
        }
    }

    // ── Private Helpers ──────────────────────────────────────

    private fun buildContent(role: String, text: String): JsonObject =
        buildJsonObject {
            put("role", role)
            put("parts", buildJsonArray {
                add(buildJsonObject { put("text", text) })
            })
        }

    fun parseGeminiResponse(responseBody: String): String {
        return try {
            val root = json.parseToJsonElement(responseBody).jsonObject
            val candidates = root["candidates"]?.jsonArray
            if (candidates.isNullOrEmpty()) {
                val errorMsg = root["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                if (!errorMsg.isNullOrBlank()) {
                    throw GeminiApiException.MalformedResponseException(errorMsg)
                }
                throw GeminiApiException.MalformedResponseException("No candidate responses returned by model.")
            }

            val parts = candidates[0].jsonObject["content"]
                ?.jsonObject?.get("parts")
                ?.jsonArray

            if (parts.isNullOrEmpty()) {
                throw GeminiApiException.MalformedResponseException("Candidate content contains no parts.")
            }

            // In Gemini 3, if includeThoughts was true or thoughts were returned, find the non-thought part
            val textPart = parts.firstOrNull { part ->
                val isThought = part.jsonObject["thought"]?.jsonPrimitive?.booleanOrNull ?: false
                !isThought && part.jsonObject["text"] != null
            } ?: parts.firstOrNull { it.jsonObject["text"] != null }

            val text = textPart?.jsonObject?.get("text")?.jsonPrimitive?.content
            if (text.isNullOrBlank()) {
                throw GeminiApiException.MalformedResponseException("Candidate parts contained empty text.")
            }
            text
        } catch (e: GeminiApiException) {
            throw e
        } catch (e: Exception) {
            throw GeminiApiException.MalformedResponseException("Failed to parse Gemini response: ${e.message}")
        }
    }

    private fun extractErrorMessage(responseBody: String): String? {
        return try {
            json.parseToJsonElement(responseBody)
                .jsonObject["error"]
                ?.jsonObject?.get("message")
                ?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "GeminiApi"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        const val MODEL_ID = "gemini-3.8-flash"
        const val ENDPOINT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_ID:generateContent"

        private const val MAX_HISTORY_TURNS = 10
        private const val MAX_RETRIES = 3

        const val SYSTEM_PROMPT = """
You are Smart Investor AI, an expert financial analyst and investment copilot for the Smart Investor app.

CORE GUIDELINES:
- You specialize in the Indian stock market (NSE/BSE), equities, mutual funds, ETFs, and macroeconomics.
- All insights are for educational and analytical purposes only, not financial advisory services.
- Never guarantee returns or provide direct buy/sell mandates. Always advise consulting a SEBI-registered investment advisor.
- When evaluating queries, tailor your depth to the user's risk profile and capital context.
- Use clear, professional, concise language with bullet points and bold headers.

STRUCTURE YOUR IN-DEPTH INVESTMENT ANALYSIS AS FOLLOWS:
1. **Summary**: Concise overview answering the core question directly.
2. **Bullish Scenario**: Growth catalysts, positive earnings drivers, or favorable trends.
3. **Bearish Scenario**: Downside risks, valuation headwinds, or competitive threats.
4. **Neutral Scenario**: Base-case baseline or consolidation expectations.
5. **Key Risk Factors**: Volatility, regulatory changes, macro risks.
6. **Educational Disclaimer**: Mandatory reminder that past performance does not guarantee future results.

Lead Developer: Ranveer Mandal (ranveermandal183@gmail.com | +91 8340320664)"""
    }
}
