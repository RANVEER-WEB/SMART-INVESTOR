package com.smartinvestor.app.ui.screens.tools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinvestor.app.domain.model.RiskProfile
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.components.PremiumButton
import com.smartinvestor.app.ui.theme.*

data class RiskQuestion(
    val title: String,
    val description: String,
    val options: List<RiskOption>
)

data class RiskOption(
    val text: String,
    val score: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentScreen(
    onBack: () -> Unit,
    onComplete: (RiskProfile) -> Unit = {}
) {
    val questions = remember {
        listOf(
            RiskQuestion(
                title = "Primary Investment Goal",
                description = "What do you primarily want your investments to achieve?",
                options = listOf(
                    RiskOption("Capital Preservation & Fixed Income (Low risk)", 1),
                    RiskOption("Balanced Growth beating inflation steadily (Moderate)", 2),
                    RiskOption("Maximum Capital Appreciation with high volatility (Aggressive)", 3)
                )
            ),
            RiskQuestion(
                title = "Investment Horizon",
                description = "How long do you plan to keep your funds invested before needing them?",
                options = listOf(
                    RiskOption("Less than 1 year (Short term)", 1),
                    RiskOption("1 to 5 years (Medium term)", 2),
                    RiskOption("More than 5 to 10+ years (Long term)", 3)
                )
            ),
            RiskQuestion(
                title = "Reaction to Market Drops",
                description = "If your portfolio dropped by 20% in a broad market correction, you would:",
                options = listOf(
                    RiskOption("Panic and sell all holdings immediately to avoid further loss", 1),
                    RiskOption("Hold your positions patiently and wait for recovery", 2),
                    RiskOption("View it as a buying opportunity and deploy more capital", 3)
                )
            ),
            RiskQuestion(
                title = "Emergency Fund & Financial Cushion",
                description = "Do you have dedicated emergency savings outside your stock portfolio?",
                options = listOf(
                    RiskOption("No emergency fund / Less than 2 months living expenses", 1),
                    RiskOption("3 to 6 months expenses in safe liquid bank deposits", 2),
                    RiskOption("Over 6+ months expenses plus active secondary income streams", 3)
                )
            )
        )
    }

    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var resultProfile by remember { mutableStateOf<RiskProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Suitability Assessment", fontWeight = FontWeight.Bold) },
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
            if (resultProfile == null) {
                Text(
                    text = "Understand your risk tolerance to tailor AI recommendations and portfolio allocations responsibly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                questions.forEachIndexed { qIdx, question ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Question ${qIdx + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PremiumPurpleLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = question.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            question.options.forEachIndexed { optIdx, opt ->
                                val isSelected = selectedAnswers[qIdx] == opt.score
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (isSelected) PremiumPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAnswers[qIdx] = opt.score }
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) PremiumPurple else Color.Transparent,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedAnswers[qIdx] = opt.score },
                                            colors = RadioButtonDefaults.colors(selectedColor = PremiumPurple)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = opt.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val allAnswered = selectedAnswers.size == questions.size
                PremiumButton(
                    text = "Calculate Risk Profile",
                    onClick = {
                        val totalScore = selectedAnswers.values.sum()
                        val profile = when {
                            totalScore <= 6 -> RiskProfile.CONSERVATIVE
                            totalScore <= 9 -> RiskProfile.MODERATE
                            else -> RiskProfile.AGGRESSIVE
                        }
                        resultProfile = profile
                        onComplete(profile)
                    },
                    enabled = allAnswered,
                    icon = Icons.Rounded.Verified
                )
            } else {
                // ── Result Display ──
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    showGlow = true,
                    glowColor = when (resultProfile) {
                        RiskProfile.CONSERVATIVE -> BadgeBlueNews
                        RiskProfile.MODERATE -> SuccessGreen
                        RiskProfile.AGGRESSIVE -> DangerRed
                        null -> PremiumPurple
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = PremiumPurpleLight,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Your Profile: ${resultProfile?.name}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (resultProfile) {
                                RiskProfile.CONSERVATIVE ->
                                    "You prioritize capital protection. Recommended allocation: 60% Debt/FDs, 30% Large-Cap Equities, 10% Gold."
                                RiskProfile.MODERATE ->
                                    "You balance growth and stability. Recommended allocation: 65% Diversified Equities (Index/Flexi-cap), 25% Debt, 10% Gold/Commodities."
                                RiskProfile.AGGRESSIVE ->
                                    "You seek high long-term wealth creation and can withstand cyclical downturns. Recommended allocation: 80% Equities (Mid & Small caps included), 15% Debt, 5% Alternative."
                                null -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumButton(
                            text = "Apply to AI Copilot",
                            onClick = onBack,
                            icon = Icons.Rounded.CheckCircle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
