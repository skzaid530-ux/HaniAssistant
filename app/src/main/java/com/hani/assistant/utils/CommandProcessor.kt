package com.hani.assistant.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import com.hani.assistant.repository.AppIndexer
import com.hani.assistant.repository.ContactIndexer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandProcessor @Inject constructor(
    private val context: Context,
    private val appIndexer: AppIndexer,
    private val contactIndexer: ContactIndexer
) {

    suspend fun executeCommand(command: String): String? {
        val lower = command.lowercase().trim()

        // === Sleep command handled elsewhere ===

        // === Open app ===
        if (lower.contains("open ")) {
            val appName = command.substringAfter("open ").trim()
            val app = appIndexer.findApp(appName)
            if (app != null) {
                context.startActivity(context.packageManager.getLaunchIntentForPackage(app.packageName))
                return "Opening ${app.name}"
            } else {
                return "I couldn't find that app."
            }
        }

        // === Call ===
        if (lower.startsWith("call ")) {
            val nameOrNumber = command.substringAfter("call ").trim()
            val contact = contactIndexer.findContact(nameOrNumber)
            if (contact != null) {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.phoneNumber}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return "Calling ${contact.name}"
            } else if (nameOrNumber.matches(Regex("^[0-9+-]+$"))) {
                // Direct number
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$nameOrNumber"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return "Calling $nameOrNumber"
            } else {
                return "Could not find contact."
            }
        }

        // === Send message ===
        if (lower.startsWith("send message to ")) {
            val rest = command.substringAfter("send message to ").trim()
            val parts = rest.split(" message ")
            if (parts.size == 2) {
                val contactName = parts[0].trim()
                val msg = parts[1].trim()
                val contact = contactIndexer.findContact(contactName)
                if (contact != null) {
                    sendSms(contact.phoneNumber, msg)
                    return "Message sent to ${contact.name}"
                } else {
                    return "Contact not found."
                }
            }
            return "Please specify contact and message."
        }

        // === Settings ===
        if (lower.contains("open settings")) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return "Opening settings"
        }

        // === Bluetooth/WiFi ===
        if (lower.contains("turn bluetooth on")) {
            // requires permission
            // For simplicity, we'll just use intent
            try {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return "Opening Bluetooth settings"
            } catch (e: Exception) {
                return "Unable to control Bluetooth"
            }
        }
        if (lower.contains("turn bluetooth off")) {
            // similar
            return "Bluetooth control not fully implemented."
        }
        if (lower.contains("turn wifi on")) {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return "Opening WiFi settings"
        }

        // === Brightness ===
        if (lower.contains("increase brightness")) {
            // We'll just open display settings
            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return "Opening display settings"
        }

        // === Time ===
        if (lower.contains("what time is it")) {
            return "It is ${java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
        }

        // === Battery ===
        if (lower.contains("battery")) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val level = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
            return "Battery is at $level%"
        }

        // === Storage ===
        if (lower.contains("storage")) {
            val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
            val total = stat.totalBytes / (1024 * 1024 * 1024)
            val free = stat.availableBytes / (1024 * 1024 * 1024)
            return "Total storage: ${total}GB, Free: ${free}GB"
        }

        // === RAM ===
        if (lower.contains("ram")) {
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            am.getMemoryInfo(memoryInfo)
            val total = memoryInfo.totalMem / (1024 * 1024 * 1024)
            val avail = memoryInfo.availMem / (1024 * 1024 * 1024)
            return "RAM total: ${total}GB, Available: ${avail}GB"
        }

        // === Weather ===
        if (lower.contains("weather")) {
            // For simplicity, open a weather app or browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return "Opening weather"
        }

        // === Notifications ===
        if (lower.contains("notifications")) {
            val count = NotificationRepository.getNotificationCount(context)
            return "You have $count notifications. Open the notification panel to see them."
        }

        // === Search ===
        if (lower.startsWith("search ")) {
            val query = command.substringAfter("search ").trim()
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra(android.app.SearchManager.QUERY, query)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return "Searching for $query"
        }

        // === YouTube search ===
        if (lower.startsWith("search youtube ")) {
            val query = command.substringAfter("search youtube ").trim()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return "Searching YouTube"
        }

        // === Play music ===
        if (lower.contains("play music")) {
            // Launch music app
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search")) // example
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("spotify:search")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            return "Opening music"
        }

        // === Set alarm ===
        if (lower.startsWith("set alarm ")) {
            val time = command.substringAfter("set alarm ").trim()
            // Parse time, open alarm intent
            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "Alarm")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return "Opening alarm setup for $time"
        }

        // === Calculator ===
        if (lower.contains("open calculator")) {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("calculator://")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            return "Opening calculator"
        }

        // === Maps ===
        if (lower.contains("open maps")) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=loc")))
            return "Opening maps"
        }

        // === Not a direct command ===
        return null
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            Log.e("CommandProcessor", "SMS failed: ${e.message}")
        }
    }
}
