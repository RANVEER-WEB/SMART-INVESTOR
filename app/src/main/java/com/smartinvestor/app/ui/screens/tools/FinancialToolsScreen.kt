package com.smartinvestor.app.ui.screens.tools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialToolsScreen(
    onBack: () -> Unit,
    onNavigateToRiskQuiz: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // ── SIP State ──
    var monthlyInvestment by remember { mutableStateOf("10000") }
    var expectedReturn by remember { mutableStateOf("12") }
    var years by remember { mutableStateOf("15") }

    val monthly = monthlyInvestment.toDoubleOrNull() ?: 0.0
    val rate = (expectedReturn.toDoubleOrNull() ?: 0.0) / 100.0 / 12.0
    val months = (years.toIntOrNull() ?: 0) * 12
    val sipFutureValue = if (rate > 0 && months > 0) {
        monthly * (((1.0 + rate).pow(months.toDouble()) - 1.0) / rate) * (1.0 + rate)
    } else monthly * months
    val sipInvested = monthly * months
    val sipGains = (sipFutureValue - sipInvested).coerceAtLeast(0.0)

    // ── Goal Planner State ──
    var targetCorpus by remember { mutableStateOf("10000000") } // ₹1 Crore
    var goalYears by remember { mutableStateOf("15") }
    var goalReturn by remember { mutableStateOf("12") }

    val targetAmt = targetCorpus.toDoubleOrNull() ?: 0.0
    val goalM = (goalYears.toIntOrNull() ?: 0) * 12
    val goalR = (goalReturn.toDoubleOrNull() ?: 0.0) / 100.0 / 12.0
    val requiredMonthlySip = if (goalM > 0) {
        if (goalR > 0) {
            targetAmt / ((((1.0 + goalR).pow(goalM.toDouble()) - 1.0) / goalR) * (1.0 + goalR))
        } else {
            targetAmt / goalM
        }
    } else 0.0

    // ── Tax Estimator State (Indian Equities FY24-25) ──
    var shortTermGain by remember { mutableStateOf("50000") }
    var longTermGain by remember { mutableStateOf("250000") }

    val stcg = shortTermGain.toDoubleOrNull() ?: 0.0
    val ltcg = longTermGain.toDoubleOrNull() ?: 0.0
    val stcgTax = stcg * 0.20 // 20% STCG per Union Budget 2024
    val taxableLtcg = (ltcg - 125000.0).coerceAtLeast(0.0) // ₹1.25L exemption
    val ltcgTax = taxableLtcg * 0.125 // 12.5% LTCG
    val baseTax = stcgTax + ltcgTax
    val cess = baseTax * 0.04 // 4% Health & Education Cess
    val totalEstimatedTax = baseTax + cess

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Tools & Calculators", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("SIP Calc") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Goal Planner") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tax Estimator") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // ── SIP Calculator ──
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Systematic Investment Plan (SIP)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                OutlinedTextField(
                                    value = monthlyInvestment,
                                    onValueChange = { monthlyInvestment = it },
                                    label = { Text("Monthly Investment (₹)") },
                                    leadingIcon = { Icon(Icons.Rounded.CurrencyRupee, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = expectedReturn,
                                    onValueChange = { expectedReturn = it },
                                    label = { Text("Expected Return Rate (% p.a.)") },
                                    leadingIcon = { Icon(Icons.Rounded.Percent, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = years,
                                    onValueChange = { years = it },
                                    label = { Text("Time Horizon (Years)") },
                                    leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = SuccessGreen) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ResultRow("Total Amount Invested", "₹${String.format("%,.0f", sipInvested)}", MaterialTheme.colorScheme.onSurface)
                                ResultRow("Estimated Wealth Gain", "₹${String.format("%,.0f", sipGains)}", SuccessGreen)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ResultRow("Total Projected Maturity", "₹${String.format("%,.0f", sipFutureValue)}", MaterialTheme.colorScheme.onSurface, bold = true)
                            }
                        }
                    }

                    1 -> {
                        // ── Goal Planner ──
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Target Corpus Goal Planner", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                OutlinedTextField(
                                    value = targetCorpus,
                                    onValueChange = { targetCorpus = it },
                                    label = { Text("Target Corpus (₹)") },
                                    leadingIcon = { Icon(Icons.Rounded.Flag, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = goalYears,
                                    onValueChange = { goalYears = it },
                                    label = { Text("Years to Goal") },
                                    leadingIcon = { Icon(Icons.Rounded.Timelapse, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = goalReturn,
                                    onValueChange = { goalReturn = it },
                                    label = { Text("Assumed Return (% p.a.)") },
                                    leadingIcon = { Icon(Icons.Rounded.Percent, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = PremiumPurple) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ResultRow("Target Corpus", "₹${String.format("%,.0f", targetAmt)}", MaterialTheme.colorScheme.onSurface)
                                ResultRow("Timeframe", "$goalYears Years (${goalM} months)", MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ResultRow("Required Monthly SIP", "₹${String.format("%,.0f", requiredMonthlySip)}/mo", PremiumPurpleLight, bold = true)
                            }
                        }
                    }

                    2 -> {
                        // ── Tax Estimator ──
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Equity Capital Gains Tax (FY 2024-25)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("STCG taxed @ 20% · LTCG taxed @ 12.5% with ₹1.25 Lakh annual exemption.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(
                                    value = shortTermGain,
                                    onValueChange = { shortTermGain = it },
                                    label = { Text("Short Term Capital Gains (< 1 yr) ₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = longTermGain,
                                    onValueChange = { longTermGain = it },
                                    label = { Text("Long Term Capital Gains (> 1 yr) ₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = DangerRed) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ResultRow("STCG Tax @ 20%", "₹${String.format("%,.0f", stcgTax)}", MaterialTheme.colorScheme.onSurface)
                                ResultRow("LTCG Tax @ 12.5% (after ₹1.25L relief)", "₹${String.format("%,.0f", ltcgTax)}", MaterialTheme.colorScheme.onSurface)
                                ResultRow("Health & Education Cess (4%)", "₹${String.format("%,.0f", cess)}", MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ResultRow("Total Tax Payable", "₹${String.format("%,.0f", totalEstimatedTax)}", DangerRed, bold = true)
                            }
                        }
                    }
                }

                // ── Direct Link to Risk Suitability ──
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRiskQuiz() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.Quiz, null, tint = PremiumPurple, modifier = Modifier.size(32.dp))
                            Column {
                                Text("Take Risk Suitability Quiz", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Calibrate your financial comfort level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = PremiumPurple)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = if (bold) FinancialSubBalanceStyle else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = color)
    }
}
