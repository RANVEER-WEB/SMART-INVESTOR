package com.smartinvestor.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@Composable
fun SettingsScreen(onSignOut: () -> Unit) {
    var selectedTheme by remember { mutableIntStateOf(2) } // 0=Light, 1=Dark, 2=System
    val themeOptions = listOf("Light", "Dark", "System")

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)

        // Theme
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Appearance", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                themeOptions.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedTheme == index,
                        onClick = { selectedTheme = index },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(
                                when (index) { 0 -> Icons.Rounded.LightMode; 1 -> Icons.Rounded.DarkMode; else -> Icons.Rounded.SettingsBrightness },
                                null, modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Notifications
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsToggle("Push Notifications", Icons.Rounded.Notifications, true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
            SettingsToggle("AI Recommendations", Icons.Rounded.AutoAwesome, true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
            SettingsToggle("Price Alerts", Icons.Rounded.NotificationsActive, false)
        }

        // Support
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Support & Contact", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            SettingsRow("Developer", "Ranveer Mandal", Icons.Rounded.Person)
            SettingsRow("Email", "ranveermandal183@gmail.com", Icons.Rounded.Email)
            SettingsRow("Phone", "+91 8340320664", Icons.Rounded.Phone)
        }

        // About
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsRow("App Version", "1.0.0", Icons.Rounded.Info)
            SettingsRow("Build", "Phase 1 - Foundation", Icons.Rounded.Build)
        }

        // Sign Out
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsToggle(label: String, icon: ImageVector, defaultValue: Boolean) {
    var checked by remember { mutableStateOf(defaultValue) }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PremiumPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
private fun SettingsRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
