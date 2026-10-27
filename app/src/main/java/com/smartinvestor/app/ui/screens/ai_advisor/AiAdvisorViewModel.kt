package com.smartinvestor.app.ui.screens.ai_advisor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.remote.GeminiApi
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
    val timestamp: Long = System.currentTimeMillis()
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
                content = "👋 Welcome to Smart Investor AI Copilot! I am your AI financial tutor and market analyst.\n\n" +
                        "I can analyze stocks, break down bull/bear scenarios, explain financial statements, and guide your wealth strategy. All insights are educational and SEBI-aligned.\n\n" +
                        "How can I assist your investment journey today?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    val suggestionChips = listOf(
        "Analyze AAPL with Bull/Bear cases",
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
                    
                    [Response Structure Instructions]
                    1. Concise Explanation or Analysis
                    2. Bullish, Bearish, and Neutral considerations (if evaluating an asset or sector)
                    3. Key Risk Factors & Assumptions
                    4. Brief educational takeaway (Remind that past performance doesn't guarantee future returns)
                """.trimIndent()

                Log.d(TAG, "Querying Gemini API...")
                val response = geminiApi.sendMessage(contextualPrompt)
                _messages.value = _messages.value + ChatMessage(content = response, isUser = false)
            } catch (e: Exception) {
                Log.e(TAG, "AI Advisor error: ${e.message}", e)
                val errorDetail = when {
                    e.message?.contains("401") == true || e.message?.contains("403") == true ->
                        "⚠️ API key may be invalid or expired. Please check your Gemini API key in local.properties."
                    e.message?.contains("429") == true ->
                        "⏳ Gemini rate limit reached. Please wait a moment before sending another prompt."
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("timeout") == true ->
                        "📡 Network error. Please check your internet connection."
                    else ->
                        "⚠️ Sorry, I encountered an error processing your inquiry. Please try again."
                }
                _messages.value = _messages.value + ChatMessage(content = errorDetail, isUser = false)
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun resetChat() {
        geminiApi.resetChat()
        _messages.value = listOf(
            ChatMessage("Chat session reset! What investment or stock concept would you like to explore?", isUser = false)
        )
    }
}
