package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActionPayload
import com.example.data.ActionType
import com.example.data.ChatMessage
import com.example.data.JarvisState
import com.example.ui.components.VoiceWaveVisualizer
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoiceTerminalScreen(
    messages: List<ChatMessage>,
    state: JarvisState,
    audioAmplitude: Float,
    liveTranscript: String,
    onSendCommand: (String) -> Unit,
    onActivateVoice: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onExecuteAction: (ActionPayload) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        // Status & Audio Wave Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavy)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state == JarvisState.LISTENING) "LISTENING FOR INPUT" else "NEURAL AGENT READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan
                )
            }

            VoiceWaveVisualizer(
                isActive = state == JarvisState.LISTENING || state == JarvisState.SPEAKING,
                activeColor = if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan,
                barCount = 14,
                maxHeight = 20.dp
            )
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onSpeak = { onSpeakMessage(msg.text) },
                    onExecuteAction = onExecuteAction
                )
            }
        }

        // Real-Time Live Voice Recognition HUD (Pops up while user is speaking)
        if (state == JarvisState.LISTENING) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MatrixGreen.copy(alpha = 0.12f))
                    .border(1.dp, MatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MatrixGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "VOICE RECOGNITION STREAMING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MatrixGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (liveTranscript.isNotBlank()) "\"$liveTranscript\""
                            else "Listening... speak command (e.g. 'Call Rahul', 'Set volume 80%', 'Open YouTube')",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontStyle = if (liveTranscript.isBlank()) FontStyle.Italic else FontStyle.Normal
                        )
                    }
                    VoiceWaveVisualizer(
                        isActive = true,
                        activeColor = MatrixGreen,
                        barCount = 8,
                        maxHeight = 16.dp
                    )
                }
            }
        }

        // Bottom Input Terminal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavy)
                .border(width = 1.dp, color = CardNavyBorder)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Mic Button
                IconButton(
                    onClick = onActivateVoice,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (state == JarvisState.LISTENING) MatrixGreen.copy(alpha = 0.2f) else CardNavy)
                        .border(
                            1.dp,
                            if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Box
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Instruction de ya / command bole...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            onSendCommand(inputText)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendCommand(inputText)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.2f))
                        .border(1.dp, CyberCyan, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send command",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onExecuteAction: (ActionPayload) -> Unit
) {
    val isUser = message.isUser
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = if (isUser) "OPERATOR" else "J.A.R.V.I.S.",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) NeonAmber else CyberCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                .clip(RoundedCornerShape(if (isUser) 14.dp else 10.dp))
                .background(if (isUser) CardNavyBorder.copy(alpha = 0.5f) else CardNavy)
                .border(
                    width = 1.dp,
                    color = if (isUser) CyberCyan.copy(alpha = 0.4f) else CardNavyBorder,
                    shape = RoundedCornerShape(if (isUser) 14.dp else 10.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )

                message.action?.let { action ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavy)
                            .border(1.dp, MatrixGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (action.type) {
                                            ActionType.CALL_PHONE -> Icons.Default.Call
                                            ActionType.SEND_WHATSAPP, ActionType.SEND_SMS -> Icons.Default.Message
                                            ActionType.OPEN_APP -> Icons.Default.Launch
                                            ActionType.TOGGLE_FLASHLIGHT -> Icons.Default.FlashlightOn
                                            ActionType.OPEN_SETTINGS -> Icons.Default.Settings
                                            ActionType.ADJUST_VOLUME -> Icons.Default.VolumeUp
                                            ActionType.SET_REMINDER -> Icons.Default.NotificationsActive
                                            else -> Icons.Default.Launch
                                        },
                                        contentDescription = "Action",
                                        tint = MatrixGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = action.type.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MatrixGreen
                                    )
                                }

                                Button(
                                    onClick = { onExecuteAction(action) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MatrixGreen.copy(alpha = 0.2f),
                                        contentColor = MatrixGreen
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = "EXECUTE",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            if (action.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = action.details,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak message",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
