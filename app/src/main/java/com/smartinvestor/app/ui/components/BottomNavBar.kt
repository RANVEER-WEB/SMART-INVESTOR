package com.smartinvestor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.theme.GlassBorderLight
import com.smartinvestor.app.ui.theme.GlassOverlay

// ══════════════════════════════════════════════════════════
// Smart Investor — Glass Bottom Navigation Bar
// Glassmorphism bottom nav per brandguidelines.md §4
// ══════════════════════════════════════════════════════════

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    DASHBOARD("dashboard", "Home", Icons.Rounded.Home, Icons.Rounded.Home),
    PORTFOLIO("portfolio", "Portfolio", Icons.Rounded.PieChart, Icons.Rounded.PieChart),
    AI_ADVISOR("ai_advisor", "AI", Icons.Rounded.AutoAwesome, Icons.Rounded.AutoAwesome),
    WATCHLIST("watchlist", "Watchlist", Icons.Rounded.BookmarkBorder, Icons.Rounded.Bookmark),
    SETTINGS("settings", "Settings", Icons.Rounded.Settings, Icons.Rounded.Settings),
}

@Composable
fun SmartInvestorBottomBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.extraLarge

    NavigationBar(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape)
            .border(width = 0.5.dp, color = GlassBorderLight, shape = shape),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 0.dp,
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(text = item.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
