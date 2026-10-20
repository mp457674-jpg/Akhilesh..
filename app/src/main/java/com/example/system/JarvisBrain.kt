package com.example.system

import com.example.data.ActionPayload
import com.example.data.ActionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BrainResponse(
    val spokenText: String,
    val displayText: String,
    val action: ActionPayload? = null
)

class JarvisBrain {

    fun processCommand(input: String): BrainResponse {
        val raw = input.trim()
        val text = raw.lowercase()

        // 1. Volume Adjustments & Mute Controls
        if (text.contains("volume") || text.contains("awaz") || text.contains("sound") ||
            text.contains("mute") || text.contains("unmute") || text.contains("chup") || text.contains("shant")
        ) {
            val isMute = text.contains("mute") || text.contains("chup") || text.contains("shant")
            val isUnmute = text.contains("unmute")
            val isIncrease = text.contains("increase") || text.contains("badhao") || text.contains("up") ||
                    text.contains("high") || text.contains("loud") || text.contains("zyada")
            val isDecrease = text.contains("decrease") || text.contains("kam") || text.contains("down") ||
                    text.contains("low") || text.contains("slow") || text.contains("ghatao")
            val percentMatch = Regex("(\\d{1,3})\\s*(%|percent)").find(text)

            when {
                percentMatch != null -> {
                    val pct = percentMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 70
                    return BrainResponse(
                        spokenText = "Setting volume level to $pct percent, sir.",
                        displayText = "Adjusting stream volume to $pct%.",
                        action = ActionPayload(
                            type = ActionType.ADJUST_VOLUME,
                            target = pct.toString(),
                            details = "Volume -> $pct%"
                        )
                    )
                }
                text.contains("max") || text.contains("full") -> {
                    return BrainResponse(
                        spokenText = "Setting volume to maximum, sir.",
                        displayText = "Volume set to 100%.",
                        action = ActionPayload(
                            type = ActionType.ADJUST_VOLUME,
                            target = "100",
                            details = "Volume -> 100%"
                        )
                    )
                }
                isMute -> {
                    return BrainResponse(
                        spokenText = "Audio output muted, sir.",
                        displayText = "Audio muted.",
                        action = ActionPayload(
                            type = ActionType.ADJUST_VOLUME,
                            target = "0",
                            details = "Volume -> Muted"
                        )
                    )
                }
                isUnmute -> {
                    return BrainResponse(
                        spokenText = "Audio output unmuted, sir.",
                        displayText = "Audio unmuted.",
                        action = ActionPayload(
                            type = ActionType.ADJUST_VOLUME,
                            target = "60",
                            details = "Volume -> 60%"
                        )
                    )
                }
                isIncrease -> {
                    return BrainResponse(
                        spokenText = "Raising audio volume, sir.",
                        displayText = "Increasing audio volume.",
                        action = ActionPayload(
                            type = ActionType.ADJUST_VOLUME,
                            target = "increase",
                            details = "Volume (+)"
                        )
                    )
                }
                isDecrease -> {
                    return BrainResponse(
                        spokenText = "Lowering audio volume, sir.",
                        displayText = "Decreasing audio volume.",
                        action = ActionPayload(
                            type = ActionType.ADJUST_VOLUME,
                            target = "decrease",
                            details = "Volume (-)"
                        )
                    )
                }
            }
        }

        // 2. Phone Call Intent
        if (text.startsWith("call ") || text.contains(" ko call") || text.contains(" phone karo") ||
            text.contains(" call lagao") || text.startsWith("dial ") || text.contains(" ko dial") ||
            text.contains("phone lagao") || text.startsWith("make a call") || text.startsWith("phone ")
        ) {
            val target = extractCallTarget(raw)
            return BrainResponse(
                spokenText = "Yes sir, placing a call to $target right away.",
                displayText = "Initiating voice call to $target...",
                action = ActionPayload(
                    type = ActionType.CALL_PHONE,
                    target = target,
                    details = "Dialing $target"
                )
            )
        }

        // 3. WhatsApp Message
        if (text.contains("whatsapp") || text.contains("whats app")) {
            val (recipient, message) = extractMessageComponents(raw)
            return BrainResponse(
                spokenText = "Opening WhatsApp to send message to $recipient.",
                displayText = "Composing WhatsApp message for $recipient: \"$message\"",
                action = ActionPayload(
                    type = ActionType.SEND_WHATSAPP,
                    target = recipient,
                    details = message
                )
            )
        }

        // 4. SMS Message
        if (text.contains("message") || text.contains("sms") || text.contains("sandesh") || text.startsWith("text ")) {
            val (recipient, message) = extractMessageComponents(raw)
            return BrainResponse(
                spokenText = "Preparing SMS message for $recipient.",
                displayText = "Composing SMS to $recipient: \"$message\"",
                action = ActionPayload(
                    type = ActionType.SEND_SMS,
                    target = recipient,
                    details = message
                )
            )
        }

        // 5. Flashlight / Torch
        if (text.contains("torch") || text.contains("flashlight") || text.contains("flash light")) {
            val turnOff = text.contains("off") || text.contains("band") || text.contains("bujha") || text.contains("stop")
            val state = if (turnOff) "off" else "on"
            return BrainResponse(
                spokenText = "Toggling flashlight $state, sir.",
                displayText = "Flashlight switched $state.",
                action = ActionPayload(
                    type = ActionType.TOGGLE_FLASHLIGHT,
                    target = state,
                    details = "Flashlight $state"
                )
            )
        }

        // 6. Wi-Fi & Device Settings Management
        if (text.contains("wifi") || text.contains("wi-fi") || text.contains("bluetooth") ||
            text.contains("brightness") || text.contains("display") || text.contains("setting") ||
            text.contains("battery") || text.contains("internet")
        ) {
            val settingType = when {
                text.contains("wifi") || text.contains("wi-fi") || text.contains("internet") -> "wifi"
                text.contains("bluetooth") || text.contains("bt") -> "bluetooth"
                text.contains("display") || text.contains("brightness") || text.contains("screen") -> "display"
                text.contains("battery") || text.contains("charging") -> "battery"
                text.contains("apps") || text.contains("application") -> "apps"
                else -> "general"
            }
            return BrainResponse(
                spokenText = "Opening $settingType control panel, sir.",
                displayText = "Accessing device settings: $settingType",
                action = ActionPayload(
                    type = ActionType.OPEN_SETTINGS,
                    target = settingType,
                    details = "Managing $settingType settings"
                )
            )
        }

        // 7. Open Apps (Fast Recognition)
        if (text.startsWith("open ") || text.startsWith("launch ") || text.startsWith("start ") ||
            text.contains(" kholo") || text.contains(" chalu karo") || text.contains(" start karo") ||
            text.contains(" open karo") || text.contains("play ")
        ) {
            val appName = extractAppName(raw)
            return BrainResponse(
                spokenText = "Opening $appName, sir.",
                displayText = "Launching application: $appName",
                action = ActionPayload(
                    type = ActionType.OPEN_APP,
                    target = appName,
                    details = "Opening $appName"
                )
            )
        }

        // 8. Schedule & Reminders
        if (text.contains("remind") || text.contains("reminder") || text.contains("schedule") ||
            text.contains("yaad dilana") || text.contains("meeting") || text.contains("appointment")
        ) {
            val reminderTitle = extractReminderDetails(raw)
            val timeStr = extractTimeStr(raw)
            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
            return BrainResponse(
                spokenText = "Reminder set for $reminderTitle at $timeStr, sir.",
                displayText = "Reminder programmed: \"$reminderTitle\" at $timeStr ($dateStr)",
                action = ActionPayload(
                    type = ActionType.SET_REMINDER,
                    target = reminderTitle,
                    details = "$dateStr • $timeStr"
                )
            )
        }

        // 9. System Diagnostics / Status
        if (text.contains("status") || text.contains("diagnostic") || text.contains("telemetry") ||
            text.contains("battery kitni") || text.contains("phone check") || text.contains("system ready")
        ) {
            return BrainResponse(
                spokenText = "All primary protocols are operational. Power grid stable, memory capacity optimal.",
                displayText = "System Diagnostic: Online • CPU Nominal • All subroutines ready.",
                action = ActionPayload(
                    type = ActionType.SYSTEM_DIAGNOSTIC,
                    target = "diagnostics",
                    details = "Diagnostics completed"
                )
            )
        }

        // 10. Time & Date
        if (text.contains("time") || text.contains("samay") || text.contains("kitne baje") ||
            text.contains("date") || text.contains("tarikh") || text.contains("aaj kaun sa din")
        ) {
            val nowTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val nowDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
            return BrainResponse(
                spokenText = "It is currently $nowTime on $nowDate, sir.",
                displayText = "Current telemetry: $nowTime | $nowDate",
                action = null
            )
        }

        // 11. Identity & Greeting
        if (text.contains("who are you") || text.contains("tum kaun ho") || text.contains("kaun ho") ||
            text.contains("identity") || text.contains("what can you do") || text.contains("kya kar sakte")
        ) {
            return BrainResponse(
                spokenText = "I am JARVIS, your personal artificial intelligence assistant. I can control your phone, initiate calls, send messages, launch applications, toggle settings like volume and Wi-Fi, and organize your daily schedule.",
                displayText = "JARVIS OS v2.4 Online.\nCapabilities:\n• Voice Phone Calls & Messaging\n• App Launcher\n• Volume & Wi-Fi Settings\n• Torch / Hardware Control\n• Daily Schedule & Reminders",
                action = null
            )
        }

        if (text.contains("hello") || text.contains("hey") || text.contains("hi") ||
            text.contains("namaste") || text.contains("kaisa hai") || text.contains("how are you")
        ) {
            return BrainResponse(
                spokenText = "Hello sir! JARVIS voice recognition is active and listening. How may I assist you?",
                displayText = "SYSTEM READY: Aapka personal AI assistant. Bataye main kaise madad kar sakta hoon?",
                action = null
            )
        }

        if (text.contains("thank") || text.contains("shukriya") || text.contains("dhanyawad")) {
            return BrainResponse(
                spokenText = "Always at your service, sir. It is my pleasure.",
                displayText = "Protocol complete. Standing by for further commands, sir.",
                action = null
            )
        }

        // Fallback Intelligent Response
        return BrainResponse(
            spokenText = "Acknowledged, sir. Command received: $raw.",
            displayText = "Directive registered: \"$raw\". All systems standing by.",
            action = null
        )
    }

