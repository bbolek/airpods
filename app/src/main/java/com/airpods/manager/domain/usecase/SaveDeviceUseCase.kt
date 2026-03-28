package com.airpods.manager.domain.usecase

import com.airpods.manager.ble.model.AirPodsAdvertisement
import com.airpods.manager.data.repository.DeviceRepository
import javax.inject.Inject

class SaveDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(advertisement: AirPodsAdvertisement) {
        deviceRepository.saveDevice(advertisement)
    }
}
