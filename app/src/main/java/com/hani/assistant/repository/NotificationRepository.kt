package com.hani.assistant.repository

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val context: Context
) {
    private val _notifications = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    val notifications: StateFlow<List<StatusBarNotification>> = _notifications

    fun updateNotifications(sbn: Array<StatusBarNotification>) {
        _notifications.value = sbn.toList()
    }

    fun getNotificationCount(): Int = _notifications.value.size
}