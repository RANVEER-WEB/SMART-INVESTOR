package com.smartinvestor.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Admin Suite", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard("Total Users", "1,245", Icons.Rounded.People, SuccessGreen)
            AdminStatCard("Active Sessions", "342", Icons.Rounded.Visibility, PremiumPurple)
            AdminStatCard("Open Tickets", "18", Icons.Rounded.SupportAgent, BadgeBlueNews)
            AdminStatCard("Revenue (MTD)", "₹4,52,300", Icons.Rounded.CurrencyRupee, SuccessGreen)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Management", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminActionCard("Users", Icons.Rounded.ManageAccounts, Modifier.weight(1f))
                AdminActionCard("Payments", Icons.Rounded.Payment, Modifier.weight(1f))
                AdminActionCard("Tickets", Icons.Rounded.ConfirmationNumber, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, icon: ImageVector, color: Color) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
            }
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = FinancialSubBalanceStyle, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun AdminActionCard(label: String, icon: ImageVector, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = PremiumPurple, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
