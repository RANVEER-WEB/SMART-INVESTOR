package com.smartinvestor.app.ui.screens.notifications

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

data class NotificationItem(val id: Int, val title: String, val message: String, val type: String, val isRead: Boolean, val time: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notifications = remember {
        mutableStateListOf(
            NotificationItem(1, "AI Insight", "NVDA shows strong momentum. Consider reviewing.", "ai", false, "2m ago"),
            NotificationItem(2, "Profit Alert", "AAPL is up +3.2% today 🎉", "profit", false, "15m ago"),
            NotificationItem(3, "Market News", "Fed announces rate decision tomorrow", "news", true, "1h ago"),
            NotificationItem(4, "AI Insight", "Your portfolio risk level has changed", "ai", true, "3h ago"),
            NotificationItem(5, "Profit Alert", "TSLA crossed your target price of $250", "profit", true, "5h ago"),
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Notifications", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
            actions = {
                TextButton(onClick = { /* mark all read */ }) { Text("Read All", color = PremiumPurple) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notifications) { notif ->
                val badgeColor = when (notif.type) { "ai" -> PremiumPurple; "profit" -> SuccessGreen; else -> BadgeBlueNews }
                val icon = when (notif.type) { "ai" -> Icons.Rounded.AutoAwesome; "profit" -> Icons.Rounded.TrendingUp; else -> Icons.Rounded.Newspaper }

                GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = !notif.isRead, glowColor = badgeColor) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = MaterialTheme.shapes.small, color = badgeColor.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = badgeColor, modifier = Modifier.size(20.dp)) }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Normal), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                if (!notif.isRead) { Surface(shape = MaterialTheme.shapes.extraSmall, color = badgeColor, modifier = Modifier.size(8.dp)) {} }
                            }
                            Text(notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(notif.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
