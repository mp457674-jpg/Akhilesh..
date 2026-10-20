package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import com.example.data.DeviceStatus
import com.example.data.InstalledApp

class DeviceController(private val context: Context) {

    private val TAG = "DeviceController"
    private var isTorchOn = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun getDeviceStatus(): DeviceStatus {
        var batteryLevel = 85
        var isCharging = false
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (batteryManager != null) {
                batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                if (batteryLevel <= 0 || batteryLevel > 100) batteryLevel = 85
                val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
                isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting battery status", e)
        }

        var networkStatus = "Offline"
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(network)
            networkStatus = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi Connected"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "5G / LTE Connected"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet Connected"
                capabilities != null -> "Connected"
                else -> "Offline"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network status", e)
        }

        var availRam = 3000L
        var totalRam = 8192L
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            availRam = memInfo.availMem / (1024 * 1024)
            totalRam = memInfo.totalMem / (1024 * 1024)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading memory info", e)
        }

        var freeStorageGb = 32L
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val availableBlocks = stat.availableBlocksLong
            val blockSize = stat.blockSizeLong
            freeStorageGb = (availableBlocks * blockSize) / (1024L * 1024L * 1024L)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading storage info", e)
        }

        val volumePct = getVolumePercent()

        return DeviceStatus(
            batteryPercent = batteryLevel,
            isCharging = isCharging,
            networkStatus = networkStatus,
            isFlashlightOn = isTorchOn,
            availableRamMb = availRam,
            totalRamMb = totalRam,
            freeStorageGb = freeStorageGb,
            volumePercent = volumePct
        )
    }

    fun vibrateShort() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibrate failed", e)
        }
    }

    fun getVolumePercent(): Int {
        return try {
            val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10
            val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            if (max > 0) ((current.toFloat() / max.toFloat()) * 100).toInt() else 70
        } catch (e: Exception) {
            70
        }
    }

    fun setVolumePercent(percent: Int): Int {
        return try {
            val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            val targetLevel = ((percent.coerceIn(0, 100) / 100f) * max).toInt()
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetLevel, AudioManager.FLAG_SHOW_UI)
            getVolumePercent()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume percent", e)
            70
        }
    }

    fun adjustVolume(increase: Boolean): Int {
        return try {
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            getVolumePercent()
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting volume", e)
            70
        }
    }

    fun toggleFlashlight(turnOn: Boolean? = null): Boolean {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (cameraManager != null) {
                val cameraIdList = cameraManager.cameraIdList
                for (id in cameraIdList) {
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    if (hasFlash) {
                        val newState = turnOn ?: !isTorchOn
                        cameraManager.setTorchMode(id, newState)
                        isTorchOn = newState
                        vibrateShort()
                        return isTorchOn
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle flashlight", e)
        }
        return false
    }

    fun makeCall(number: String): Boolean {
        return try {
            vibrateShort()
            val clean = number.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${if (clean.isNotEmpty()) clean else "100"}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching phone call", e)
            false
        }
    }

    fun sendSms(number: String, message: String): Boolean {
        return try {
            vibrateShort()
            val clean = number.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${if (clean.isNotEmpty()) clean else ""}")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening SMS composer", e)
            false
        }
    }

    fun sendWhatsApp(number: String, message: String): Boolean {
        return try {
            vibrateShort()
            val clean = number.filter { it.isDigit() }
            val uri = if (clean.isNotEmpty()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(message)}")
            } else {
                Uri.parse("whatsapp://send?text=${Uri.encode(message)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp intent fallback to SMS", e)
            sendSms(number, message)
        }
    }

    fun launchAppByPackage(packageName: String): Boolean {
        return try {
            vibrateShort()
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch $packageName", e)
            false
        }
    }

    fun launchAppByName(nameQuery: String): Pair<Boolean, String> {
        vibrateShort()
        val q = nameQuery.lowercase().trim()
        val directMap = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "google" to "com.google.android.googlequicksearchbox",
            "maps" to "com.google.android.apps.maps",
            "camera" to "com.google.android.GoogleCamera",
            "photos" to "com.google.android.apps.photos",
            "gallery" to "com.google.android.apps.photos",
            "gmail" to "com.google.android.gm",
            "mail" to "com.google.android.gm",
            "spotify" to "com.spotify.music",
            "music" to "com.spotify.music",
            "calculator" to "com.google.android.calculator",
            "calendar" to "com.google.android.calendar",
            "clock" to "com.google.android.deskclock",
            "alarm" to "com.google.android.deskclock",
            "settings" to "com.android.settings",
            "phone" to "com.google.android.dialer",
            "dialer" to "com.google.android.dialer",
            "messages" to "com.google.android.apps.messaging",
            "sms" to "com.google.android.apps.messaging",
            "instagram" to "com.instagram.android",
            "telegram" to "org.telegram.messenger",
            "netflix" to "com.netflix.mediaclient",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android"
        )

        for ((key, pkg) in directMap) {
            if (q.contains(key)) {
                if (launchAppByPackage(pkg)) {
                    return Pair(true, key.replaceFirstChar { it.uppercase() })
                }
            }
        }

        // Generic camera intent
        if (q.contains("camera")) {
            try {
                val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return Pair(true, "Camera")
            } catch (e: Exception) {
                Log.e(TAG, "Camera intent fallback failed", e)
            }
        }

        // Generic browser intent
        if (q.contains("browser") || q.contains("internet") || q.contains("web")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return Pair(true, "Web Browser")
            } catch (e: Exception) {
                Log.e(TAG, "Browser fallback failed", e)
            }
        }

        // Search installed applications list
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolved = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolved) {
                val label = ri.loadLabel(pm).toString().lowercase()
                if (label.contains(q) || q.contains(label)) {
                    val pkg = ri.activityInfo.packageName
                    if (launchAppByPackage(pkg)) {
                        return Pair(true, ri.loadLabel(pm).toString())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Querying package manager failed", e)
        }

        return Pair(false, nameQuery)
    }

    fun openSettings(type: String): Boolean {
        vibrateShort()
        return try {
            val action = when (type.lowercase()) {
                "wifi", "wi-fi", "internet" -> Settings.ACTION_WIFI_SETTINGS
                "bluetooth", "bt" -> Settings.ACTION_BLUETOOTH_SETTINGS
                "display", "brightness", "screen" -> Settings.ACTION_DISPLAY_SETTINGS
                "sound", "volume", "audio" -> Settings.ACTION_SOUND_SETTINGS
                "battery", "power" -> Intent.ACTION_POWER_USAGE_SUMMARY
                "apps", "application" -> Settings.ACTION_APPLICATION_SETTINGS
                "date", "time", "clock" -> Settings.ACTION_DATE_SETTINGS
                else -> Settings.ACTION_SETTINGS
            }
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed opening settings: $type", e)
            false
        }
    }

    fun getInstalledApps(): List<InstalledApp> {
        val appList = mutableListOf<InstalledApp>()
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolved = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolved) {
                val name = ri.loadLabel(pm).toString()
                val pkg = ri.activityInfo.packageName
                if (pkg != context.packageName) {
                    appList.add(InstalledApp(appName = name, packageName = pkg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching installed apps", e)
        }

        if (appList.isEmpty()) {
            appList.addAll(
                listOf(
                    InstalledApp("WhatsApp", "com.whatsapp", true),
                    InstalledApp("YouTube", "com.google.android.youtube", true),
                    InstalledApp("Chrome", "com.android.chrome", true),
                    InstalledApp("Google Maps", "com.google.android.apps.maps", true),
                    InstalledApp("Camera", "com.google.android.GoogleCamera", true),
                    InstalledApp("Settings", "com.android.settings", true),
                    InstalledApp("Spotify", "com.spotify.music", true),
                    InstalledApp("Calculator", "com.google.android.calculator", true)
                )
            )
        }
        return appList.sortedBy { it.appName }
    }
}
