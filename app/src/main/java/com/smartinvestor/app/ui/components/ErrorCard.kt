package com.smartinvestor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.theme.DangerRed
import com.smartinvestor.app.ui.theme.DangerRedContainer
import com.smartinvestor.app.ui.theme.DangerRedLight

// ══════════════════════════════════════════════════════════
// Smart Investor — Professional Error Card
// Per brandguidelines.md §2: "Professional Alert Cards"
// with soft-red glow and "Smart Retry" button
// ══════════════════════════════════════════════════════════

@Composable
fun ErrorCard(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Animated soft-red glow
    val infiniteTransition = rememberInfiniteTransition(label = "errorGlow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "errorGlowRadius"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Soft-red radial glow behind the card
                if (glowRadius >= 1f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DangerRed.copy(alpha = 0.15f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            },
        glowColor = DangerRed,
        showGlow = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Error",
                tint = DangerRed,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            if (onRetry != null) {
                PremiumButton(
                    text = "Smart Retry",
                    onClick = onRetry,
                    icon = Icons.Rounded.Refresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                    )
                )
            }
        }
    }
}

/**
 * Inline error indicator for smaller contexts.
 */
@Composable
fun InlineError(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = DangerRed,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall,
            color = DangerRed,
        )
    }
}
