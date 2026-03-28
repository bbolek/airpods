package com.airpods.manager.domain.usecase

import com.airpods.manager.ble.model.AirPodsAdvertisement
import com.airpods.manager.data.repository.DeviceRepository
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.model.BatteryState
import com.airpods.manager.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveNearbyDevicesUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<AirPodsDevice> =
        deviceRepository.observeNearbyDevices().map { it.toDomain() }

    private fun AirPodsAdvertisement.toDomain() = AirPodsDevice(
        address = deviceAddress,
        name = deviceName ?: model.displayName,
        model = model,
        battery = BatteryState(
            left = leftBattery,
            right = rightBattery,
            case = caseBattery,
            leftCharging = leftCharging,
            rightCharging = rightCharging,
            caseCharging = caseCharging
        ),
        connectionState = ConnectionState.NEARBY,
        rssi = rssi,
        lastSeen = System.currentTimeMillis(),
        leftInEar = leftInEar,
        rightInEar = rightInEar,
        lidOpen = lidOpen
    )
}
