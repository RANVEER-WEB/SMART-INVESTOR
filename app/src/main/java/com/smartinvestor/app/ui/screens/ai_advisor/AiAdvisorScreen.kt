package com.smartinvestor.app.ui.screens.ai_advisor

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(
    onBack: () -> Unit,
    viewModel: AiAdvisorViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SignatureAiOrbIcon(isThinking = isThinking)
                    Column {
                        Text("Smart Investor AI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Powered by Gemini 2.0 Flash", style = MaterialTheme.typography.labelSmall, color = PremiumPurpleLight)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.resetChat() }) {
                    Icon(Icons.Rounded.Refresh, "Reset Chat")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(msg.content))
                    }
                )
            }
            if (isThinking) {
                item {
                    AiThinkingOrb()
                }
            }
        }

        // Suggestion Chips
        if (!isThinking) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.suggestionChips) { chipText ->
                    SuggestionChip(
                        onClick = {
                            viewModel.sendMessage(chipText)
                        },
                        label = { Text(chipText, style = MaterialTheme.typography.labelSmall) },
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = PremiumPurple.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // Input Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            showGlow = true,
            glowColor = PremiumPurple
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about any stock, indicator, or strategy...") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                    singleLine = false,
                    maxLines = 3,
                )
                FilledIconButton(
                    onClick = {
                        val text = inputText
                        inputText = ""
                        viewModel.sendMessage(text)
                    },
                    enabled = inputText.isNotBlank() && !isThinking,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = PremiumPurple)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SignatureAiOrbIcon(isThinking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulseHeader")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isThinking) 0.85f else 0.95f,
        targetValue = if (isThinking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 600 else 1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headerOrbScale"
    )

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        modifier = Modifier.size(32.dp).scale(pulseScale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumPurpleLight,
                            BadgeBlueNews,
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, onCopy: () -> Unit) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) PremiumPurple else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        GlassCard(
            modifier = Modifier.widthIn(max = 340.dp),
            showGlow = message.isUser,
            glowColor = PremiumPurple
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!message.isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = PremiumPurpleLight, modifier = Modifier.size(14.dp))
                            Text("AI Analysis", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PremiumPurpleLight)
                        }
                        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AiThinkingOrb() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orbScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orbAlpha"
    )

    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Rounded.AutoAwesome,
            null,
            tint = PremiumPurple.copy(alpha = alpha),
            modifier = Modifier.size(26.dp).scale(scale)
        )
        Text(
            text = "AI is evaluating market conditions & calculating scenarios...",
            style = MaterialTheme.typography.bodySmall,
            color = PremiumPurpleLight
        )
    }
}