    private fun extractCallTarget(raw: String): String {
        var s = raw
        val prefixes = listOf(
            "make a phone call to ", "make a call to ", "call ", "dial ",
            "phone lagao ", "phone karo ", "phone ", "call to "
        )
        for (p in prefixes) {
            if (s.lowercase().startsWith(p)) {
                s = s.substring(p.length)
                break
            }
        }
        val suffixes = listOf(
            " ko call lagao", " ko call karo", " ko phone karo",
            " ko dial karo", " call lagao", " call karo"
        )
        for (suffix in suffixes) {
            if (s.lowercase().endsWith(suffix)) {
                s = s.substring(0, s.length - suffix.length)
                break
            }
        }
        return s.trim().ifEmpty { "100" }
    }

    private fun extractMessageComponents(raw: String): Pair<String, String> {
        val lower = raw.lowercase()
        var recipient = "Friend"
        var message = "Hello from JARVIS"

        if (lower.contains(" to ") && (lower.contains(" that ") || lower.contains(" saying ") || lower.contains(" : "))) {
            val toIndex = lower.indexOf(" to ") + 4
            val splitKeyword = when {
                lower.contains(" that ") -> " that "
                lower.contains(" saying ") -> " saying "
                else -> " : "
            }
            val splitIndex = lower.indexOf(splitKeyword)
            if (splitIndex > toIndex) {
                recipient = raw.substring(toIndex, splitIndex).trim()
                message = raw.substring(splitIndex + splitKeyword.length).trim()
                return Pair(recipient, message)
            }
        }

        if (lower.contains(" to ")) {
            val afterTo = raw.substring(lower.indexOf(" to ") + 4).trim()
            val words = afterTo.split(" ", limit = 2)
            recipient = words.firstOrNull() ?: "Friend"
            if (words.size > 1) {
                message = words[1]
            }
        } else if (lower.contains(" ko ")) {
            val parts = raw.split(Regex("(?i)\\sko\\s"), limit = 2)
            recipient = parts[0].replace(Regex("(?i)(whatsapp|message|sms|par)"), "").trim()
            if (parts.size > 1) {
                message = parts[1].replace(Regex("(?i)(bhejo|karo|send karo|message|par)"), "").trim()
            }
        } else if (lower.startsWith("text ") || lower.startsWith("sms ") || lower.startsWith("whatsapp ")) {
            val words = raw.split(" ", limit = 3)
            if (words.size >= 2) {
                recipient = words[1]
                if (words.size >= 3) message = words[2]
            }
        } else {
            val digits = raw.filter { it.isDigit() || it == '+' }
            if (digits.length >= 5) {
                recipient = digits
            }
        }
        if (recipient.isBlank()) recipient = "Friend"
        if (message.isBlank()) message = "Hello from JARVIS"
        return Pair(recipient, message)
    }

