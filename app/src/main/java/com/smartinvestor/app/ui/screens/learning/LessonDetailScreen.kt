package com.smartinvestor.app.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.components.PremiumButton
import com.smartinvestor.app.ui.theme.*

data class LessonContent(
    val id: String,
    val title: String,
    val category: String,
    val readTimeMinutes: Int,
    val summary: String,
    val sections: List<Pair<String, String>>
)

object LessonCatalog {
    val lessons = mapOf(
        "lesson_1" to LessonContent(
            id = "lesson_1",
            title = "Introduction to Indian Stock Markets",
            category = "Basics",
            readTimeMinutes = 5,
            summary = "Understand how the National Stock Exchange (NSE) and Bombay Stock Exchange (BSE) facilitate capital allocation for Indian businesses.",
            sections = listOf(
                "What is a Share?" to "When you purchase a share of a company (like Reliance, TCS, or Infosys), you own a fractional claim on the company's assets and future earnings. Companies issue shares via Initial Public Offerings (IPOs) to raise growth capital.",
                "Primary vs Secondary Market" to "The primary market is where new securities are created and sold for the first time via IPOs. The secondary market (NSE/BSE) is where investors trade existing shares amongst themselves with high liquidity.",
                "How Prices Are Discovered" to "Share prices move based on continuous continuous double auctions: buyers place bids (highest price willing to pay) and sellers place asks (lowest price willing to accept). When bid meets ask, a transaction executes."
            )
        ),
        "lesson_2" to LessonContent(
            id = "lesson_2",
            title = "Reading Candlestick Patterns",
            category = "Technical Analysis",
            readTimeMinutes = 7,
            summary = "Master candlestick mechanics, wick interpretations, and key reversal patterns.",
            sections = listOf(
                "Anatomy of a Candlestick" to "Each candlestick captures four key prices: Open, High, Low, and Close (OHLC). The rectangular body shows the distance between Open and Close. The upper and lower wicks (shadows) show extreme highs and lows.",
                "Green vs Red Candles" to "A green candle indicates bullish sentiment where buyers drove the close higher than the open. A red candle indicates bearish pressure where sellers forced the close below the open.",
                "Key Reversal Patterns" to "Hammers and Bullish Engulfing patterns near support zones often signify that buyers rejected lower prices and momentum is shifting upwards."
            )
        ),
        "lesson_3" to LessonContent(
            id = "lesson_3",
            title = "Fundamental Analysis & Key Ratios",
            category = "Value Investing",
            readTimeMinutes = 6,
            summary = "Evaluate company health using Price-to-Earnings (P/E), Return on Equity (ROE), and Debt levels.",
            sections = listOf(
                "Price to Earnings (P/E) Ratio" to "P/E compares a company's market price to its earnings per share (EPS). A lower P/E relative to sector peers might suggest undervalued earnings, while a high P/E implies investors expect high future growth.",
                "Debt to Equity (D/E)" to "Measures financial leverage. A D/E ratio under 1.0 indicates a conservative balance sheet that can survive economic downturns without insolvency risks.",
                "Return on Equity (ROE)" to "Measures how efficiently management generates profits from shareholders' equity capital. Sustained ROE above 15% is an indicator of strong competitive moats."
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lessonId: String,
    onBack: () -> Unit,
    onTakeQuiz: (String) -> Unit
) {
    val lesson = LessonCatalog.lessons[lessonId] ?: LessonCatalog.lessons["lesson_1"]!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = PremiumPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(lesson.category) }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Schedule, null, tint = PremiumPurpleLight, modifier = Modifier.size(16.dp))
                        Text("${lesson.readTimeMinutes} min read", style = MaterialTheme.typography.labelSmall, color = PremiumPurpleLight)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lesson.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Sections
            lesson.sections.forEachIndexed { idx, (header, body) ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${idx + 1}. $header",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            // Quiz Button
            PremiumButton(
                text = "Take Knowledge Check Quiz",
                onClick = { onTakeQuiz(lesson.id) },
                icon = Icons.Rounded.Quiz
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
