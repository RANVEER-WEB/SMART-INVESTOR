package com.smartinvestor.app.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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
fun LearningScreen(onBack: () -> Unit) {
    val lessons = listOf(
        Triple("Introduction to Stock Market", "Learn the basics of investing", 0.85f),
        Triple("Reading Stock Charts", "Understand candlestick patterns", 0.60f),
        Triple("Fundamental Analysis", "Evaluate company financials", 0.30f),
        Triple("Technical Analysis", "Chart patterns & indicators", 0.0f),
        Triple("Risk Management", "Protect your investments", 0.0f),
        Triple("Mutual Funds & SIPs", "Passive investing strategies", 0.0f),
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Learning Academy", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), showGlow = true, glowColor = PremiumPurple) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.School, null, tint = PremiumPurple, modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Your Progress", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("2 of 6 lessons completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { 0.33f }, modifier = Modifier.fillMaxWidth(), color = PremiumPurple, trackColor = PremiumPurpleContainer)
                        }
                    }
                }
            }

            items(lessons.size) { index ->
                val (title, desc, progress) = lessons[index]
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = MaterialTheme.shapes.small, color = if (progress > 0) PremiumPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                if (progress >= 1f) Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen)
                                else Icon(Icons.Rounded.PlayCircle, null, tint = if (progress > 0) PremiumPurple else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (progress > 0 && progress < 1f) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = PremiumPurple, trackColor = PremiumPurpleContainer)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
