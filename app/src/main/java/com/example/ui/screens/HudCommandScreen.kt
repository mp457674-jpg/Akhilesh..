package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.data.DeviceStatus
import com.example.data.JarvisState
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.CyberCard
import com.example.ui.components.HudNodeButton
import com.example.ui.components.VoiceWaveVisualizer
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HudCommandScreen(
    state: JarvisState,
    deviceStatus: DeviceStatus,
    audioAmplitude: Float,
    liveTranscript: String,
    onActivateVoice: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onAdjustVolume: (Boolean) -> Unit,
    onOpenSettingsTab: () -> Unit,
    onOpenScheduleTab: () -> Unit,
    onOpenAppsTab: () -> Unit,
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Core Arc Reactor Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = when (state) {
                JarvisState.LISTENING -> MatrixGreen.copy(alpha = 0.8f)
                JarvisState.THINKING -> NeonAmber.copy(alpha = 0.8f)
                JarvisState.SPEAKING -> ElectricBlue.copy(alpha = 0.8f)
                else -> CyberCyan.copy(alpha = 0.4f)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Header Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardNavyBorder.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (state) {
                                    JarvisState.LISTENING -> MatrixGreen
                                    JarvisState.THINKING -> NeonAmber
                                    JarvisState.SPEAKING -> ElectricBlue
                                    else -> CyberCyan
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (state) {
                            JarvisState.LISTENING -> if (liveTranscript.isNotBlank()) "\"$liveTranscript\"" else "LISTENING TO VOICE..."
                            JarvisState.THINKING -> "NEURAL PROCESSING..."
                            JarvisState.SPEAKING -> "JARVIS VOCALIZING..."
                            JarvisState.EXECUTING -> "EXECUTING PROTOCOL..."
                            JarvisState.STANDBY -> "SYSTEM READY"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (state) {
                            JarvisState.LISTENING -> MatrixGreen
                            JarvisState.THINKING -> NeonAmber
                            JarvisState.SPEAKING -> ElectricBlue
                            else -> CyberCyan
                        },
                        fontStyle = if (state == JarvisState.LISTENING && liveTranscript.isBlank()) FontStyle.Italic else FontStyle.Normal
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Central Animated Arc Reactor
                ArcReactorVisualizer(
                    size = 180.dp,
                    state = state,
                    audioAmplitude = audioAmplitude,
                    onClick = onActivateVoice
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Wave Visualizer
                VoiceWaveVisualizer(
                    isActive = state == JarvisState.LISTENING || state == JarvisState.SPEAKING,
                    activeColor = if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // "ACTIVATE VOICE" Button
                Button(
                    onClick = onActivateVoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state == JarvisState.LISTENING) MatrixGreen.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f),
                        contentColor = if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .border(
                            1.5.dp,
                            if (state == JarvisState.LISTENING) MatrixGreen else CyberCyan,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Activate voice",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state == JarvisState.LISTENING) "STOP LISTENING" else "ACTIVATE VOICE",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Telemetry Quick Grid: Torch, Volume, Wi-Fi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Flashlight Toggle Card
            CyberCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggleFlashlight),
                borderColor = if (deviceStatus.isFlashlightOn) MatrixGreen else CardNavyBorder
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Flashlight",
                            tint = if (deviceStatus.isFlashlightOn) MatrixGreen else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (deviceStatus.isFlashlightOn) MatrixGreen else TextMuted)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TORCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = if (deviceStatus.isFlashlightOn) "ACTIVE" else "OFFLINE",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (deviceStatus.isFlashlightOn) MatrixGreen else TextPrimary
                    )
                }
            }

            // Volume Control Card
            CyberCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAdjustVolume(true) }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = NeonAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "VOLUME",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "${deviceStatus.volumePercent}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            // Wi-Fi / Network Status Card
            CyberCard(
                modifier = Modifier
                    .weight(1.1f)
                    .clickable(onClick = onOpenSettingsTab)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Network",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "WI-FI / NET",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = deviceStatus.networkStatus,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Futuristic Subsystem Hub Nodes
        Text(
            text = "SYSTEM PROTOCOLS",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HudNodeButton(
                icon = Icons.Default.Memory,
                title = "Schedule & Memory",
                subtitle = "Manage agenda, reminders, and daily tasks",
                accentColor = CyberCyan,
                onClick = onOpenScheduleTab
            )
            HudNodeButton(
                icon = Icons.Default.Psychology,
                title = "Device Skills & Apps",
                subtitle = "Call contacts, launch apps, send WhatsApp & SMS",
                accentColor = NeonAmber,
                onClick = onOpenAppsTab
            )
            HudNodeButton(
                icon = Icons.Default.RecordVoiceOver,
                title = "Vocal Synthesis Engine",
                subtitle = "Bilingual Hindi & English conversational AI",
                accentColor = MatrixGreen,
                onClick = onActivateVoice
            )
            HudNodeButton(
                icon = Icons.Default.Settings,
                title = "Hardware & Settings Control",
                subtitle = "Wi-Fi, Bluetooth, Volume, Display and Battery settings",
                accentColor = ElectricBlue,
                onClick = onOpenSettingsTab
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Command Suggestions
        Text(
            text = "SUGGESTED VOICE DIRECTIVES",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Call Mummy",
                "Open YouTube",
                "Set volume to 80%",
                "Turn on Wi-Fi",
                "Torch on karo",
                "Send WhatsApp to Rahul",
                "Kal 8 AM meeting reminder",
                "Bluetooth settings open karo",
                "Battery status check",
                "Who are you?"
            )
            suggestions.forEach { command ->
                SuggestionChip(
                    onClick = { onSelectSuggestion(command) },
                    label = {
                        Text(
                            text = command,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = CardNavy
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = CardNavyBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
