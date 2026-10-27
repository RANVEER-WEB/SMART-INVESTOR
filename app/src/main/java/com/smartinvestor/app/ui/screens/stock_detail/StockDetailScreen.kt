package com.smartinvestor.app.ui.screens.stock_detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.domain.model.UiState
import com.smartinvestor.app.ui.components.*
import com.smartinvestor.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    onBack: () -> Unit,
    viewModel: StockDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeframe by viewModel.timeframe.collectAsStateWithLifecycle()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()

    var showOrderDialog by remember { mutableStateOf(false) }
    var orderType by remember { mutableStateOf("BUY") }
    var orderQuantity by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is UiState.Success -> {
                            Column {
                                Text(
                                    text = state.data.symbol,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.data.profile?.name ?: state.data.symbol,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> Text(viewModel.symbol)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is UiState.Success) {
                        val isWatchlist = (uiState as UiState.Success<StockDetailData>).data.isInWatchlist
                        IconButton(onClick = { viewModel.toggleWatchlist() }) {
                            Icon(
                                imageVector = if (isWatchlist) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isWatchlist) PremiumPurpleLight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                orderType = "BUY"
                                orderQuantity = 1
                                showOrderDialog = true
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buy (Paper)", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                orderType = "SELL"
                                orderQuantity = 1
                                showOrderDialog = true
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Rounded.Sell, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sell (Paper)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
                        ShimmerBox(modifier = Modifier.fillMaxWidth().height(240.dp))
                        ShimmerBox(modifier = Modifier.fillMaxWidth().height(150.dp))
                    }
                }
                is UiState.Error -> {
                    ErrorCard(
                        message = state.message,
                        onRetry = { viewModel.loadStockDetails() },
                        modifier = Modifier.padding(20.dp).align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    val data = state.data
                    val quote = data.quote
                    val profile = data.profile

                    val candlePrices = remember(data.candles) {
                        data.candles?.closePrices?.map { it.toFloat() } ?: listOf(
                            (quote.previousClose).toFloat(),
                            (quote.openPrice).toFloat(),
                            (quote.lowPrice).toFloat(),
                            (quote.highPrice).toFloat(),
                            (quote.currentPrice).toFloat(),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── Order Success or Error Alert ──
                        when (val ord = orderState) {
                            is UiState.Success -> {
                                SuccessGlow(
                                    message = ord.data,
                                    onDismiss = { viewModel.clearOrderState() }
                                )
                            }
                            is UiState.Error -> {
                                ErrorCard(
                                    message = ord.message,
                                    onRetry = { viewModel.clearOrderState() }
                                )
                            }
                            else -> {}
                        }

                        // ── Chart Area ──
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            showGlow = true,
                            glowColor = if (quote.change >= 0) SuccessGreen else DangerRed
                        ) {
                            InteractiveStockChart(
                                prices = candlePrices,
                                selectedTimeframe = timeframe,
                                onTimeframeSelected = { viewModel.setTimeframe(it) },
                                currencySymbol = "₹"
                            )
                        }

                        // ── Fundamentals & Key Metrics ──
                        Text(
                            text = "Key Metrics & Fundamentals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    MetricItem("Open", "₹${String.format("%.2f", quote.openPrice)}")
                                    MetricItem("Prev. Close", "₹${String.format("%.2f", quote.previousClose)}")
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    MetricItem("Day High", "₹${String.format("%.2f", quote.highPrice)}")
                                    MetricItem("Day Low", "₹${String.format("%.2f", quote.lowPrice)}")
                                }
                                if (profile != null) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        MetricItem("Market Cap", "₹${String.format("%,.0f", profile.marketCap)}M")
                                        MetricItem("Industry", profile.industry.ifBlank { "Technology" })
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        MetricItem("Exchange", profile.exchange.ifBlank { "NSE/BSE" })
                                        MetricItem("Country", profile.country.ifBlank { "India" })
                                    }
                                }
                            }
                        }

                        // ── SEBI Regulatory Disclaimer ──
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Prices are sourced from market feeds. Paper trading executes at current live prices for simulation and training.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
                    }
                }
                else -> {}
            }
        }
    }

    // ── Paper Trading Order Modal ──
    if (showOrderDialog && uiState is UiState.Success) {
        val currentPrice = (uiState as UiState.Success<StockDetailData>).data.quote.currentPrice
        val totalEstimatedCost = currentPrice * orderQuantity

        AlertDialog(
            onDismissRequest = { showOrderDialog = false },
            title = {
                Text("$orderType ${viewModel.symbol}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Current Market Price: ₹${String.format("%.2f", currentPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Quantity (Shares):", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (orderQuantity > 1) orderQuantity-- }
                            ) {
                                Icon(Icons.Rounded.Remove, null)
                            }
                            Text(
                                text = "$orderQuantity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { orderQuantity++ }
                            ) {
                                Icon(Icons.Rounded.Add, null)
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Total:", fontWeight = FontWeight.Bold)
                        Text(
                            "₹${String.format("%,.2f", totalEstimatedCost)}",
                            fontWeight = FontWeight.Bold,
                            color = if (orderType == "BUY") SuccessGreen else DangerRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (orderType == "BUY") {
                            viewModel.buyStock(orderQuantity)
                        } else {
                            viewModel.sellStock(orderQuantity)
                        }
                        showOrderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (orderType == "BUY") SuccessGreen else DangerRed
                    )
                ) {
                    Text("Confirm $orderType")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOrderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
    }
}
