package com.smartinvestor.app.ui.screens.ai_advisor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.remote.GeminiApi
import com.smartinvestor.app.data.remote.GeminiApiException
import com.smartinvestor.app.data.repository.PortfolioRepository
import com.smartinvestor.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val canRetry: Boolean = false,
    val failedPrompt: String? = null
)

@HiltViewModel
class AiAdvisorViewModel @Inject constructor(
    private val geminiApi: GeminiApi,
    private val userRepository: UserRepository,
    private val portfolioRepository: PortfolioRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "AiAdvisorVM"
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "👋 Welcome to Smart Investor AI Copilot! Powered by **Gemini 3.8 Flash**.\n\n" +
                        "I can analyze Indian equities, break down Bull/Bear/Neutral scenarios, explain financial concepts, and evaluate portfolio diversification. All insights are educational and SEBI-aligned.\n\n" +
                        "How can I assist your investment journey today?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    val suggestionChips = listOf(
        "Analyze RELIANCE with Bull/Bear cases",
        "Explain P/E ratio in plain English",
        "How to diversify a ₹1,00,000 portfolio?",
        "SIP vs Lumpsum in volatile markets"
    )

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(content = text, isUser = true)
        _messages.value = _messages.value + userMsg
        _isThinking.value = true

        viewModelScope.launch {
            try {
                // Fetch context: risk profile and virtual portfolio size
                val riskProfileStr = try {
                    userRepository.getUserProfile().getOrNull()?.riskProfile?.name ?: "MODERATE"
                } catch (e: Exception) {
                    "MODERATE"
                }

                val virtualCash = try {
                    portfolioRepository.virtualCash.first()
                } catch (e: Exception) {
                    1000000.0
                }

                val contextualPrompt = """
                    [Context]
                    - Investor Risk Tolerance: $riskProfileStr
                    - Available Virtual Demo Capital: ₹${String.format("%,.0f", virtualCash)}
                    - Regulatory Compliance: SEBI (India) educational disclaimer required.
                    
                    [User Query]
                    $text
                """.trimIndent()

                Log.d(TAG, "Querying Gemini 3.8 Flash...")
                val response = geminiApi.sendMessage(contextualPrompt)
                _messages.value = _messages.value + ChatMessage(content = response, isUser = false)
            } catch (e: Exception) {
                Log.e(TAG, "AI Advisor error: ${e.message}", e)
                val errorMessage = when (e) {
                    is GeminiApiException.AuthenticationException ->
                        "⚠️ API Authentication Error: ${e.message ?: "Please check your GEMINI_API_KEY in local.properties."}"
                    is GeminiApiException.ModelUnavailableException ->
                        "⚠️ Model Unavailable (404): ${e.message ?: "The requested Gemini model endpoint is currently unreachable. Tap 'Retry' to attempt fallback endpoints."}"
                    is GeminiApiException.RateLimitExceededException ->
                        "⏳ Rate Limit Exceeded (429): Quota exhausted or rate limited. Please wait a moment before sending another prompt."
                    is GeminiApiException.ServerUnavailableException ->
                        "⚡ High Demand (503): Gemini AI is currently experiencing high global demand. Spikes are temporary. Tap 'Retry' to try again."
                    is GeminiApiException.NetworkException ->
                        "📡 Network Error: ${e.message ?: "Unable to connect. Please check your internet connection."}"
                    is GeminiApiException.InvalidRequestException ->
                        "⚠️ Invalid Request (400): ${e.message ?: "Please rephrase your query."}"
                    is GeminiApiException.MalformedResponseException ->
                        "⚠️ Response Parsing Error: ${e.message ?: "Failed to read AI response. Please try again."}"
                    else ->
                        "⚠️ Sorry, I encountered an unexpected error: ${e.message ?: "Please try again."}"
                }

                val canRetry = ((e as? GeminiApiException)?.isRetryable ?: false) || e is GeminiApiException.ModelUnavailableException
                _messages.value = _messages.value + ChatMessage(
                    content = errorMessage,
                    isUser = false,
                    isError = true,
                    canRetry = canRetry,
                    failedPrompt = text
                )
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun retryLastMessage(failedPrompt: String) {
        // Remove the error message from history
        _messages.value = _messages.value.filterNot { it.isError && it.failedPrompt == failedPrompt }
        // Re-execute sending the prompt
        sendMessage(failedPrompt)
    }

    fun resetChat() {
        geminiApi.resetChat()
        _messages.value = listOf(
            ChatMessage("Chat session reset! What investment or stock concept would you like to explore?", isUser = false)
        )
    }
}
