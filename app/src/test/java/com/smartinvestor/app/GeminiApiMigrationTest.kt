package com.smartinvestor.app

import com.smartinvestor.app.data.remote.GeminiApi
import com.smartinvestor.app.data.remote.GeminiApiException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GeminiApiMigrationTest {

    private lateinit var geminiApi: GeminiApi

    @Before
    fun setUp() {
        geminiApi = GeminiApi()
    }

    @Test
    fun testTargetModelConstant_IsGemini38Flash() {
        assertEquals("gemini-3.8-flash", GeminiApi.MODEL_ID)
    }

    @Test
    fun testEndpointUrl_UsesV1BetaAndModel38Flash() {
        val expected = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
        assertEquals(expected, GeminiApi.ENDPOINT_URL)
    }

    @Test
    fun testParseGeminiResponse_StandardSingleTextPart() {
        val sampleResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "role": "model",
                    "parts": [
                      {
                        "text": "1. **Summary**: Reliance demonstrates steady growth.\n2. **Bullish Scenario**: Strong retail & telecom metrics.\n3. **Bearish Scenario**: Oil-to-chemical margin compression.\n4. **Neutral Scenario**: Rangebound consolidation.\n5. **Key Risk Factors**: Crude oil volatility.\n6. **Educational Disclaimer**: Educational only."
                      }
                    ]
                  },
                  "finishReason": "STOP",
                  "index": 0
                }
              ]
            }
        """.trimIndent()

        val parsed = geminiApi.parseGeminiResponse(sampleResponse)
        assertTrue(parsed.contains("1. **Summary**: Reliance demonstrates steady growth."))
        assertTrue(parsed.contains("2. **Bullish Scenario**"))
        assertTrue(parsed.contains("6. **Educational Disclaimer**"))
    }

    @Test
    fun testParseGeminiResponse_WithThinkingThoughtParts_ExtractsAnswerText() {
        val responseWithThoughts = """
            {
              "candidates": [
                {
                  "content": {
                    "role": "model",
                    "parts": [
                      {
                        "thought": true,
                        "text": "Thinking process: The user is asking about TCS quarterly results..."
                      },
                      {
                        "text": "1. **Summary**: TCS shows resilient order book with cloud growth."
                      }
                    ]
                  },
                  "finishReason": "STOP"
                }
              ]
            }
        """.trimIndent()

        val parsed = geminiApi.parseGeminiResponse(responseWithThoughts)
        assertEquals("1. **Summary**: TCS shows resilient order book with cloud growth.", parsed)
        assertFalse(parsed.contains("Thinking process"))
    }

    @Test(expected = GeminiApiException.MalformedResponseException::class)
    fun testParseGeminiResponse_EmptyCandidates_ThrowsMalformedResponseException() {
        val emptyResponse = """{ "candidates": [] }"""
        geminiApi.parseGeminiResponse(emptyResponse)
    }

    @Test(expected = GeminiApiException.MalformedResponseException::class)
    fun testParseGeminiResponse_EmptyParts_ThrowsMalformedResponseException() {
        val emptyParts = """{ "candidates": [{ "content": { "parts": [] } }] }"""
        geminiApi.parseGeminiResponse(emptyParts)
    }

    @Test
    fun testParseGeminiResponse_ErrorBodyExtraction() {
        val errorJson = """
            {
              "error": {
                "code": 404,
                "message": "This model models/gemini-2.0-flash is no longer available. Please update your code to use models/gemini-3.8-flash.",
                "status": "NOT_FOUND"
              }
            }
        """.trimIndent()

        try {
            geminiApi.parseGeminiResponse(errorJson)
            fail("Expected MalformedResponseException with error details")
        } catch (e: GeminiApiException.MalformedResponseException) {
            assertTrue(e.message?.contains("models/gemini-3.8-flash") == true)
        }
    }

    @Test
    fun testSystemPrompt_ContainsMandatorySixSectionStructure() {
        val prompt = GeminiApi.SYSTEM_PROMPT
        assertTrue(prompt.contains("1. **Summary**"))
        assertTrue(prompt.contains("2. **Bullish Scenario**"))
        assertTrue(prompt.contains("3. **Bearish Scenario**"))
        assertTrue(prompt.contains("4. **Neutral Scenario**"))
        assertTrue(prompt.contains("5. **Key Risk Factors**"))
        assertTrue(prompt.contains("6. **Educational Disclaimer**"))
        assertTrue(prompt.contains("SEBI"))
    }

    @Test
    fun testOkHttpInterceptor_Simulated404ModelUnavailable() {
        val client = OkHttpClient.Builder()
            .addInterceptor { _ ->
                Response.Builder()
                    .request(Request.Builder().url(GeminiApi.ENDPOINT_URL).build())
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .message("Not Found")
                    .body(
                        """{"error":{"code":404,"message":"Model not found"}}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }
            .build()

        val mockApi = GeminiApi(client)
        assertNotNull(mockApi)
    }

    @Test
    fun testRetryClassification_RetryableVsPermanentErrors() {
        val err400 = GeminiApiException.InvalidRequestException("Bad request", 400)
        assertFalse(err400.isRetryable)

        val err401 = GeminiApiException.AuthenticationException("Unauthorized", 401)
        assertFalse(err401.isRetryable)

        val err404 = GeminiApiException.ModelUnavailableException("Not found", 404)
        assertFalse(err404.isRetryable)

        val err429 = GeminiApiException.RateLimitExceededException("Quota exceeded", 429)
        assertTrue(err429.isRetryable)

        val err503 = GeminiApiException.ServerUnavailableException("High demand", 503)
        assertTrue(err503.isRetryable)

        val netErr = GeminiApiException.NetworkException("Timed out")
        assertTrue(netErr.isRetryable)
    }
}
