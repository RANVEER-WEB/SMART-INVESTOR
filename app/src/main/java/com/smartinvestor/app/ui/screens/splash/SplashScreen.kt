package com.smartinvestor.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinvestor.app.ui.theme.*
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════
// Smart Investor — Animated Splash Screen
// Per gemini.md §1.1: Animated Splash + Logo Intro + Trust Statement
// Per brandguidelines.md §3: Logo scale 0.8→1.0 with "Glow Bloom"
// ══════════════════════════════════════════════════════════

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // Logo scale: 0.8 → 1.0 per brand guidelines
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val glowRadius = remember { Animatable(0f) }
    val trustAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phase 1: Logo appears with glow bloom
        logoAlpha.animateTo(1f, tween(600, easing = EaseOutCubic))
        logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        glowRadius.animateTo(300f, tween(800, easing = EaseOutCubic))

        // Phase 2: Trust statement fades in
        delay(200)
        trustAlpha.animateTo(1f, tween(500))

        // Phase 3: Hold, then navigate
        delay(1500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glow bloom behind logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (glowRadius.value >= 1f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumPurple.copy(alpha = 0.25f),
                            PremiumPurple.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = glowRadius.value
                    ),
                    radius = glowRadius.value,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(logoScale.value)
                .alpha(logoAlpha.value)
        ) {
            // App Logo / Brand Name
            Icon(
                imageVector = Icons.Rounded.Diamond,
                contentDescription = "Logo",
                tint = PremiumPurpleLight,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SMART INVESTOR",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                ),
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Invest Smarter. Grow Faster.",
                style = MaterialTheme.typography.bodyMedium,
                color = PremiumPurpleLight,
            )
        }

        // Trust Statement at bottom
        Text(
            text = "Your trusted partner in wealth creation\nPowered by AI • Secured by Supabase",
            style = MaterialTheme.typography.bodySmall,
            color = DarkOnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(trustAlpha.value)
        )
    }
}
