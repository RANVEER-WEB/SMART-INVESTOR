package com.smartinvestor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.data.repository.UserPreferencesRepository
import com.smartinvestor.app.navigation.SmartInvestorNavGraph
import com.smartinvestor.app.ui.theme.SmartInvestorTheme
import com.smartinvestor.app.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ══════════════════════════════════════════════════════════
// Smart Investor — Main Activity
// Single Activity architecture with Compose
// ══════════════════════════════════════════════════════════

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

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

            // Theme mode connected to DataStore per gemini.md §2
            val themeMode by preferencesRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.DARK)

            SmartInvestorTheme(themeMode = themeMode) {
                SmartInvestorNavGraph()
            }
        }
    }
}
