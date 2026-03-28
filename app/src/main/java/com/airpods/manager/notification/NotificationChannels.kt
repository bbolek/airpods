package com.airpods.manager.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

object NotificationChannels {
    const val MONITORING_SERVICE = "airpods_monitoring"
    const val BATTERY_ALERTS = "airpods_battery_alerts"
    const val CONNECTION = "airpods_connection"

    fun createAll(context: Context) {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return

        val monitoringChannel = NotificationChannel(
            MONITORING_SERVICE,
            "AirPods Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification showing AirPods battery levels"
            setShowBadge(false)
        }

        val batteryAlertsChannel = NotificationChannel(
            BATTERY_ALERTS,
            "Battery Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when AirPods battery is low"
        }

        val connectionChannel = NotificationChannel(
            CONNECTION,
            "Connection Events",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications when AirPods connect or disconnect"
            setShowBadge(false)
        }

        notificationManager.createNotificationChannels(
            listOf(monitoringChannel, batteryAlertsChannel, connectionChannel)
        )
    }
}
