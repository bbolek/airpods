package com.airpods.manager.data.repository

import com.airpods.manager.data.db.dao.BatteryHistoryDao
import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryHistoryRepository @Inject constructor(
    private val batteryHistoryDao: BatteryHistoryDao
) {
    fun observeHistory(deviceAddress: String, limit: Int = 100): Flow<List<BatteryHistoryEntity>> =
        batteryHistoryDao.observeHistory(deviceAddress, limit)

    suspend fun pruneOldEntries(deviceAddress: String, keepDays: Int = 7) {
        val cutoff = System.currentTimeMillis() - keepDays * 24 * 60 * 60 * 1000L
        batteryHistoryDao.deleteOlderThan(deviceAddress, cutoff)
    }
}
