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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartinvestor.app.domain.model.KycData
import com.smartinvestor.app.ui.components.*
import com.smartinvestor.app.ui.theme.*

// ══════════════════════════════════════════════════════════
// Smart Investor — KYC Stepper with Indian Regulatory Validation
// PAN: [A-Z]{5}[0-9]{4}[A-Z]
// Aadhaar: 12-digit UIDAI format
// Bank: 9-18 digits + RBI IFSC code
// Demat: 16-digit CDSL/NSDL BOID
// ══════════════════════════════════════════════════════════

object KycValidator {
    val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    val aadhaarRegex = Regex("^[2-9]{1}[0-9]{11}$")
    val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
    val accountRegex = Regex("^[0-9]{9,18}$")
    val dematRegex = Regex("^[0-9]{16}$")

    fun maskAadhaar(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return if (digits.length == 12) {
            "•••• •••• " + digits.takeLast(4)
        } else raw
    }
}

@Composable
fun KycStepperScreen(
    onKycComplete: () -> Unit,
    viewModel: KycViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf("PAN Card", "Aadhaar", "Bank Account", "Demat Account")

    var panNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var bankAccount by remember { mutableStateOf("") }
    var bankIfsc by remember { mutableStateOf("") }
    var dematId by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }
    val completedSteps = remember { mutableStateListOf<Int>() }

    fun validateCurrentStep(): Boolean {
        validationError = null
        return when (currentStep) {
            0 -> {
                val cleaned = panNumber.trim().uppercase()
                if (!KycValidator.panRegex.matches(cleaned)) {
                    validationError = "Invalid PAN format. Must be 10 characters (e.g. ABCDE1234F)."
                    false
                } else true
            }
            1 -> {
                val cleaned = aadhaarNumber.trim().replace(" ", "")
                if (!KycValidator.aadhaarRegex.matches(cleaned)) {
                    validationError = "Invalid Aadhaar. Must be 12 digits not starting with 0 or 1."
                    false
                } else true
            }
            2 -> {
                val cleanedAcc = bankAccount.trim()
                val cleanedIfsc = bankIfsc.trim().uppercase()
                if (!KycValidator.accountRegex.matches(cleanedAcc)) {
                    validationError = "Invalid Bank Account Number (9 to 18 digits required)."
                    false
                } else if (!KycValidator.ifscRegex.matches(cleanedIfsc)) {
                    validationError = "Invalid IFSC Code. Must be 11 characters (e.g. HDFC0001234)."
                    false
                } else true
            }
            3 -> {
                val cleaned = dematId.trim()
                if (!KycValidator.dematRegex.matches(cleaned)) {
                    validationError = "Invalid Demat ID. Must be a 16-digit CDSL/NSDL BOID."
                    false
                } else true
            }
            else -> true
        }
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
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header with Glowing Shield Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = PremiumPurple.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, PremiumPurpleLight),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.GppGood, null, tint = PremiumPurpleLight, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "KYC Verification",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "Step ${currentStep + 1} of ${steps.size} · SEBI Identity Protocol",
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

            Spacer(modifier = Modifier.height(28.dp))

            // Error alert if validation fails
            if (validationError != null) {
                ErrorCard(message = validationError!!)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Step Content
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                showGlow = true,
                glowColor = if (currentStep in completedSteps) SuccessGreen else PremiumPurple,
            ) {
                when (currentStep) {
                    0 -> KycPanStep(panNumber, onValueChange = { panNumber = it; validationError = null })
                    1 -> KycAadhaarStep(aadhaarNumber, onValueChange = { aadhaarNumber = it; validationError = null })
                    2 -> KycBankStep(bankAccount, bankIfsc, onAccountChange = { bankAccount = it; validationError = null }, onIfscChange = { bankIfsc = it; validationError = null })
                    3 -> KycDematStep(dematId, onValueChange = { dematId = it; validationError = null })
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
                        onClick = {
                            validationError = null
                            currentStep--
                        },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.ArrowBack,
                    )
                }
                PremiumButton(
                    text = if (currentStep == steps.lastIndex) "Complete KYC" else "Verify & Continue",
                    onClick = {
                        if (validateCurrentStep()) {
                            if (!completedSteps.contains(currentStep)) {
                                completedSteps.add(currentStep)
                            }
                            if (currentStep < steps.lastIndex) {
                                currentStep++
                            } else {
                                viewModel.completeKyc(
                                    pan = panNumber,
                                    aadhaar = aadhaarNumber,
                                    bankAccount = bankAccount,
                                    ifsc = bankIfsc,
                                    demat = dematId,
                                    onSuccess = onKycComplete
                                )
                            }
                        }
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
        Text("Permanent Account Number (PAN)", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Mandatory for Indian stock market trading under SEBI regulations.", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("PAN Number") },
            leadingIcon = { Icon(Icons.Rounded.CreditCard, null) },
            placeholder = { Text("e.g. ABCDE1234F") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
private fun KycAadhaarStep(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Aadhaar Identity Verification", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Enter your 12-digit UIDAI Aadhaar number for instant e-KYC matching.", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Aadhaar Number (12 Digits)") },
            leadingIcon = { Icon(Icons.Rounded.Badge, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
private fun KycBankStep(account: String, ifsc: String, onAccountChange: (String) -> Unit, onIfscChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Bank Account Linking", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Used for fast deposits and paper funds simulation ledger.", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(
            value = account,
            onValueChange = onAccountChange,
            label = { Text("Bank Account Number") },
            leadingIcon = { Icon(Icons.Rounded.AccountBalance, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = ifsc,
            onValueChange = onIfscChange,
            label = { Text("Bank IFSC Code (11 Chars)") },
            leadingIcon = { Icon(Icons.Rounded.Code, null) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            placeholder = { Text("e.g. SBIN0001234") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
private fun KycDematStep(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Demat Account Linking", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Enter your 16-digit Beneficiary Owner ID (BOID) with CDSL or NSDL.", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("16-digit Demat BOID") },
            leadingIcon = { Icon(Icons.Rounded.AccountBox, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("e.g. 1208160012345678") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }
}
