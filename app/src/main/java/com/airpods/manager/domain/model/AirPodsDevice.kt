package com.airpods.manager.domain.model

import com.airpods.manager.ble.model.AirPodsModel

data class AirPodsDevice(
    val address: String,
    val name: String,
    val model: AirPodsModel,
    val battery: BatteryState,
    val connectionState: ConnectionState,
    val rssi: Int,
    val lastSeen: Long,
    val leftInEar: Boolean,
    val rightInEar: Boolean,
    val lidOpen: Boolean
)
