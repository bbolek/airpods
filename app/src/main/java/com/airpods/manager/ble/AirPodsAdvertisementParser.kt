package com.airpods.manager.ble

import com.airpods.manager.ble.model.AirPodsAdvertisement
import com.airpods.manager.ble.model.AirPodsModel

/**
 * Parses Apple BLE manufacturer-specific advertisement data from AirPods.
 *
 * Android's ScanRecord.getManufacturerSpecificData(0x004C) strips the company ID bytes,
 * so the returned byte array starts directly with the type byte:
 *   Byte 0   : Type = 0x07 (AirPods/Beats)
 *   Byte 1   : Data length
 *   Byte 2-3 : Device model ID (little-endian)
 *   Byte 4   : Status flags (flip bit, in-case, lid open, charging)
 *   Byte 5   : Battery nibbles (upper=pod1, lower=pod2)
 *   Byte 6   : Battery/charging (upper=case, lower=charging flags)
 *   Byte 7   : Lid open counter / extra flags
 */
object AirPodsAdvertisementParser {

    private const val AIRPODS_TYPE = 0x07.toByte()
    private const val MIN_DATA_LENGTH = 8

    // Status byte (index 6) bit masks
    private const val STATUS_FLIP = 0x20        // Which pod is "right"
    private const val STATUS_BOTH_IN_CASE = 0x04
    private const val STATUS_RIGHT_IN_EAR = 0x02
    private const val STATUS_LEFT_IN_EAR = 0x01
    private const val STATUS_LID_OPEN = 0x04    // From byte 9 upper nibble

    // Charging flags in byte 8 lower nibble
    private const val CHARGE_LEFT = 0x01
    private const val CHARGE_RIGHT = 0x02
    private const val CHARGE_CASE = 0x04

    fun parse(
        manufacturerData: ByteArray,
        rssi: Int,
        deviceAddress: String,
        deviceName: String?
    ): AirPodsAdvertisement? {
        if (manufacturerData.size < MIN_DATA_LENGTH) return null
        if (manufacturerData[0] != AIRPODS_TYPE) return null

        val modelId = (manufacturerData[3].toInt() and 0xFF shl 8) or
                (manufacturerData[2].toInt() and 0xFF)
        val model = AirPodsModel.fromModelId(modelId)

        val statusByte = manufacturerData[4].toInt() and 0xFF
        val batteryByte1 = manufacturerData[5].toInt() and 0xFF
        val batteryByte2 = manufacturerData[6].toInt() and 0xFF

        // Flip bit determines which nibble is left vs right
        val isFlipped = (statusByte and STATUS_FLIP) != 0

        val pod1Battery = (batteryByte1 ushr 4) and 0x0F
        val pod2Battery = batteryByte1 and 0x0F
        val caseBatteryNibble = (batteryByte2 ushr 4) and 0x0F
        val chargingFlags = batteryByte2 and 0x0F

        // Map pod1/pod2 to left/right based on flip bit
        val leftBatteryRaw = if (!isFlipped) pod1Battery else pod2Battery
        val rightBatteryRaw = if (!isFlipped) pod2Battery else pod1Battery

        // Battery nibbles: 0x0=0%, 0xA=100%, 0xF=unknown
        val leftBattery = batteryNibbleToPercent(leftBatteryRaw)
        val rightBattery = batteryNibbleToPercent(rightBatteryRaw)
        val caseBattery = batteryNibbleToPercent(caseBatteryNibble)

        val leftCharging = if (!isFlipped)
            (chargingFlags and CHARGE_LEFT) != 0
        else
            (chargingFlags and CHARGE_RIGHT) != 0
        val rightCharging = if (!isFlipped)
            (chargingFlags and CHARGE_RIGHT) != 0
        else
            (chargingFlags and CHARGE_LEFT) != 0
        val caseCharging = (chargingFlags and CHARGE_CASE) != 0

        val bothInCase = (statusByte and STATUS_BOTH_IN_CASE) != 0
        val rightInEarRaw = (statusByte and STATUS_RIGHT_IN_EAR) != 0
        val leftInEarRaw = (statusByte and STATUS_LEFT_IN_EAR) != 0

        val leftInEar = if (!isFlipped) leftInEarRaw else rightInEarRaw
        val rightInEar = if (!isFlipped) rightInEarRaw else leftInEarRaw

        // Lid state is in byte 7 upper nibble
        val lidOpen = if (manufacturerData.size > 7) {
            val lidByte = manufacturerData[7].toInt() and 0xFF
            (lidByte ushr 4) != 0
        } else false

        return AirPodsAdvertisement(
            model = model,
            leftBattery = leftBattery,
            rightBattery = rightBattery,
            caseBattery = caseBattery,
            leftCharging = leftCharging,
            rightCharging = rightCharging,
            caseCharging = caseCharging,
            leftInEar = leftInEar,
            rightInEar = rightInEar,
            lidOpen = lidOpen,
            bothInCase = bothInCase,
            rssi = rssi,
            deviceAddress = deviceAddress,
            deviceName = deviceName
        )
    }

    private fun batteryNibbleToPercent(nibble: Int): Int = when {
        nibble == 0xF -> -1   // Unknown/not connected
        nibble > 0xA -> 100
        else -> nibble * 10
    }

    fun isAirPodsAdvertisement(manufacturerData: ByteArray): Boolean {
        if (manufacturerData.size < MIN_DATA_LENGTH) return false
        return manufacturerData[0] == AIRPODS_TYPE
    }
}
