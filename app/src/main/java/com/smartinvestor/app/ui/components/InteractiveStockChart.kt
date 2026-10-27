package com.smartinvestor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinvestor.app.ui.theme.*

enum class ChartTimeframe(val label: String, val days: Int) {
    ONE_DAY("1D", 1),
    ONE_WEEK("1W", 7),
    ONE_MONTH("1M", 30),
    ONE_YEAR("1Y", 365),
    ALL("ALL", 730),
}

@Composable
fun InteractiveStockChart(
    prices: List<Float>,
    modifier: Modifier = Modifier,
    timestamps: List<Long> = emptyList(),
    selectedTimeframe: ChartTimeframe = ChartTimeframe.ONE_MONTH,
    onTimeframeSelected: ((ChartTimeframe) -> Unit)? = null,
    currencySymbol: String = "₹",
) {
    if (prices.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No chart data available for this timeframe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var activeIndex by remember { mutableStateOf<Int?>(null) }

    val minPrice = remember(prices) { prices.minOrNull() ?: 0f }
    val maxPrice = remember(prices) { prices.maxOrNull() ?: 1f }
    val priceRange = remember(minPrice, maxPrice) { (maxPrice - minPrice).takeIf { it > 0.001f } ?: 1f }

    val isPositive = prices.last() >= prices.first()
    val themeGainColor = SuccessGreen
    val themeLossColor = DangerRed
    val chartColor = if (isPositive) themeGainColor else themeLossColor

    val displayPrice = activeIndex?.let { prices.getOrNull(it) } ?: prices.last()
    val changeFromFirst = displayPrice - prices.first()
    val changePercent = if (prices.first() > 0) (changeFromFirst / prices.first()) * 100 else 0f
    val isScrubPositive = changeFromFirst >= 0

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Active scrub value header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$currencySymbol${String.format("%,.2f", displayPrice)}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isScrubPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = if (isScrubPositive) "Gain" else "Loss",
                        tint = if (isScrubPositive) SuccessGreen else DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${if (isScrubPositive) "+" else ""}${String.format("%.2f", changeFromFirst)} (${String.format("%+.2f", changePercent)}%)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isScrubPositive) SuccessGreen else DangerRed
                    )
                    if (activeIndex != null) {
                        Text(
                            text = "· Point ${activeIndex!! + 1} of ${prices.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Min & Max range badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "High: $currencySymbol${String.format("%,.2f", maxPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Low: $currencySymbol${String.format("%,.2f", minPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Canvas Chart with Gesture Detection ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(prices) {
                    detectTapGestures(
                        onPress = { offset ->
                            val width = size.width
                            val stepX = width / (prices.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / stepX).toInt().coerceIn(0, prices.lastIndex)
                            activeIndex = idx
                            tryAwaitRelease()
                            activeIndex = null
                        }
                    )
                }
                .pointerInput(prices) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val stepX = width / (prices.size - 1).coerceAtLeast(1)
                            activeIndex = (offset.x / stepX).toInt().coerceIn(0, prices.lastIndex)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val width = size.width
                            val stepX = width / (prices.size - 1).coerceAtLeast(1)
                            activeIndex = (change.position.x / stepX).toInt().coerceIn(0, prices.lastIndex)
                        },
                        onDragEnd = { activeIndex = null },
                        onDragCancel = { activeIndex = null }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingBottom = 16.dp.toPx()
                val effectiveHeight = height - paddingBottom
                val stepX = width / (prices.size - 1).coerceAtLeast(1)

                // Draw horizontal guide lines
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val linesCount = 3
                for (i in 0..linesCount) {
                    val y = (effectiveHeight / linesCount) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.06f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        pathEffect = dashedEffect,
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Construct main path
                val mainPath = Path()
                prices.forEachIndexed { i, p ->
                    val x = i * stepX
                    val normalized = 1f - ((p - minPrice) / priceRange)
                    val y = (normalized * (effectiveHeight - 20.dp.toPx())) + 10.dp.toPx()
                    if (i == 0) mainPath.moveTo(x, y) else mainPath.lineTo(x, y)
                }

                // Fill under path
                val fillPath = Path().apply {
                    addPath(mainPath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            chartColor.copy(alpha = 0.35f),
                            chartColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Glow outline stroke
                drawPath(
                    path = mainPath,
                    color = chartColor.copy(alpha = 0.4f),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Crisp foreground line
                drawPath(
                    path = mainPath,
                    color = chartColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // If scrubbing, draw cursor and dot
                activeIndex?.let { idx ->
                    val scrubX = idx * stepX
                    val scrubPrice = prices[idx]
                    val normalized = 1f - ((scrubPrice - minPrice) / priceRange)
                    val scrubY = (normalized * (effectiveHeight - 20.dp.toPx())) + 10.dp.toPx()

                    // Vertical tracking line
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, height),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )

                    // Outer halo
                    drawCircle(
                        color = chartColor.copy(alpha = 0.3f),
                        radius = 12.dp.toPx(),
                        center = Offset(scrubX, scrubY)
                    )
                    // Inner dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(scrubX, scrubY)
                    )
                }
            }
        }

        // ── Timeframe Selector Tabs ──
        if (onTimeframeSelected != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChartTimeframe.entries.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimeframeSelected(tf) },
                        label = {
                            Text(
                                tf.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PremiumPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PremiumPurple else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
