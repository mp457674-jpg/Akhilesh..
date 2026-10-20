package com.example.data

import java.util.UUID

enum class JarvisState {
    STANDBY,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING
}

enum class ActionType {
    NONE,
    CALL_PHONE,
    SEND_SMS,
    SEND_WHATSAPP,
    OPEN_APP,
    TOGGLE_FLASHLIGHT,
    OPEN_SETTINGS,
    ADJUST_VOLUME,
    SET_REMINDER,
    SYSTEM_DIAGNOSTIC
}

data class ActionPayload(
    val type: ActionType = ActionType.NONE,
    val target: String = "",
    val details: String = "",
    val success: Boolean = true,
    val executed: Boolean = false
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val action: ActionPayload? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceStatus(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val networkStatus: String = "Wi-Fi Connected",
    val isFlashlightOn: Boolean = false,
    val availableRamMb: Long = 3200,
    val totalRamMb: Long = 8192,
    val freeStorageGb: Long = 48,
    val isAudioMuted: Boolean = false,
    val volumePercent: Int = 70
)

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val isCommon: Boolean = false
)

data class IntelHeadline(
    val id: String,
    val title: String,
    val category: String,
    val timeAgo: String,
    val severity: String = "INFO" // INFO, WARN, SUCCESS
)
