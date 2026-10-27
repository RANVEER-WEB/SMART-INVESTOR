package com.smartinvestor.app.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.data.local.entities.OrderEntity
import com.smartinvestor.app.domain.model.PortfolioHolding
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@Composable
fun PortfolioScreen(
    onNavigateToStock: (String) -> Unit = {},
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val uiData by viewModel.uiData.collectAsStateWithLifecycle()
    val isPositive = uiData.totalProfitLoss >= 0

    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Portfolio Command Center",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ── Portfolio Net Worth Hero Card ──
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                showGlow = true,
                glowColor = if (isPositive) SuccessGreen else DangerRed
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Total Portfolio Net Worth",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format("%,.2f", uiData.totalPortfolioNetWorth)}",
                        style = FinancialBalanceStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.2f", uiData.currentValue)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Invested Capital", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.2f", uiData.investedValue)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total P&L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isPositive) SuccessGreen else DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${if (isPositive) "+" else ""}₹${String.format("%,.2f", uiData.totalProfitLoss)} (${String.format("%+.2f", uiData.profitLossPercent)}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPositive) SuccessGreen else DangerRed
                                )
                            }
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = PremiumPurple.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.AccountBalanceWallet, null, tint = PremiumPurple, modifier = Modifier.size(16.dp))
                                Text("Available Virtual Cash", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("₹${String.format("%,.2f", uiData.virtualCash)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PremiumPurpleLight)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Section Tabs: Holdings vs Orders ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Holdings (${uiData.holdings.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Order History (${uiData.recentOrders.size})") }
                )
            }
        }

        if (selectedTab == 0) {
            if (uiData.holdings.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Inventory2, null, tint = PremiumPurple, modifier = Modifier.size(40.dp))
                            Text("No Active Holdings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Execute paper trades in Stock Details to build your portfolio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(uiData.holdings, key = { it.symbol }) { holding ->
                    HoldingCard(holding = holding, onClick = { onNavigateToStock(holding.symbol) })
                }
            }
        } else {
            if (uiData.recentOrders.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.ReceiptLong, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Text("No Executed Orders", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Your simulated paper trade logs will appear here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(uiData.recentOrders, key = { it.id }) { order ->
                    OrderHistoryCard(order = order)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun HoldingCard(holding: PortfolioHolding, onClick: () -> Unit) {
    val isPositive = holding.profitLoss >= 0
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(holding.symbol, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(holding.companyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${holding.quantity} shares · Avg ₹${String.format("%.2f", holding.avgBuyPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${String.format("%,.2f", holding.currentValue)}", style = StockPriceStyle, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        imageVector = if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) SuccessGreen else DangerRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${if (isPositive) "+" else ""}₹${String.format("%.2f", holding.profitLoss)} (${String.format("%+.1f", holding.profitLossPercent)}%)",
                        style = StockChangeStyle,
                        color = if (isPositive) SuccessGreen else DangerRed
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: OrderEntity) {
    val isBuy = order.type == "BUY"
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = (if (isBuy) SuccessGreen else DangerRed).copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBuy) "B" else "S",
                            fontWeight = FontWeight.Bold,
                            color = if (isBuy) SuccessGreen else DangerRed
                        )
                    }
                }
                Column {
                    Text(order.symbol, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("${order.quantity} shares @ ₹${String.format("%.2f", order.executionPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%,.2f", order.quantity * order.executionPrice)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "EXECUTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen
                )
            }
        }
    }
}
