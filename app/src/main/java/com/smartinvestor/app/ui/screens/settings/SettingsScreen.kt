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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // ── Theme Appearance (DataStore Persisted) ──
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple(ThemeMode.LIGHT, "Light", Icons.Rounded.LightMode),
                    Triple(ThemeMode.DARK, "Dark", Icons.Rounded.DarkMode),
                    Triple(ThemeMode.SYSTEM, "System", Icons.Rounded.SettingsBrightness)
                ).forEach { (mode, label, icon) ->
                    val isSelected = currentTheme == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(icon, null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // ── Security & Biometrics ──
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Security & Access",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Fingerprint, null, tint = PremiumPurple, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric App Unlock", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Unlock with Fingerprint or Face ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                )
            }
        }

        // ── Virtual Portfolio & Paper Trading Management ──
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Paper Trading Wallet",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reset your simulated portfolio cash to ₹10,00,000 for learning without financial risk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumPurple)
            ) {
                Icon(Icons.Rounded.RestartAlt, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Virtual Balance")
            }
        }

        // ── Support & Founder Credentials (Rule §3) ──
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Support & Founder Credentials",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            SettingsRow("Lead Developer", "Ranveer Mandal", Icons.Rounded.Person)
            SettingsRow("Support Email", "ranveermandal183@gmail.com", Icons.Rounded.Email)
            SettingsRow("Direct Contact", "+91 8340320664", Icons.Rounded.Phone)
            SettingsRow("SEBI Compliance", "Educational Simulator (Non-Advisory)", Icons.Rounded.VerifiedUser)
        }

        // ── About & Version ──
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsRow("App Version", "2.1.0-PRO", Icons.Rounded.Info)
            SettingsRow("Design Engine", "Luxury Glass 2.0", Icons.Rounded.AutoAwesome)
            SettingsRow("AI Engine", "Gemini 3.8 Flash REST", Icons.Rounded.Psychology)
        }

        // ── Sign Out ──
        Button(
            onClick = { viewModel.signOut(onSignOut) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Paper Balance?") },
            text = { Text("Your virtual cash will be restored to ₹10,00,000 and past trades cleared.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetVirtualBalance()
                    showResetDialog = false
                }) {
                    Text("Reset", color = PremiumPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}
