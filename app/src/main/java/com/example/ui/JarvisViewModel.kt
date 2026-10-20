package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActionPayload
import com.example.data.ActionType
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.DeviceStatus
import com.example.data.InstalledApp
import com.example.data.IntelHeadline
import com.example.data.JarvisState
import com.example.data.Reminder
import com.example.system.DeviceController
import com.example.system.JarvisBrain
import com.example.system.JarvisSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "JarvisViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val reminderDao = database.reminderDao()

    val deviceController = DeviceController(application)
    val speechManager = JarvisSpeechManager(application)
    val brain = JarvisBrain()

    private val _jarvisState = MutableStateFlow(JarvisState.STANDBY)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _deviceStatus = MutableStateFlow(deviceController.getDeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    val reminders: StateFlow<List<Reminder>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioAmplitude: StateFlow<Float> = speechManager.audioAmplitude
    val liveTranscript: StateFlow<String> = speechManager.liveTranscript

    private val _headlines = MutableStateFlow<List<IntelHeadline>>(emptyList())
    val headlines: StateFlow<List<IntelHeadline>> = _headlines.asStateFlow()

    init {
        val initialGreeting = ChatMessage(
            isUser = false,
            text = "SYSTEM READY: Aapka personal AI assistant. Bataye main kaise madad kar sakta hoon? Aap mujhe apps open karne, message bhejne, settings jaise volume/Wi-Fi adjust karne, aur call lagane ke liye bol sakte hain."
        )
        _messages.value = listOf(initialGreeting)

        viewModelScope.launch {
            _installedApps.value = deviceController.getInstalledApps()
        }

        _headlines.value = listOf(
            IntelHeadline("1", "Satellite uplink established: Orbiting constellation synchronization 100%.", "Sat-Link", "Just now", "INFO"),
            IntelHeadline("2", "AI neural core v10.5 initialized on Android platform with speech heuristics.", "Defense", "5m ago", "SUCCESS"),
            IntelHeadline("3", "Audio subsystem calibrated; volume level synchronized.", "Hardware", "12m ago", "INFO"),
            IntelHeadline("4", "Global cyber security protocols engaged across all comms channels.", "Security", "25m ago", "ALERT")
        )

        refreshDeviceStatus()

        viewModelScope.launch {
            speechManager.isSpeaking.collect { speaking ->
                if (speaking && _jarvisState.value != JarvisState.LISTENING) {
                    _jarvisState.value = JarvisState.SPEAKING
                } else if (!speaking && _jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.STANDBY
                }
            }
        }
    }

    fun refreshDeviceStatus() {
        _deviceStatus.value = deviceController.getDeviceStatus()
    }

    fun executeCommand(prompt: String) {
        if (prompt.isBlank()) return

        val userMsg = ChatMessage(isUser = true, text = prompt)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _jarvisState.value = JarvisState.THINKING
            delay(250)

            val response = brain.processCommand(prompt)

            val jarvisMsg = ChatMessage(
                isUser = false,
                text = response.displayText,
                action = response.action
            )
            _messages.value = _messages.value + jarvisMsg

            speechManager.speak(response.spokenText)

            response.action?.let { action ->
                _jarvisState.value = JarvisState.EXECUTING
                executeAction(action)
            }

            if (_jarvisState.value != JarvisState.SPEAKING) {
                _jarvisState.value = JarvisState.STANDBY
            }
            refreshDeviceStatus()
        }
    }

    fun executeAction(action: ActionPayload) {
        when (action.type) {
            ActionType.CALL_PHONE -> {
                deviceController.makeCall(action.target)
            }
            ActionType.SEND_WHATSAPP -> {
                deviceController.sendWhatsApp(action.target, action.details)
            }
            ActionType.SEND_SMS -> {
                deviceController.sendSms(action.target, action.details)
            }
            ActionType.OPEN_APP -> {
                val (found, _) = deviceController.launchAppByName(action.target)
                if (!found) {
                    deviceController.launchAppByPackage(action.target)
                }
            }
            ActionType.ADJUST_VOLUME -> {
                when (action.target) {
                    "increase" -> {
                        val newVol = deviceController.adjustVolume(true)
                        _deviceStatus.value = _deviceStatus.value.copy(volumePercent = newVol)
                    }
                    "decrease" -> {
                        val newVol = deviceController.adjustVolume(false)
                        _deviceStatus.value = _deviceStatus.value.copy(volumePercent = newVol)
                    }
                    else -> {
                        val pct = action.target.toIntOrNull() ?: 70
                        val newVol = deviceController.setVolumePercent(pct)
                        _deviceStatus.value = _deviceStatus.value.copy(volumePercent = newVol)
                    }
                }
                refreshDeviceStatus()
            }
            ActionType.TOGGLE_FLASHLIGHT -> {
                val turnOn = action.target.lowercase() == "on"
                deviceController.toggleFlashlight(turnOn)
                refreshDeviceStatus()
            }
            ActionType.OPEN_SETTINGS -> {
                deviceController.openSettings(action.target)
            }
            ActionType.SET_REMINDER -> {
                val parts = action.details.split("•")
                val date = parts.getOrNull(0)?.trim() ?: "Today"
                val time = parts.getOrNull(1)?.trim() ?: "05:00 PM"
                addReminder(action.target, time, date, "Voice", "HIGH")
            }
            ActionType.SYSTEM_DIAGNOSTIC -> {
                refreshDeviceStatus()
            }
            ActionType.NONE -> {}
        }
    }

    fun startVoiceListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            _jarvisState.value = JarvisState.STANDBY
            return
        }

        _jarvisState.value = JarvisState.LISTENING
        speechManager.startListening(
            onResult = { recognizedText ->
                _jarvisState.value = JarvisState.STANDBY
                executeCommand(recognizedText)
            },
            onError = { error ->
                Log.e(TAG, "Voice recognition error: $error")
                _jarvisState.value = JarvisState.STANDBY
            }
        )
    }

    fun toggleFlashlight() {
        val newState = deviceController.toggleFlashlight()
        refreshDeviceStatus()
        speechManager.speak(if (newState) "Flashlight turned on, sir." else "Flashlight turned off, sir.")
    }

    fun setVolume(percent: Int) {
        val newVol = deviceController.setVolumePercent(percent)
        _deviceStatus.value = _deviceStatus.value.copy(volumePercent = newVol)
        speechManager.speak("Volume set to $newVol percent, sir.")
    }

    fun adjustVolume(increase: Boolean) {
        val newVol = deviceController.adjustVolume(increase)
        _deviceStatus.value = _deviceStatus.value.copy(volumePercent = newVol)
    }

    fun toggleMute() {
        val newMute = !_deviceStatus.value.isAudioMuted
        _deviceStatus.value = _deviceStatus.value.copy(isAudioMuted = newMute)
        speechManager.setMuted(newMute)
    }

    fun speakText(text: String) {
        speechManager.speak(text)
    }

    fun addReminder(
        title: String,
        time: String,
        date: String,
        category: String = "Schedule",
        priority: String = "MEDIUM"
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                timeStr = time,
                dateStr = date,
                category = category,
                priority = priority
            )
            reminderDao.insertReminder(reminder)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.setCompleted(reminder.id, !reminder.isCompleted)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.deleteReminder(reminder)
        }
    }

    fun makeCall(target: String) {
        deviceController.makeCall(target)
    }

    fun sendSms(target: String, message: String) {
        deviceController.sendSms(target, message)
    }

    fun sendWhatsApp(target: String, message: String) {
        deviceController.sendWhatsApp(target, message)
    }

    fun openSetting(type: String) {
        deviceController.openSettings(type)
    }

    fun launchApp(packageName: String) {
        deviceController.launchAppByPackage(packageName)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
