package com.smartinvestor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════
// Smart Investor — Success Glow / Celebration
// Per brandguidelines.md §2: "Celebration Glow"
// Soft green radial bloom + "Premium Confirmation Card"
// ══════════════════════════════════════════════════════════

@Composable
fun SuccessGlow(
    message: String,
    subtitle: String? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Celebration bloom animation
    val bloomAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Bloom outward
        bloomAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        // Scale in the card
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Auto-dismiss after 3 seconds
        if (onDismiss != null) {
            delay(3000)
            onDismiss()
        }
    }

    // Pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "successPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successPulseAlpha"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .drawBehind {
                // Green radial bloom
                val bloomRadius = 200f * bloomAnim.value
                if (bloomRadius >= 1f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SuccessGreen.copy(alpha = pulseAlpha * bloomAnim.value),
                                Color.Transparent,
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = bloomRadius
                        ),
                        radius = bloomRadius,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            },
        glowColor = SuccessGreen,
        showGlow = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = SuccessGreen,
                textAlign = TextAlign.Center,
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (onDismiss != null) {
                PremiumButton(
                    text = "Continue",
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                    )
                )
            }
        }
    }
}
