package com.airpods.manager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryHistoryDao {

    @Query("SELECT * FROM battery_history WHERE deviceAddress = :address ORDER BY timestamp DESC LIMIT :limit")
    fun observeHistory(address: String, limit: Int = 100): Flow<List<BatteryHistoryEntity>>

    @Insert
    suspend fun insert(entry: BatteryHistoryEntity)

    @Query("DELETE FROM battery_history WHERE deviceAddress = :address AND timestamp < :before")
    suspend fun deleteOlderThan(address: String, before: Long)

    @Query("SELECT COUNT(*) FROM battery_history WHERE deviceAddress = :address")
    suspend fun countForDevice(address: String): Int
}
