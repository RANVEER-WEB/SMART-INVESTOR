package com.smartinvestor.app.ui.screens.watchlist

import androidx.compose.animation.animateColorAsState
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
import com.smartinvestor.app.domain.model.WatchlistItem
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@Composable
fun WatchlistScreen() {
    val watchlist = remember {
        mutableStateListOf(
            WatchlistItem("AAPL", "Apple Inc.", 178.50, 2.30, 1.31),
            WatchlistItem("GOOGL", "Alphabet Inc.", 2950.0, -15.0, -0.51),
            WatchlistItem("TSLA", "Tesla Inc.", 245.75, 8.50, 3.58),
            WatchlistItem("AMZN", "Amazon.com", 185.60, 1.20, 0.65),
            WatchlistItem("NVDA", "NVIDIA Corp.", 875.30, 22.40, 2.63),
            WatchlistItem("META", "Meta Platforms", 505.75, -3.20, -0.63),
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Watchlist", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${watchlist.size} symbols tracked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(watchlist, key = { it.symbol }) { item ->
            WatchlistCard(item = item, onRemove = { watchlist.remove(item) })
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun WatchlistCard(item: WatchlistItem, onRemove: () -> Unit) {
    val isPositive = item.change >= 0
    val changeColor by animateColorAsState(if (isPositive) SuccessGreen else DangerRed, label = "changeColor")

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.symbol, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(item.companyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format("%.2f", item.currentPrice)}", style = StockPriceStyle, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown, null, tint = changeColor, modifier = Modifier.size(14.dp))
                    Text("${String.format("%+.2f", item.changePercent)}%", style = StockChangeStyle, color = changeColor)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}
