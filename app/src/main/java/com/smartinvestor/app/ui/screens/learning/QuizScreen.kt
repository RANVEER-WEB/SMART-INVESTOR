package com.smartinvestor.app.ui.screens.learning

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
import androidx.compose.ui.unit.dp
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.components.PremiumButton
import com.smartinvestor.app.ui.components.SuccessGlow
import com.smartinvestor.app.ui.theme.*

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

object QuizCatalog {
    val quizzes = mapOf(
        "lesson_1" to listOf(
            QuizQuestion(
                question = "Where are existing shares traded among everyday retail investors with high liquidity?",
                options = listOf("Primary Market (IPO)", "Secondary Market (NSE / BSE)", "Government Treasury", "Private Trust"),
                correctIndex = 1
            ),
            QuizQuestion(
                question = "What does buying a share of a listed company give the shareholder?",
                options = listOf("A debt loan to the company", "Fractional ownership of assets and future earnings", "Guaranteed 20% fixed return", "Personal liability for corporate lawsuits"),
                correctIndex = 1
            ),
            QuizQuestion(
                question = "How are real-time share prices determined during trading hours?",
                options = listOf("Fixed daily by SEBI", "Continuous double auction between buyer bids and seller asks", "Decided by the company's CEO", "Set by the Reserve Bank of India"),
                correctIndex = 1
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    lessonId: String,
    onBack: () -> Unit,
    onQuizPassed: () -> Unit = {}
) {
    val questions = QuizCatalog.quizzes[lessonId] ?: QuizCatalog.quizzes["lesson_1"]!!
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson Knowledge Quiz", fontWeight = FontWeight.Bold) },
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
            if (!isSubmitted) {
                questions.forEachIndexed { qIdx, q ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Question ${qIdx + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PremiumPurpleLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            q.options.forEachIndexed { optIdx, optText ->
                                val isSelected = selectedAnswers[qIdx] == optIdx
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (isSelected) PremiumPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAnswers[qIdx] = optIdx }
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
                                            onClick = { selectedAnswers[qIdx] = optIdx },
                                            colors = RadioButtonDefaults.colors(selectedColor = PremiumPurple)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = optText, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                PremiumButton(
                    text = "Submit Quiz",
                    onClick = {
                        var correctCount = 0
                        questions.forEachIndexed { idx, q ->
                            if (selectedAnswers[idx] == q.correctIndex) correctCount++
                        }
                        score = correctCount
                        isSubmitted = true
                        if (correctCount >= 2) onQuizPassed()
                    },
                    enabled = selectedAnswers.size == questions.size,
                    icon = Icons.Rounded.DoneAll
                )
            } else {
                val passed = score >= 2
                if (passed) {
                    SuccessGlow(
                        message = "Quiz Passed! Score: $score / ${questions.size}",
                        subtitle = "Great job! You have demonstrated core comprehension of this financial lesson."
                    )
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = DangerRed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Icon(Icons.Rounded.HighlightOff, null, tint = DangerRed, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Score: $score / ${questions.size}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Review the lesson material and try again to unlock the mastery badge.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                PremiumButton(
                    text = if (passed) "Return to Academy" else "Try Again",
                    onClick = {
                        if (passed) onBack() else {
                            selectedAnswers.clear()
                            isSubmitted = false
                        }
                    },
                    icon = if (passed) Icons.Rounded.School else Icons.Rounded.Refresh
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
