package com.airpods.manager.domain.usecase

import com.airpods.manager.ble.model.AirPodsAdvertisement
import com.airpods.manager.bluetooth.BluetoothController
import com.airpods.manager.data.repository.DeviceRepository
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.model.BatteryState
import com.airpods.manager.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveNearbyDevicesUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val bluetoothController: BluetoothController
) {
    operator fun invoke(): Flow<AirPodsDevice> =
        deviceRepository.observeNearbyDevices().map { it.toDomain() }

    private fun AirPodsAdvertisement.toDomain(): AirPodsDevice {
        // BLE advertising uses a different (often rotating) address from the Bluetooth Classic
        // address used for A2DP. Match connected devices by name instead.
        val connectedDevices = bluetoothController.getConnectedDevices()
        val bleDeviceName = deviceName

        val matchedConnectedDevice = if (bleDeviceName != null) {
            connectedDevices.firstOrNull { it.name.equals(bleDeviceName, ignoreCase = true) }
        } else null

        val connectionState = if (matchedConnectedDevice != null) {
            ConnectionState.CONNECTED
        } else {
            ConnectionState.NEARBY
        }

        // Prefer the BLE-advertised name, then the BT classic name from a connected device,
        // then fall back to the model display name so we never show a raw "Unknown Device".
        val resolvedName = bleDeviceName
            ?: matchedConnectedDevice?.name
            ?: bluetoothController.getPairedAirPods()
                .firstOrNull { it.name?.contains(model.displayName, ignoreCase = true) == true }
                ?.name
            ?: model.displayName

        return AirPodsDevice(
            address = deviceAddress,
            name = resolvedName,
            model = model,
            battery = BatteryState(
                left = leftBattery,
                right = rightBattery,
                case = caseBattery,
                leftCharging = leftCharging,
                rightCharging = rightCharging,
                caseCharging = caseCharging
            ),
            connectionState = connectionState,
            rssi = rssi,
            lastSeen = System.currentTimeMillis(),
            leftInEar = leftInEar,
            rightInEar = rightInEar,
            lidOpen = lidOpen
        )
    }
}
