package com.smartinvestor.app.ui.screens.ai_advisor

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartinvestor.app.ui.components.GlassCard
import com.smartinvestor.app.ui.theme.*
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = PremiumPurple)
                    Text("AI Advisor", fontWeight = FontWeight.Bold)
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
            actions = { IconButton(onClick = { viewModel.resetChat() }) { Icon(Icons.Rounded.Refresh, "Reset") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg -> ChatBubble(message = msg) }
            if (isThinking) {
                item { AiThinkingOrb() }
            }
        }

        // Input Bar
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            showGlow = true,
            glowColor = PremiumPurple,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about stocks...") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                    singleLine = false,
                    maxLines = 3,
                )
                FilledIconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isThinking,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = PremiumPurple)
                ) {
                    Icon(Icons.Rounded.Send, "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) PremiumPurple else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        GlassCard(
            modifier = Modifier.widthIn(max = 300.dp),
            showGlow = message.isUser,
            glowColor = if (message.isUser) PremiumPurple else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun AiThinkingOrb() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orbScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orbAlpha"
    )

    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Rounded.AutoAwesome,
            null,
            tint = PremiumPurple.copy(alpha = alpha),
            modifier = Modifier.size(24.dp).scale(scale)
        )
        Text("AI is thinking...", style = MaterialTheme.typography.bodySmall, color = PremiumPurpleLight)
    }
}
