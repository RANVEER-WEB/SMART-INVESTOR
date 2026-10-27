package com.smartinvestor.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.content.Context
import android.content.ContextWrapper

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ══════════════════════════════════════════════════════════
// Smart Investor — Theme Configuration
// Dual theme (Dark default + Light) per brandguidelines.md §1
// System-auto detection per gemini.md §2
// ══════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = PremiumPurple,
    onPrimary = DarkOnBackground,
    primaryContainer = PremiumPurpleContainer,
    onPrimaryContainer = PremiumPurpleOnContainer,
    secondary = SuccessGreen,
    onSecondary = DarkOnBackground,
    secondaryContainer = SuccessGreenContainer,
    onSecondaryContainer = SuccessGreenOnContainer,
    tertiary = BadgeBlueNews,
    tertiaryContainer = BadgeBlueNewsContainer,
    error = DangerRed,
    onError = DarkOnBackground,
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerRedOnContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = GlassBorderLight,
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumPurple,
    onPrimary = LightBackground,
    primaryContainer = PremiumPurpleLight,
    onPrimaryContainer = PremiumPurpleDark,
    secondary = SuccessGreen,
    onSecondary = LightBackground,
    secondaryContainer = SuccessGreenLight,
    onSecondaryContainer = SuccessGreenDark,
    tertiary = BadgeBlueNews,
    error = DangerRed,
    onError = LightBackground,
    errorContainer = DangerRedLight,
    onErrorContainer = DangerRedDark,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = GlassBorderDark,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
)

/**
 * Theme mode enum for DataStore persistence.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

@Composable
fun SmartInvestorTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    // Update system bars to match theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SmartInvestorTypography,
        shapes = SmartInvestorShapes,
        content = content
    )
}
