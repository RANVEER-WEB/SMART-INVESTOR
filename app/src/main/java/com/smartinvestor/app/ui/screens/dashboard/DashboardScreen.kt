package com.smartinvestor.app.ui.screens.dashboard

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

// ══════════════════════════════════════════════════════════
// Smart Investor — Dashboard (Primary Command Center)
// Per gemini.md §1.4: Interactive 3D Stock Graph
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAiAdvisor: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val stocksState by viewModel.stocksState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()
    val chartState by viewModel.chartState.collectAsStateWithLifecycle()

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
                Text("Good Morning 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Smart Investor", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(Icons.Rounded.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // AI Orb
                IconButton(onClick = onNavigateToAiAdvisor) {
                    Icon(Icons.Rounded.AutoAwesome, "AI Advisor", tint = PremiumPurple)
                }
            }
        }

        // ── Offline Indicator ──
        if (isOffline) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                color = DangerRedContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.WifiOff, null, tint = DangerRed, modifier = Modifier.size(16.dp))
                    Text("Showing cached data", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.loadDashboard() }) {
                        Text("Retry Sync", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Portfolio Value Card ──
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            showGlow = true,
            glowColor = PremiumPurple
        ) {
            Text("Total Portfolio Value", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("₹12,45,678.90", style = FinancialBalanceStyle, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.TrendingUp, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Text("+₹24,560.00 (2.34%)", style = StockChangeStyle, color = SuccessGreen)
                Text("today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Stock Chart Area ──
        Text("Interactive Chart · $selectedSymbol", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(220.dp)) {
            when (val state = chartState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumPurple)
                    }
                }
                is UiState.Success -> {
                    if (state.data.closePrices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No chart data available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Interactive3DStockChart(prices = state.data.closePrices.map { it.toFloat() })
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Market Movers ──
        Text("Market Movers", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))

        when (stocksState) {
            is UiState.Loading -> LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(4) { ShimmerBox(modifier = Modifier.width(160.dp).height(100.dp)) }
            }
            is UiState.Success -> {
                val stocks = (stocksState as UiState.Success<List<WatchlistItem>>).data
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(stocks) { stock -> StockMoverCard(stock = stock, onClick = { viewModel.loadChart(stock.symbol) }) }
                }
            }
            is UiState.Error -> ErrorCard(message = (stocksState as UiState.Error).message, onRetry = { viewModel.loadDashboard() }, modifier = Modifier.padding(horizontal = 20.dp))
            else -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Quick Actions ──
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("SIP Calc", Icons.Rounded.Calculate, Modifier.weight(1f)) {}
            QuickActionCard("Tax Report", Icons.Rounded.Description, Modifier.weight(1f)) {}
            QuickActionCard("Risk Quiz", Icons.Rounded.Quiz, Modifier.weight(1f)) {}
        }

        Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
    }
}

@Composable
private fun StockMoverCard(stock: WatchlistItem, onClick: () -> Unit) {
    val isPositive = stock.change >= 0
    GlassCard(modifier = Modifier.width(160.dp).clickable(onClick = onClick)) {
        Text(stock.symbol, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(stock.companyName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Spacer(modifier = Modifier.height(8.dp))
        Text("$${String.format("%.2f", stock.currentPrice)}", style = StockPriceStyle, color = MaterialTheme.colorScheme.onSurface)
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

@Composable
fun Interactive3DStockChart(prices: List<Float>) {
    val maxPrice = prices.maxOrNull() ?: 1f
    val minPrice = prices.minOrNull() ?: 0f
    val range = (maxPrice - minPrice).takeIf { it > 0 } ?: 1f
    
    val isPositive = prices.last() >= prices.first()
    val lineColor = if (isPositive) SuccessGreen else DangerRed
    
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / (prices.size - 1).coerceAtLeast(1)
        
        val path = androidx.compose.ui.graphics.Path().apply {
            prices.forEachIndexed { index, price ->
                val x = index * stepX
                val normalizedY = 1f - ((price - minPrice) / range)
                val y = normalizedY * height
                
                if (index == 0) moveTo(x, y)
                else lineTo(x, y)
            }
        }
        
        // Draw glow/gradient underneath
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.5f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )
        
        // Draw main 3D-like thick stroke
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}
