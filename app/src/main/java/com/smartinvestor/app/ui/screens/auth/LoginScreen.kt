package com.smartinvestor.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.domain.model.UiState
import com.smartinvestor.app.ui.components.*
import com.smartinvestor.app.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val resetState by viewModel.resetPasswordState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Icon(
                imageVector = Icons.Rounded.Diamond,
                contentDescription = "Logo",
                tint = PremiumPurpleLight,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "The Vault",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
            )
            Text(
                text = "Access your investment command center",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkOnSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Form
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Rounded.VisibilityOff
                                    else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )

                    // Error display
                    if (loginState is UiState.Error) {
                        ErrorCard(message = (loginState as UiState.Error).message)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            resetEmailInput = email
                            showResetDialog = true
                        }) {
                            Text(
                                "Forgot Password?",
                                style = MaterialTheme.typography.labelMedium,
                                color = PremiumPurpleLight
                            )
                        }
                    }

                    PremiumButton(
                        text = "Sign In",
                        onClick = { viewModel.signIn(email, password) },
                        isLoading = loginState is UiState.Loading,
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        icon = Icons.Rounded.Login,
                    )

                    PremiumOutlineButton(
                        text = "Sign In with Biometrics",
                        onClick = { /* Biometric prompt */ },
                        icon = Icons.Rounded.Fingerprint,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurfaceVariant,
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PremiumPurple,
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
                viewModel.clearResetPasswordState()
            },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your account email to receive password reset instructions via Supabase Auth.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    when (val st = resetState) {
                        is UiState.Success -> {
                            Text(st.data, color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
                        }
                        is UiState.Error -> {
                            Text(st.message, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetPassword(resetEmailInput) },
                    enabled = resetEmailInput.isNotBlank() && resetState !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                ) {
                    if (resetState is UiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Reset Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.clearResetPasswordState()
                }) {
                    Text("Close")
                }
            }
        )
    }
}
