package com.smartinvestor.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartinvestor.app.ui.components.BottomNavItem
import com.smartinvestor.app.ui.components.SmartInvestorBottomBar
import com.smartinvestor.app.ui.screens.admin.AdminScreen
import com.smartinvestor.app.ui.screens.ai_advisor.AiAdvisorScreen
import com.smartinvestor.app.ui.screens.auth.LoginScreen
import com.smartinvestor.app.ui.screens.auth.RegisterScreen
import com.smartinvestor.app.ui.screens.dashboard.DashboardScreen
import com.smartinvestor.app.ui.screens.kyc.KycStepperScreen
import com.smartinvestor.app.ui.screens.learning.LearningScreen
import com.smartinvestor.app.ui.screens.learning.LessonDetailScreen
import com.smartinvestor.app.ui.screens.learning.QuizScreen
import com.smartinvestor.app.ui.screens.notifications.NotificationsScreen
import com.smartinvestor.app.ui.screens.portfolio.PortfolioScreen
import com.smartinvestor.app.ui.screens.settings.SettingsScreen
import com.smartinvestor.app.ui.screens.splash.SplashScreen
import com.smartinvestor.app.ui.screens.stock_detail.StockDetailScreen
import com.smartinvestor.app.ui.screens.tools.FinancialToolsScreen
import com.smartinvestor.app.ui.screens.tools.RiskAssessmentScreen
import com.smartinvestor.app.ui.screens.watchlist.WatchlistScreen
import com.smartinvestor.app.ui.screens.PlaceholderScreen

// ══════════════════════════════════════════════════════════
// Smart Investor — Centralized NavHost
// Per gemini.md §1: 25+ Composable modules
// ══════════════════════════════════════════════════════════

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MFA_VERIFY = "mfa_verify"
    const val BIOMETRIC_SETUP = "biometric_setup"
    const val KYC = "kyc"
    const val KYC_PAN = "kyc_pan"
    const val KYC_AADHAAR = "kyc_aadhaar"
    const val KYC_BANK = "kyc_bank"
    const val KYC_DEMAT = "kyc_demat"
    const val DASHBOARD = "dashboard"
    const val STOCK_DETAIL = "stock_detail/{symbol}"
    const val MARKET_MOVERS = "market_movers"
    const val PORTFOLIO = "portfolio"
    const val PORTFOLIO_HISTORY = "portfolio_history"
    const val WATCHLIST = "watchlist"
    const val AI_ADVISOR = "ai_advisor"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val LEARNING = "learning"
    const val LESSON_DETAIL = "lesson_detail/{lessonId}"
    const val QUIZ = "quiz/{lessonId}"
    const val ADMIN = "admin"
    const val USER_MGMT = "user_mgmt"
    const val PAYMENT_MGMT = "payment_mgmt"
    const val TICKET_MGMT = "ticket_mgmt"
    const val FINANCIAL_TOOLS = "financial_tools"
    const val SIP_CALCULATOR = "sip_calculator"
    const val TAX_REPORT = "tax_report"
    const val RISK_ASSESSMENT = "risk_assessment"
}

// Screens that show the bottom nav bar
private val bottomNavRoutes = setOf(
    Routes.DASHBOARD, Routes.PORTFOLIO, Routes.WATCHLIST, Routes.AI_ADVISOR, Routes.SETTINGS
)

@Composable
fun SmartInvestorNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SmartInvestorBottomBar(
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(300)) },
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onSplashComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.KYC) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.KYC) {
                KycStepperScreen(onKycComplete = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.KYC) { inclusive = true }
                    }
                })
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToAiAdvisor = { navController.navigate(Routes.AI_ADVISOR) },
                    onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onNavigateToStock = { symbol -> navController.navigate("stock_detail/$symbol") },
                    onNavigateToTools = { navController.navigate(Routes.FINANCIAL_TOOLS) },
                    onNavigateToRiskQuiz = { navController.navigate(Routes.RISK_ASSESSMENT) }
                )
            }

            composable(
                route = Routes.STOCK_DETAIL,
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) {
                StockDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PORTFOLIO) {
                PortfolioScreen(onNavigateToStock = { symbol -> navController.navigate("stock_detail/$symbol") })
            }

            composable(Routes.WATCHLIST) {
                WatchlistScreen(onNavigateToStock = { symbol -> navController.navigate("stock_detail/$symbol") })
            }

            composable(Routes.AI_ADVISOR) {
                AiAdvisorScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onSignOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.LEARNING) {
                LearningScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLesson = { lessonId -> navController.navigate("lesson_detail/$lessonId") }
                )
            }

            composable(
                route = Routes.LESSON_DETAIL,
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: "lesson_1"
                LessonDetailScreen(
                    lessonId = lessonId,
                    onBack = { navController.popBackStack() },
                    onTakeQuiz = { id -> navController.navigate("quiz/$id") }
                )
            }

            composable(
                route = Routes.QUIZ,
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: "lesson_1"
                QuizScreen(
                    lessonId = lessonId,
                    onBack = { navController.popBackStack() },
                    onQuizPassed = { /* optionally handle badge award */ }
                )
            }

            composable(Routes.ADMIN) {
                AdminScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.FINANCIAL_TOOLS) {
                FinancialToolsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToRiskQuiz = { navController.navigate(Routes.RISK_ASSESSMENT) }
                )
            }

            composable(Routes.RISK_ASSESSMENT) {
                RiskAssessmentScreen(onBack = { navController.popBackStack() })
            }
            
            // Sub-routes and specialized modules
            composable(Routes.MFA_VERIFY) { PlaceholderScreen("MFA Verify") { navController.popBackStack() } }
            composable(Routes.BIOMETRIC_SETUP) { PlaceholderScreen("Biometric Setup") { navController.popBackStack() } }
            composable(Routes.KYC_PAN) { PlaceholderScreen("PAN Verification") { navController.popBackStack() } }
            composable(Routes.KYC_AADHAAR) { PlaceholderScreen("Aadhaar Verification") { navController.popBackStack() } }
            composable(Routes.KYC_BANK) { PlaceholderScreen("Bank Linking") { navController.popBackStack() } }
            composable(Routes.KYC_DEMAT) { PlaceholderScreen("Demat Linking") { navController.popBackStack() } }
            composable(Routes.MARKET_MOVERS) { PlaceholderScreen("Market Movers") { navController.popBackStack() } }
            composable(Routes.PORTFOLIO_HISTORY) { PlaceholderScreen("Portfolio History") { navController.popBackStack() } }
            composable(Routes.USER_MGMT) { PlaceholderScreen("User Management") { navController.popBackStack() } }
            composable(Routes.PAYMENT_MGMT) { PlaceholderScreen("Payment Management") { navController.popBackStack() } }
            composable(Routes.TICKET_MGMT) { PlaceholderScreen("Ticket Management") { navController.popBackStack() } }
            composable(Routes.SIP_CALCULATOR) { FinancialToolsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TAX_REPORT) { FinancialToolsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
