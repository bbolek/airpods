package com.airpods.manager.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.airpods.manager.MainActivity
import com.airpods.manager.R
import com.airpods.manager.domain.model.AirPodsDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MONITORING_NOTIFICATION_ID = 1001
        const val LOW_BATTERY_NOTIFICATION_BASE_ID = 2000
    }

    private val notificationManager = context.getSystemService<NotificationManager>()

    fun buildMonitoringNotification(device: AirPodsDevice?): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = device?.name ?: "AirPods Manager"
        val content = if (device != null) {
            val battery = device.battery
            buildString {
                if (battery.left != -1) append("L: ${battery.left}%  ")
                if (battery.right != -1) append("R: ${battery.right}%  ")
                if (battery.case != -1) append("Case: ${battery.case}%")
            }.trim().ifEmpty { "Scanning..." }
        } else {
            "Scanning for AirPods..."
        }

        return NotificationCompat.Builder(context, NotificationChannels.MONITORING_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showLowBatteryAlert(device: AirPodsDevice, whichEarbud: String, level: Int) {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.BATTERY_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${device.name} Battery Low")
            .setContentText("$whichEarbud battery is at $level%")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationId = LOW_BATTERY_NOTIFICATION_BASE_ID + device.address.hashCode()
        notificationManager?.notify(notificationId, notification)
    }

    fun updateMonitoringNotification(device: AirPodsDevice?) {
        val notification = buildMonitoringNotification(device)
        notificationManager?.notify(MONITORING_NOTIFICATION_ID, notification)
    }

    fun cancelAll() {
        notificationManager?.cancelAll()
    }
}
