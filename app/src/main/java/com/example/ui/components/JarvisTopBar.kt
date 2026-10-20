package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceStatus
import com.example.data.JarvisState
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun JarvisTopBar(
    state: JarvisState,
    deviceStatus: DeviceStatus,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CardNavy.copy(alpha = 0.9f))
            .border(width = 1.dp, color = CardNavyBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title and Protocol Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
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
                Column {
                    Text(
                        text = "J.A.R.V.I.S.",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan
                    )
                    Text(
                        text = "STARK AI • PROTOCOL 10.5",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Right: Telemetry & Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Battery Badge
                Row(
                    modifier = Modifier
                        .background(CardNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (deviceStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (deviceStatus.isCharging) MatrixGreen else CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${deviceStatus.batteryPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }

                // RAM Badge
                Box(
                    modifier = Modifier
                        .background(CardNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RAM ${(deviceStatus.availableRamMb / 1024f).let { "%.1f".format(it) }}G",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Voice Audio Mute Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (deviceStatus.isAudioMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = if (deviceStatus.isAudioMuted) "Unmute voice" else "Mute voice",
                        tint = if (deviceStatus.isAudioMuted) TextMuted else CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
