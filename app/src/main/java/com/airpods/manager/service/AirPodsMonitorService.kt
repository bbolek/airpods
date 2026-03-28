package com.airpods.manager.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.airpods.manager.data.repository.DeviceRepository
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.model.BatteryState
import com.airpods.manager.domain.model.ConnectionState
import com.airpods.manager.domain.usecase.ObserveNearbyDevicesUseCase
import com.airpods.manager.notification.BatteryNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AirPodsMonitorService : Service() {

    @Inject lateinit var observeNearbyDevices: ObserveNearbyDevicesUseCase
    @Inject lateinit var deviceRepository: DeviceRepository
    @Inject lateinit var batteryNotificationManager: BatteryNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scanJob: Job? = null
    private var currentDevice: AirPodsDevice? = null
    private val alertedDevices = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        startForeground(
            BatteryNotificationManager.MONITORING_NOTIFICATION_ID,
            batteryNotificationManager.buildMonitoringNotification(null)
        )
        startScanning()
    }

    private fun startScanning() {
        scanJob = serviceScope.launch {
            observeNearbyDevices()
                .catch { /* Restart on error */ startScanning() }
                .collect { device ->
                    currentDevice = device
                    batteryNotificationManager.updateMonitoringNotification(device)

                    // Persist device to DB
                    deviceRepository.observeNearbyDevices()

                    // Check low battery alerts
                    checkBatteryAlerts(device)
                }
        }
    }

    private fun checkBatteryAlerts(device: AirPodsDevice) {
        val alertKey = device.address
        if (alertedDevices.contains(alertKey)) return

        val battery = device.battery
        val threshold = 20 // Default, should come from preferences in production

        if (battery.left in 1 until threshold) {
            batteryNotificationManager.showLowBatteryAlert(device, "Left earbud", battery.left)
            alertedDevices.add("$alertKey-left")
        }
        if (battery.right in 1 until threshold) {
            batteryNotificationManager.showLowBatteryAlert(device, "Right earbud", battery.right)
            alertedDevices.add("$alertKey-right")
        }
        if (battery.case in 1 until threshold) {
            batteryNotificationManager.showLowBatteryAlert(device, "Case", battery.case)
            alertedDevices.add("$alertKey-case")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scanJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.airpods.manager.STOP_SERVICE"
    }
}
