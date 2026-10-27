package com.smartinvestor.app.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.smartinvestor.app.ui.components.PremiumButton
import com.smartinvestor.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialToolsScreen(onBack: () -> Unit) {
    var monthlyInvestment by remember { mutableStateOf("5000") }
    var expectedReturn by remember { mutableStateOf("12") }
    var years by remember { mutableStateOf("10") }

    val monthly = monthlyInvestment.toDoubleOrNull() ?: 0.0
    val rate = (expectedReturn.toDoubleOrNull() ?: 0.0) / 100 / 12
    val months = (years.toIntOrNull() ?: 0) * 12
    val futureValue = if (rate > 0) monthly * ((Math.pow(1 + rate, months.toDouble()) - 1) / rate) * (1 + rate) else monthly * months
    val totalInvested = monthly * months
    val returns = futureValue - totalInvested

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Financial Tools", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("SIP Calculator", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = monthlyInvestment, onValueChange = { monthlyInvestment = it }, label = { Text("Monthly Investment (₹)") }, leadingIcon = { Icon(Icons.Rounded.CurrencyRupee, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = expectedReturn, onValueChange = { expectedReturn = it }, label = { Text("Expected Return (% p.a.)") }, leadingIcon = { Icon(Icons.Rounded.Percent, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = years, onValueChange = { years = it }, label = { Text("Time Period (Years)") }, leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                }
            }

            // Results
            GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = SuccessGreen) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultRow("Invested Amount", "₹${String.format("%,.0f", totalInvested)}", MaterialTheme.colorScheme.onSurface)
                    ResultRow("Est. Returns", "₹${String.format("%,.0f", returns)}", SuccessGreen)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    ResultRow("Total Value", "₹${String.format("%,.0f", futureValue)}", MaterialTheme.colorScheme.onSurface, bold = true)
                }
            }

            // Other tools
            Text("More Tools", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Description, null, tint = PremiumPurple, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tax Report", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Quiz, null, tint = PremiumPurple, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Risk Quiz", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
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
