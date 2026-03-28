package com.airpods.manager.ble.model

data class AirPodsAdvertisement(
    val model: AirPodsModel,
    val leftBattery: Int,       // 0-100, -1 if unknown
    val rightBattery: Int,      // 0-100, -1 if unknown
    val caseBattery: Int,       // 0-100, -1 if unknown
    val leftCharging: Boolean,
    val rightCharging: Boolean,
    val caseCharging: Boolean,
    val leftInEar: Boolean,
    val rightInEar: Boolean,
    val lidOpen: Boolean,
    val bothInCase: Boolean,
    val rssi: Int,              // Signal strength in dBm
    val deviceAddress: String,
    val deviceName: String?
)
