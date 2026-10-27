package com.smartinvestor.app.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.smartinvestor.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    onBack: () -> Unit,
    onNavigateToLesson: (String) -> Unit = {}
) {
    val lessons = listOf(
        Triple("Introduction to Stock Market", "Learn the basics of Indian equity investing", "lesson_1"),
        Triple("Reading Stock Charts", "Understand candlestick patterns & volume", "lesson_2"),
        Triple("Fundamental Analysis", "Evaluate company balance sheets & P/E ratios", "lesson_3"),
        Triple("Technical Indicators", "Moving averages, RSI, and trend lines", "lesson_1"),
        Triple("Risk Management & Stop Losses", "Protect capital against market downturns", "lesson_2"),
        Triple("Mutual Funds & SIPs", "Systematic passive wealth compounding", "lesson_3"),
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Learning Academy", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = PremiumPurple) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.School, null, tint = PremiumPurpleLight, modifier = Modifier.size(36.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Your Academy Progress", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("Interactive chapters with knowledge check quizzes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { 0.40f }, modifier = Modifier.fillMaxWidth(), color = PremiumPurple, trackColor = PremiumPurpleContainer)
                        }
                    }
                }
            }

            items(lessons.size) { index ->
                val (title, desc, id) = lessons[index]
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLesson(id) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = PremiumPurple.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.PlayCircle, null, tint = PremiumPurpleLight)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
