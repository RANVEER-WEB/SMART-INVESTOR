package com.smartinvestor.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.repository.AuthRepository
import com.smartinvestor.app.domain.model.UiState
import com.smartinvestor.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<String>> = _resetPasswordState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = authRepository.signIn(email, password)
            _loginState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun signUp(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            val result = authRepository.signUp(email, password, fullName)
            _registerState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun isAuthenticated(): Boolean = authRepository.isAuthenticated()

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _loginState.value = UiState.Idle
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = UiState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            _resetPasswordState.value = result.fold(
                onSuccess = { UiState.Success("Password reset instructions sent to $email") },
                onFailure = { UiState.Error(it.message ?: "Failed to send reset email") }
            )
        }
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = UiState.Idle
    }
}
