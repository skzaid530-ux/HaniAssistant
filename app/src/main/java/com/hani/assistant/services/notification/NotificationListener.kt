package com.hani.assistant.services.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import com.hani.assistant.repository.NotificationRepository
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // We can update repository
        sbn?.let {
            // We can collect notifications
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Update
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val active = activeNotifications
        notificationRepository.updateNotifications(active)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }
}
