package com.smartinvestor.app.ui.screens.kyc

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinvestor.app.ui.components.*
import com.smartinvestor.app.ui.theme.*

// ══════════════════════════════════════════════════════════
// Smart Investor — KYC Stepper
// Per gemini.md §1.3: PAN, Aadhaar, Bank & Demat linking
// Per brandguidelines.md §4: "Glowing Shield" 3D icon
// ══════════════════════════════════════════════════════════

@Composable
fun KycStepperScreen(onKycComplete: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf("PAN Card", "Aadhaar", "Bank Account", "Demat Account")
    var panNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var bankAccount by remember { mutableStateOf("") }
    var bankIfsc by remember { mutableStateOf("") }
    var dematId by remember { mutableStateOf("") }
    val completedSteps = remember { mutableStateListOf<Int>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header with Glowing Shield
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛡️", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "KYC Verification",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "Step ${currentStep + 1} of ${steps.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                steps.forEachIndexed { index, step ->
                    KycStepIndicator(
                        stepNumber = index + 1,
                        label = step,
                        isActive = index == currentStep,
                        isCompleted = index in completedSteps,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Step Content
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                showGlow = true,
                glowColor = if (currentStep in completedSteps) SuccessGreen else PremiumPurple,
            ) {
                when (currentStep) {
                    0 -> KycPanStep(panNumber, onValueChange = { panNumber = it })
                    1 -> KycAadhaarStep(aadhaarNumber, onValueChange = { aadhaarNumber = it })
                    2 -> KycBankStep(bankAccount, bankIfsc, onAccountChange = { bankAccount = it }, onIfscChange = { bankIfsc = it })
                    3 -> KycDematStep(dematId, onValueChange = { dematId = it })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 0) {
                    PremiumOutlineButton(
                        text = "Back",
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.ArrowBack,
                    )
                }
                PremiumButton(
                    text = if (currentStep == steps.lastIndex) "Complete KYC" else "Verify & Continue",
                    onClick = {
                        completedSteps.add(currentStep)
                        if (currentStep < steps.lastIndex) currentStep++
                        else onKycComplete()
                    },
                    modifier = Modifier.weight(1f),
                    icon = if (currentStep == steps.lastIndex) Icons.Rounded.CheckCircle else Icons.Rounded.ArrowForward,
                )
            }
        }
    }
}

@Composable
private fun KycStepIndicator(stepNumber: Int, label: String, isActive: Boolean, isCompleted: Boolean) {
    val scale by animateFloatAsState(if (isActive) 1.1f else 1f, label = "stepScale")
    val color = when {
        isCompleted -> SuccessGreen
        isActive -> PremiumPurple
        else -> DarkOnSurfaceVariant
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, color),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) Icon(Icons.Rounded.Check, null, tint = color, modifier = Modifier.size(18.dp))
                else Text("$stepNumber", color = color, style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun KycPanStep(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PAN Card Verification", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Enter your 10-digit PAN number", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("PAN Number") }, leadingIcon = { Icon(Icons.Rounded.CreditCard, null) }, placeholder = { Text("ABCDE1234F") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
    }
}

@Composable
private fun KycAadhaarStep(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Aadhaar Verification", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Enter your 12-digit Aadhaar number", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("Aadhaar Number") }, leadingIcon = { Icon(Icons.Rounded.Badge, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
    }
}

@Composable
private fun KycBankStep(account: String, ifsc: String, onAccountChange: (String) -> Unit, onIfscChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Bank Account Linking", style = MaterialTheme.typography.titleMedium, color = Color.White)
        OutlinedTextField(value = account, onValueChange = onAccountChange, label = { Text("Account Number") }, leadingIcon = { Icon(Icons.Rounded.AccountBalance, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
        OutlinedTextField(value = ifsc, onValueChange = onIfscChange, label = { Text("IFSC Code") }, leadingIcon = { Icon(Icons.Rounded.Code, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
    }
}

@Composable
private fun KycDematStep(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Demat Account Linking", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Enter your Demat account ID", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("Demat ID") }, leadingIcon = { Icon(Icons.Rounded.AccountBox, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
    }
}
