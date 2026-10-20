package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisTopBar
import com.example.ui.screens.AppControlScreen
import com.example.ui.screens.HudCommandScreen
import com.example.ui.screens.SatLinkIntelScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.VoiceTerminalScreen
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted

enum class JarvisNavTab(val title: String, val icon: ImageVector) {
    HUD("CORE", Icons.Default.Memory),
    AGENT("VOICE", Icons.Default.RecordVoiceOver),
    CONTROL("APPS", Icons.Default.PhoneAndroid),
    SCHEDULE("AGENDA", Icons.Default.CalendarMonth),
    INTEL("INTEL", Icons.Default.Public)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                JarvisApp()
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(JarvisNavTab.HUD.ordinal) }

    val jarvisState by viewModel.jarvisState.collectAsStateWithLifecycle()
    val deviceStatus by viewModel.deviceStatus.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val audioAmplitude by viewModel.audioAmplitude.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val headlines by viewModel.headlines.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.refreshDeviceStatus()
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        topBar = {
            JarvisTopBar(
                state = jarvisState,
                deviceStatus = deviceStatus,
                onToggleMute = { viewModel.toggleMute() },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = CardNavyBorder)
                    .navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                JarvisNavTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) CyberCyan else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CyberCyan else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = CyberCyan,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepSpace)
        ) {
            when (selectedTab) {
                JarvisNavTab.HUD.ordinal -> {
                    HudCommandScreen(
                        state = jarvisState,
                        deviceStatus = deviceStatus,
                        audioAmplitude = audioAmplitude,
                        liveTranscript = liveTranscript,
                        onActivateVoice = {
                            viewModel.startVoiceListening()
                            selectedTab = JarvisNavTab.AGENT.ordinal
                        },
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        onAdjustVolume = { inc -> viewModel.adjustVolume(inc) },
                        onOpenSettingsTab = { selectedTab = JarvisNavTab.CONTROL.ordinal },
                        onOpenScheduleTab = { selectedTab = JarvisNavTab.SCHEDULE.ordinal },
                        onOpenAppsTab = { selectedTab = JarvisNavTab.CONTROL.ordinal },
                        onSelectSuggestion = { command ->
                            viewModel.executeCommand(command)
                            selectedTab = JarvisNavTab.AGENT.ordinal
                        }
                    )
                }

                JarvisNavTab.AGENT.ordinal -> {
                    VoiceTerminalScreen(
                        messages = messages,
                        state = jarvisState,
                        audioAmplitude = audioAmplitude,
                        liveTranscript = liveTranscript,
                        onSendCommand = { prompt -> viewModel.executeCommand(prompt) },
                        onActivateVoice = { viewModel.startVoiceListening() },
                        onSpeakMessage = { text -> viewModel.speakText(text) },
                        onExecuteAction = { action -> viewModel.executeAction(action) }
                    )
                }

                JarvisNavTab.CONTROL.ordinal -> {
                    AppControlScreen(
                        installedApps = installedApps,
                        onLaunchApp = { pkg -> viewModel.launchApp(pkg) },
                        onMakeCall = { target -> viewModel.makeCall(target) },
                        onSendSms = { target, msg -> viewModel.sendSms(target, msg) },
                        onSendWhatsApp = { target, msg -> viewModel.sendWhatsApp(target, msg) },
                        onOpenSetting = { type -> viewModel.openSetting(type) },
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        isFlashlightOn = deviceStatus.isFlashlightOn
                    )
                }

                JarvisNavTab.SCHEDULE.ordinal -> {
                    ScheduleScreen(
                        reminders = reminders,
                        onToggleReminder = { item -> viewModel.toggleReminder(item) },
                        onDeleteReminder = { item -> viewModel.deleteReminder(item) },
                        onAddReminder = { title, time, date, category, priority ->
                            viewModel.addReminder(title, time, date, category, priority)
                        }
                    )
                }

                JarvisNavTab.INTEL.ordinal -> {
                    SatLinkIntelScreen(
                        headlines = headlines
                    )
                }
            }
        }
    }
}
