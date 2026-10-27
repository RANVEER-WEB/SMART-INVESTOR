package com.smartinvestor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.theme.GlassShimmerHighlight

// ══════════════════════════════════════════════════════════
// Smart Investor — Glass Shimmer Loading
// Per brandguidelines.md §2: "Glass Shimmer" skeleton screens
// ══════════════════════════════════════════════════════════

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        GlassShimmerHighlight,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Shimmer loading skeleton for a stock list item.
 */
@Composable
fun ShimmerStockCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier
                        .width(150.dp)
                        .height(12.dp)
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                ShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                )
            }
        }
    }
}

/**
 * Shimmer loading for the dashboard chart area.
 */
@Composable
fun ShimmerChart() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Shimmer avatar circle.
 */
@Composable
fun ShimmerAvatar(size: Dp = 48.dp) {
    ShimmerBox(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(50)
    )
}

/**
 * Full shimmer loading screen with multiple skeleton cards.
 */
@Composable
fun ShimmerLoadingList(itemCount: Int = 5) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            ShimmerStockCard()
        }
    }
}
