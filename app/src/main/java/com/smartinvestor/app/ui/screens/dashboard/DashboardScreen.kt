package com.smartinvestor.app.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.domain.model.UiState
import com.smartinvestor.app.domain.model.WatchlistItem
import com.smartinvestor.app.ui.components.*
import com.smartinvestor.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAiAdvisor: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToStock: (String) -> Unit = {},
    onNavigateToTools: () -> Unit = {},
    onNavigateToRiskQuiz: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val stocksState by viewModel.stocksState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()
    val chartState by viewModel.chartState.collectAsStateWithLifecycle()
    val chartTimeframe by viewModel.chartTimeframe.collectAsStateWithLifecycle()

    val portfolioNetWorth by viewModel.portfolioNetWorth.collectAsStateWithLifecycle()
    val (portfolioPL, portfolioPLPercent) = viewModel.portfolioProfitLoss.collectAsStateWithLifecycle().value

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    val isPositivePL = portfolioPL >= 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Smart Investor",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showSearchDialog = true }) {
                    Icon(Icons.Rounded.Search, "Search Stocks", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(Icons.Rounded.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Signature AI Orb button
                IconButton(onClick = onNavigateToAiAdvisor) {
                    Icon(Icons.Rounded.AutoAwesome, "AI Advisor", tint = PremiumPurpleLight)
                }
            }
        }

        // ── Offline Protocol Alert ──
        if (isOffline) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = DangerRedContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.WifiOff, null, tint = DangerRed, modifier = Modifier.size(16.dp))
                    Text("Offline · Showing cached data", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.loadDashboard() }) {
                        Text("Retry Sync", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Indian Market Indices Strip ──
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.marketIndices) { idx ->
                IndexChip(index = idx)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Portfolio Value Hero Card (Linked to real portfolio data) ──
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            showGlow = true,
            glowColor = if (isPositivePL) SuccessGreen else DangerRed
        ) {
            Text(
                text = "Total Portfolio Net Worth (Paper Trading)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${String.format("%,.2f", portfolioNetWorth)}",
                style = FinancialBalanceStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = if (isPositivePL) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositivePL) SuccessGreen else DangerRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${if (isPositivePL) "+" else ""}₹${String.format("%,.2f", portfolioPL)} (${String.format("%+.2f", portfolioPLPercent)}%)",
                    style = StockChangeStyle,
                    color = if (isPositivePL) SuccessGreen else DangerRed
                )
                Text("overall P&L", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Interactive Chart Hero Feature ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interactive Chart · $selectedSymbol",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { onNavigateToStock(selectedSymbol) }) {
                Text("Full Details", color = PremiumPurpleLight, style = MaterialTheme.typography.labelSmall)
                Icon(Icons.Rounded.ChevronRight, null, tint = PremiumPurpleLight, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            when (val state = chartState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumPurple)
                    }
                }
                is UiState.Success -> {
                    val prices = state.data.closePrices.map { it.toFloat() }
                    InteractiveStockChart(
                        prices = prices,
                        selectedTimeframe = chartTimeframe,
                        onTimeframeSelected = { viewModel.setChartTimeframe(it) },
                        currencySymbol = "₹"
                    )
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text(state.message, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Market Movers ──
        Text(
            text = "Market Movers",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (stocksState) {
            is UiState.Loading -> LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) { ShimmerBox(modifier = Modifier.width(160.dp).height(100.dp)) }
            }
            is UiState.Success -> {
                val stocks = (stocksState as UiState.Success<List<WatchlistItem>>).data
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stocks) { stock ->
                        StockMoverCard(
                            stock = stock,
                            onClick = {
                                viewModel.loadChart(stock.symbol)
                                onNavigateToStock(stock.symbol)
                            }
                        )
                    }
                }
            }
            is UiState.Error -> ErrorCard(
                message = (stocksState as UiState.Error).message,
                onRetry = { viewModel.loadDashboard() },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            else -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Quick Actions ──
        Text(
            text = "Quick Wealth Tools",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard("SIP Calc", Icons.Rounded.Calculate, Modifier.weight(1f)) {
                onNavigateToTools()
            }
            QuickActionCard("Risk Quiz", Icons.Rounded.Quiz, Modifier.weight(1f)) {
                onNavigateToRiskQuiz()
            }
            QuickActionCard("AI Copilot", Icons.Rounded.AutoAwesome, Modifier.weight(1f)) {
                onNavigateToAiAdvisor()
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
    }

    // ── Live Search Dialog ──
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Search Stock Symbols") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = { Text("e.g. AAPL, TSLA, NVDA") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recentSearches.isNotEmpty()) {
                        Text("Recent Searches", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentSearches.take(4).forEach { query ->
                                AssistChip(
                                    onClick = {
                                        searchInput = query
                                        viewModel.recordSearch(query)
                                        showSearchDialog = false
                                        onNavigateToStock(query)
                                    },
                                    label = { Text(query) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sym = searchInput.trim().uppercase()
                        if (sym.isNotBlank()) {
                            viewModel.recordSearch(sym)
                            showSearchDialog = false
                            onNavigateToStock(sym)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                ) {
                    Text("Search & Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun IndexChip(index: MarketIndex) {
    val isPositive = index.change >= 0
    GlassCard(modifier = Modifier.width(140.dp)) {
        Text(index.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("₹${String.format("%,.1f", index.value)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(
                imageVector = if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                contentDescription = null,
                tint = if (isPositive) SuccessGreen else DangerRed,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${String.format("%+.2f", index.changePercent)}%",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositive) SuccessGreen else DangerRed
            )
        }
    }
}

@Composable
private fun StockMoverCard(stock: WatchlistItem, onClick: () -> Unit) {
    val isPositive = stock.change >= 0
    GlassCard(modifier = Modifier.width(160.dp).clickable(onClick = onClick)) {
        Text(stock.symbol, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(stock.companyName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Spacer(modifier = Modifier.height(8.dp))
        Text("₹${String.format("%.2f", stock.currentPrice)}", style = StockPriceStyle, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown, null, tint = if (isPositive) SuccessGreen else DangerRed, modifier = Modifier.size(14.dp))
            Text("${String.format("%+.2f", stock.changePercent)}%", style = StockChangeStyle, color = if (isPositive) SuccessGreen else DangerRed)
        }
    }
}

@Composable
private fun QuickActionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier = modifier.clickable(onClick = onClick)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = PremiumPurple, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