    private fun extractAppName(raw: String): String {
        var s = raw
        val prefixes = listOf("open ", "launch ", "start ", "play ")
        for (p in prefixes) {
            if (s.lowercase().startsWith(p)) {
                s = s.substring(p.length)
                break
            }
        }
        val suffixes = listOf(" kholo", " chalu karo", " start karo", " open karo", " app")
        for (suffix in suffixes) {
            if (s.lowercase().endsWith(suffix)) {
                s = s.substring(0, s.length - suffix.length)
                break
            }
        }
        return s.trim().ifEmpty { "YouTube" }
    }

    private fun extractReminderDetails(raw: String): String {
        var clean = raw
        val removePhrases = listOf(
            "remind me to ", "remind me ", "reminder for ", "schedule a ",
            "schedule ", "yaad dilana ki ", "yaad dilana "
        )
        for (phrase in removePhrases) {
            if (clean.lowercase().startsWith(phrase)) {
                clean = clean.substring(phrase.length)
                break
            }
        }
        val atIndex = clean.lowercase().lastIndexOf(" at ")
        if (atIndex > 0) {
            clean = clean.substring(0, atIndex)
        }
        val bajeIndex = clean.lowercase().lastIndexOf(" baje")
        if (bajeIndex > 0) {
            clean = clean.substring(0, bajeIndex)
        }
        return clean.trim().ifEmpty { "Important Task" }
    }

    private fun extractTimeStr(raw: String): String {
        val lower = raw.lowercase()
        val timeRegex = Regex("(\\d{1,2}(:\\d{2})?\\s*(am|pm|baje)?)")
        val match = timeRegex.find(lower)
        if (match != null && match.value.isNotBlank()) {
            return match.value.trim()
        }
        return "06:00 PM"
    }
}
