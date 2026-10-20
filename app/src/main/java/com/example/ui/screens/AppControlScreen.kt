package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InstalledApp
import com.example.ui.components.CyberCard
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

@Composable
fun AppControlScreen(
    installedApps: List<InstalledApp>,
    onLaunchApp: (String) -> Unit,
    onMakeCall: (String) -> Unit,
    onSendSms: (String, String) -> Unit,
    onSendWhatsApp: (String, String) -> Unit,
    onOpenSetting: (String) -> Unit,
    onToggleFlashlight: () -> Unit,
    isFlashlightOn: Boolean,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var quickCallTarget by remember { mutableStateOf("") }
    var quickSmsMessage by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section: Direct Phone Directives
        Text(
            text = "TELECOMMUNICATIONS OVERRIDE",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Quick Call & Messaging Console",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quickCallTarget,
                    onValueChange = { quickCallTarget = it },
                    label = { Text("Contact Name or Phone Number", color = TextMuted) },
                    placeholder = { Text("e.g. 9876543210 or Rahul", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quickSmsMessage,
                    onValueChange = { quickSmsMessage = it },
                    label = { Text("Message Body", color = TextMuted) },
                    placeholder = { Text("e.g. Call me back or reaching now", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onMakeCall(quickCallTarget) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixGreen.copy(alpha = 0.2f),
                            contentColor = MatrixGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MatrixGreen, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DIAL", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { onSendWhatsApp(quickCallTarget, quickSmsMessage) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixGreen.copy(alpha = 0.2f),
                            contentColor = MatrixGreen
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .border(1.dp, MatrixGreen, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WHATSAPP", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { onSendSms(quickCallTarget, quickSmsMessage) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan.copy(alpha = 0.2f),
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SMS", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: System Settings Override Panel
        Text(
            text = "HARDWARE & SETTINGS MATRIX",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingQuickTile(
                icon = Icons.Default.FlashlightOn,
                label = "Torch",
                stateText = if (isFlashlightOn) "ON" else "OFF",
                isActive = isFlashlightOn,
                onClick = onToggleFlashlight,
                modifier = Modifier.weight(1f)
            )
            SettingQuickTile(
                icon = Icons.Default.Wifi,
                label = "Wi-Fi",
                stateText = "Config",
                isActive = true,
                onClick = { onOpenSetting("wifi") },
                modifier = Modifier.weight(1f)
            )
            SettingQuickTile(
                icon = Icons.Default.Bluetooth,
                label = "Bluetooth",
                stateText = "Config",
                isActive = false,
                onClick = { onOpenSetting("bluetooth") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingQuickTile(
                icon = Icons.Default.VolumeUp,
                label = "Volume",
                stateText = "Audio",
                isActive = false,
                onClick = { onOpenSetting("sound") },
                modifier = Modifier.weight(1f)
            )
            SettingQuickTile(
                icon = Icons.Default.BrightnessMedium,
                label = "Display",
                stateText = "Screen",
                isActive = false,
                onClick = { onOpenSetting("display") },
                modifier = Modifier.weight(1f)
            )
            SettingQuickTile(
                icon = Icons.Default.BatteryFull,
                label = "Power",
                stateText = "Battery",
                isActive = false,
                onClick = { onOpenSetting("battery") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Installed App Launcher Grid
        Text(
            text = "INSTALLED APPLICATIONS DIRECTORY",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter applications by name...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberCyan)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CardNavyBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // App Items Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredApps.take(20).chunked(2).forEach { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowApps.forEach { app ->
                        CyberCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onLaunchApp(app.packageName) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CardNavyBorder),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.appName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CyberCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "LAUNCH",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MatrixGreen
                                    )
                                }
                            }
                        }
                    }
                    if (rowApps.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingQuickTile(
    icon: ImageVector,
    label: String,
    stateText: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.clickable(onClick = onClick),
        borderColor = if (isActive) MatrixGreen.copy(alpha = 0.5f) else CardNavyBorder
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MatrixGreen else CyberCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = stateText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) MatrixGreen else TextMuted
            )
        }
    }
}
