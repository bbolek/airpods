package com.airpods.manager.ble

import com.airpods.manager.ble.model.AirPodsModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AirPodsAdvertisementParserTest {

    // Apple company ID: 0x4C 0x00, type 0x07
    // Model: AirPods 2 = 0x200F (bytes: 0x0F, 0x20 in little-endian)
    // Status byte: 0x00 (not flipped, not in case)
    // Battery byte1: 0x95 => pod1=9 (90%), pod2=5 (50%)
    // Battery byte2: 0x72 => case=7 (70%), charging flags=0x02 (right/pod2 charging)
    // Extra byte: 0x00

    private val validAirPods2Data = byteArrayOf(
        0x4C.toByte(), 0x00.toByte(), // Apple company ID
        0x07.toByte(),                // AirPods type
        0x19.toByte(),                // length
        0x0F.toByte(), 0x20.toByte(), // Model: AirPods 2 (0x200F)
        0x00.toByte(),                // status: not flipped
        0x95.toByte(),                // battery: pod1=9(90%), pod2=5(50%)
        0x72.toByte(),                // case=7(70%), charging=0x02 (pod2 charging)
        0x00.toByte(),                // extra
        0x00.toByte(), 0x00.toByte(), 0x00.toByte() // padding
    )

    @Test
    fun `parse valid AirPods2 advertisement`() {
        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = validAirPods2Data,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "My AirPods"
        )

        assertNotNull(result)
        result!!

        assertEquals(AirPodsModel.AIRPODS_2, result.model)
        assertEquals("AA:BB:CC:DD:EE:FF", result.deviceAddress)
        assertEquals("My AirPods", result.deviceName)
        assertEquals(-60, result.rssi)
    }

    @Test
    fun `battery levels are decoded correctly when not flipped`() {
        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = validAirPods2Data,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )!!

        // pod1 = left (not flipped), nibble 9 = 90%
        assertEquals(90, result.leftBattery)
        // pod2 = right (not flipped), nibble 5 = 50%
        assertEquals(50, result.rightBattery)
        // case nibble 7 = 70%
        assertEquals(70, result.caseBattery)
    }

    @Test
    fun `charging flags decoded correctly`() {
        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = validAirPods2Data,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )!!

        // Charging byte2 lower nibble = 0x02 = pod2 charging = right charging
        assertFalse(result.leftCharging)
        assertTrue(result.rightCharging)
        assertFalse(result.caseCharging)
    }

    @Test
    fun `flipped bit swaps left and right`() {
        val flippedData = validAirPods2Data.copyOf()
        flippedData[6] = 0x20.toByte() // Set flip bit

        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = flippedData,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )!!

        // When flipped, pod1 becomes right, pod2 becomes left
        assertEquals(50, result.leftBattery)  // was pod2
        assertEquals(90, result.rightBattery) // was pod1
    }

    @Test
    fun `unknown battery nibble returns minus one`() {
        val unknownBattery = validAirPods2Data.copyOf()
        unknownBattery[7] = 0xFF.toByte() // Both nibbles = 0xF = unknown

        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = unknownBattery,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )!!

        assertEquals(-1, result.leftBattery)
        assertEquals(-1, result.rightBattery)
    }

    @Test
    fun `returns null for non-Apple data`() {
        val nonAppleData = byteArrayOf(
            0x00.toByte(), 0x01.toByte(), 0x07.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )

        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = nonAppleData,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )

        assertNull(result)
    }

    @Test
    fun `returns null for non-AirPods Apple data`() {
        val nonAirPodsApple = byteArrayOf(
            0x4C.toByte(), 0x00.toByte(), 0x09.toByte(), // type 0x09, not 0x07
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )

        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = nonAirPodsApple,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )

        assertNull(result)
    }

    @Test
    fun `returns null for too short data`() {
        val shortData = byteArrayOf(0x4C.toByte(), 0x00.toByte(), 0x07.toByte())

        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = shortData,
            rssi = -60,
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = null
        )

        assertNull(result)
    }

    @Test
    fun `isAirPodsAdvertisement returns true for valid data`() {
        assertTrue(AirPodsAdvertisementParser.isAirPodsAdvertisement(validAirPods2Data))
    }

    @Test
    fun `isAirPodsAdvertisement returns false for non-AirPods data`() {
        assertFalse(
            AirPodsAdvertisementParser.isAirPodsAdvertisement(
                byteArrayOf(0x00, 0x01, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            )
        )
    }

    @Test
    fun `AirPods Pro model is identified correctly`() {
        val airPodsProData = validAirPods2Data.copyOf()
        // Set model to AirPods Pro: 0x200E -> bytes 0x0E, 0x20
        airPodsProData[4] = 0x0E.toByte()
        airPodsProData[5] = 0x20.toByte()

        val result = AirPodsAdvertisementParser.parse(
            manufacturerData = airPodsProData,
            rssi = -55,
            deviceAddress = "11:22:33:44:55:66",
            deviceName = "AirPods Pro"
        )!!

        assertEquals(AirPodsModel.AIRPODS_PRO, result.model)
    }
}
