package com.airpods.manager.domain.model

data class UserPreferences(
    val lowBatteryThreshold: Int = 20,
    val lowBatteryNotificationsEnabled: Boolean = true,
    val autoStartMonitoring: Boolean = true,
    val showNotificationOnConnect: Boolean = true,
    val persistentNotification: Boolean = true
)
