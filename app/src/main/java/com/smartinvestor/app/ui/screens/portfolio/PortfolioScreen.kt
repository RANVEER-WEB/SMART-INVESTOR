package com.smartinvestor.app.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.domain.model.PortfolioHolding
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@Composable
fun PortfolioScreen() {
    // Sample data — will be replaced with Supabase data
    val holdings = remember {
        listOf(
            PortfolioHolding("AAPL", "Apple Inc.", 10, 145.0, 178.50),
            PortfolioHolding("GOOGL", "Alphabet Inc.", 5, 2800.0, 2950.0),
            PortfolioHolding("TSLA", "Tesla Inc.", 8, 220.0, 245.75),
            PortfolioHolding("MSFT", "Microsoft Corp.", 15, 310.0, 378.90),
            PortfolioHolding("NVDA", "NVIDIA Corp.", 12, 450.0, 875.30),
        )
    }
    val totalInvested = holdings.sumOf { it.investedValue }
    val totalCurrent = holdings.sumOf { it.currentValue }
    val totalPL = totalCurrent - totalInvested

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Portfolio", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = if (totalPL >= 0) SuccessGreen else DangerRed) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Current Value", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format("%,.2f", totalCurrent)}", style = FinancialBalanceStyle, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("P&L", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(if (totalPL >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown, null, tint = if (totalPL >= 0) SuccessGreen else DangerRed, modifier = Modifier.size(18.dp))
                            Text("₹${String.format("%,.2f", totalPL)}", style = FinancialSubBalanceStyle, color = if (totalPL >= 0) SuccessGreen else DangerRed)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Holdings (${holdings.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        items(holdings) { holding -> HoldingCard(holding) }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun HoldingCard(holding: PortfolioHolding) {
    val isPositive = holding.profitLoss >= 0
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(holding.symbol, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(holding.companyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${holding.quantity} shares · Avg ₹${String.format("%.2f", holding.avgBuyPrice)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${String.format("%,.2f", holding.currentValue)}", style = StockPriceStyle, color = MaterialTheme.colorScheme.onSurface)
                Text("${if (isPositive) "+" else ""}₹${String.format("%.2f", holding.profitLoss)} (${String.format("%+.1f", holding.profitLossPercent)}%)", style = StockChangeStyle, color = if (isPositive) SuccessGreen else DangerRed)
            }
        }
    }
}
