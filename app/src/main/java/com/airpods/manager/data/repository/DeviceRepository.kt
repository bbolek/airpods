package com.airpods.manager.data.repository

import com.airpods.manager.ble.BleScanner
import com.airpods.manager.ble.model.AirPodsAdvertisement
import com.airpods.manager.ble.model.AirPodsModel
import com.airpods.manager.data.db.dao.BatteryHistoryDao
import com.airpods.manager.data.db.dao.DeviceDao
import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import com.airpods.manager.data.db.entity.DeviceEntity
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.model.BatteryState
import com.airpods.manager.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceDao: DeviceDao,
    private val batteryHistoryDao: BatteryHistoryDao,
    private val bleScanner: BleScanner
) {
    fun observeNearbyDevices(): Flow<AirPodsAdvertisement> = bleScanner.scanForAirPods()

    fun observeSavedDevices(): Flow<List<AirPodsDevice>> =
        deviceDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveDevice(advertisement: AirPodsAdvertisement) {
        val existing = deviceDao.getByAddress(advertisement.deviceAddress)
        val entity = DeviceEntity(
            address = advertisement.deviceAddress,
            name = advertisement.deviceName ?: advertisement.model.displayName,
            modelId = advertisement.model.modelId,
            lastSeen = System.currentTimeMillis(),
            isFavorite = existing?.isFavorite ?: false,
            customName = existing?.customName
        )
        deviceDao.upsert(entity)

        // Record battery history snapshot
        if (advertisement.leftBattery != -1 || advertisement.rightBattery != -1) {
            batteryHistoryDao.insert(
                BatteryHistoryEntity(
                    deviceAddress = advertisement.deviceAddress,
                    timestamp = System.currentTimeMillis(),
                    leftBattery = advertisement.leftBattery,
                    rightBattery = advertisement.rightBattery,
                    caseBattery = advertisement.caseBattery
                )
            )
        }
    }

    suspend fun deleteDevice(address: String) {
        deviceDao.delete(address)
    }

    suspend fun renameDevice(address: String, customName: String) {
        deviceDao.updateCustomName(address, customName.takeIf { it.isNotBlank() })
    }

    private fun DeviceEntity.toDomain() = AirPodsDevice(
        address = address,
        name = customName ?: name,
        model = AirPodsModel.fromModelId(modelId),
        battery = BatteryState.UNKNOWN,
        connectionState = ConnectionState.DISCONNECTED,
        rssi = 0,
        lastSeen = lastSeen,
        leftInEar = false,
        rightInEar = false,
        lidOpen = false
    )
}
