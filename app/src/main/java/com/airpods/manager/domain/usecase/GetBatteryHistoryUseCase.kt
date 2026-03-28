package com.airpods.manager.domain.usecase

import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import com.airpods.manager.data.repository.BatteryHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBatteryHistoryUseCase @Inject constructor(
    private val batteryHistoryRepository: BatteryHistoryRepository
) {
    operator fun invoke(deviceAddress: String, limit: Int = 100): Flow<List<BatteryHistoryEntity>> =
        batteryHistoryRepository.observeHistory(deviceAddress, limit)
}
