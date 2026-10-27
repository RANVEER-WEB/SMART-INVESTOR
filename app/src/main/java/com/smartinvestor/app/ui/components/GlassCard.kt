package com.smartinvestor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.theme.GlassBorderLight
import com.smartinvestor.app.ui.theme.GlassOverlay
import com.smartinvestor.app.ui.theme.GlassShimmerHighlight

// ══════════════════════════════════════════════════════════
// Smart Investor — GlassCard Component
// Frosted glass surface per brandguidelines.md §4
// "Luxury Glass" design system
// ══════════════════════════════════════════════════════════

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    showGlow: Boolean = false,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large

    // Subtle animated glow when showGlow is true
    val infiniteTransition = rememberInfiniteTransition(label = "glassGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (showGlow) {
                    Modifier.border(
                        width = 1.dp, // 1dp border as per Luxury Glass specs
                        brush = Brush.linearGradient(
                            colors = listOf(
                                glowColor.copy(alpha = glowAlpha),
                                glowColor.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent,
                            )
                        ),
                        shape = shape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp, // 1dp border as per Luxury Glass specs
                        color = GlassBorderLight,
                        shape = shape
                    )
                }
            )
    ) {
        // Luxury Glass shimmer sweep
        val shimmerTransition = rememberInfiniteTransition(label = "glassShimmerSweep")
        val sweepAnim by shimmerTransition.animateFloat(
            initialValue = -500f,
            targetValue = 1500f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerSweep"
        )
        
        // Glass background layer with shimmer sweep
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassOverlay,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.80f), // Increased transparency to 80% per Luxury Glass Pass
                        )
                    )
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassShimmerHighlight.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        start = Offset(sweepAnim, sweepAnim),
                        end = Offset(sweepAnim + 300f, sweepAnim + 300f)
                    )
                )
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Elevated glass card variant with stronger surface appearance.
 */
@Composable
fun ElevatedGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        borderWidth = 0.5.dp,
        content = content
    )
}
