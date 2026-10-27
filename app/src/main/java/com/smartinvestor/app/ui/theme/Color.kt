package com.smartinvestor.app.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════
// Smart Investor — "Luxury Glass" Color Palette
// From brandguidelines.md
// ══════════════════════════════════════════════════════════

// ── Dark Mode (Default) ──
val DarkBackground = Color(0xFF071120)
val DarkSurface = Color(0xFF0E1836)
val DarkSurfaceVariant = Color(0xFF142044)
val DarkOnBackground = Color(0xFFE8ECF4)
val DarkOnSurface = Color(0xFFCDD5E0)
val DarkOnSurfaceVariant = Color(0xFF8893A7)

// ── Light Mode ──
val LightBackground = Color(0xFFF4F7FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF1F5)
val LightOnBackground = Color(0xFF0A1628)
val LightOnSurface = Color(0xFF1A2540)
val LightOnSurfaceVariant = Color(0xFF5A6478)

// ── Accent Colors ──
val PremiumPurple = Color(0xFF7C3AED)
val PremiumPurpleLight = Color(0xFF9F67FF)
val PremiumPurpleDark = Color(0xFF5B21B6)
val PremiumPurpleContainer = Color(0xFF2D1065)
val PremiumPurpleOnContainer = Color(0xFFE8DCFF)

// ── Semantic Colors ──
val SuccessGreen = Color(0xFF16C784)
val SuccessGreenLight = Color(0xFF34E89E)
val SuccessGreenDark = Color(0xFF0D9B64)
val SuccessGreenContainer = Color(0xFF062E1E)
val SuccessGreenOnContainer = Color(0xFFB8F5DA)

val DangerRed = Color(0xFFEA3943)
val DangerRedLight = Color(0xFFFF6B73)
val DangerRedDark = Color(0xFFC62828)
val DangerRedContainer = Color(0xFF3D0E11)
val DangerRedOnContainer = Color(0xFFFFDADB)

// ── Notification Priority Badge Colors ──
val BadgePurpleAI = PremiumPurple          // AI notifications
val BadgeGreenProfit = SuccessGreen         // Profit notifications
val BadgeBlueNews = Color(0xFF3B82F6)       // News notifications
val BadgeBlueNewsContainer = Color(0xFF0C2D6B)

// ── Glass Morphism Colors ──
val GlassBorderLight = Color(0x30FFFFFF)
val GlassBorderDark = Color(0x15FFFFFF)
val GlassOverlay = Color(0x0DFFFFFF)
val GlassShimmerHighlight = Color(0x33FFFFFF)

// ── Gradient Helpers ──
val GradientPurpleStart = Color(0xFF7C3AED)
val GradientPurpleEnd = Color(0xFF4F46E5)
val GradientGreenStart = Color(0xFF16C784)
val GradientGreenEnd = Color(0xFF06B6D4)
val GradientDarkStart = Color(0xFF071120)
val GradientDarkEnd = Color(0xFF0E1836)

// ── Chart Colors ──
val ChartLine = PremiumPurple
val ChartFill = Color(0x337C3AED)
val ChartGrid = Color(0x1AFFFFFF)
val ChartPositive = SuccessGreen
val ChartNegative = DangerRed
