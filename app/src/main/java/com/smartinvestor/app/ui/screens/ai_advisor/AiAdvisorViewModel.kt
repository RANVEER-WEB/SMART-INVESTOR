package com.smartinvestor.app.ui.screens.ai_advisor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.remote.GeminiApi
import com.smartinvestor.app.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val userRepository: com.smartinvestor.app.data.repository.UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AiAdvisorVM"
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I'm your Smart Investor AI advisor. Ask me anything about stocks, investing, or market trends. 📈", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(content = text, isUser = true)
        _messages.value = _messages.value + userMsg
        _isThinking.value = true

        viewModelScope.launch {
            try {
                // Fetch user profile (non-fatal if it fails)
                val riskProfileStr = try {
                    val user = userRepository.getUserProfile().getOrNull()
                    user?.riskProfile?.name ?: "MODERATE"
                } catch (profileEx: Exception) {
                    Log.w(TAG, "Failed to load user profile, using default risk profile", profileEx)
                    "MODERATE"
                }

                val contextualPrompt = "User Risk Profile: $riskProfileStr\n\nUser Question: $text"
                Log.d(TAG, "Sending message to Gemini API...")

                val response = geminiApi.sendMessage(contextualPrompt)
                Log.d(TAG, "Gemini response received (${response.length} chars)")
                _messages.value = _messages.value + ChatMessage(content = response, isUser = false)
            } catch (e: Exception) {
                Log.e(TAG, "AI Advisor error: ${e.message}", e)
                val errorDetail = when {
                    e.message?.contains("401") == true || e.message?.contains("403") == true ->
                        "API key may be invalid or expired. Please check your Gemini API key."
                    e.message?.contains("429") == true ->
                        "Too many requests. Please wait a moment and try again."
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("timeout") == true ->
                        "Network error. Please check your internet connection."
                    else ->
                        "Sorry, I encountered an error. Please try again."
                }
                _messages.value = _messages.value + ChatMessage(content = errorDetail, isUser = false)
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun resetChat() {
        geminiApi.resetChat()
        _messages.value = listOf(ChatMessage("Chat reset! How can I help you today?", isUser = false))
    }
}
