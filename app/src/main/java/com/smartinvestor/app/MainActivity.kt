package com.smartinvestor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.smartinvestor.app.navigation.SmartInvestorNavGraph
import com.smartinvestor.app.ui.theme.SmartInvestorTheme
import com.smartinvestor.app.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

// ══════════════════════════════════════════════════════════
// Smart Investor — Main Activity
// Single Activity architecture with Compose
// ══════════════════════════════════════════════════════════

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the splash screen visible for a brief moment while Compose initializes
        var isReady by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !isReady }

        setContent {
            // Signal ready immediately — Compose is now rendering
            LaunchedEffect(Unit) { isReady = true }

            // Theme mode — will be connected to DataStore in Phase 2
            var themeMode by remember { mutableStateOf(ThemeMode.DARK) }

            SmartInvestorTheme(themeMode = themeMode) {
                SmartInvestorNavGraph()
            }
        }
    }
}
