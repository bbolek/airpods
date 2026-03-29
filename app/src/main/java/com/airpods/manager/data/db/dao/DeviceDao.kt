package com.airpods.manager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.airpods.manager.data.db.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE address = :address")
    suspend fun getByAddress(address: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)

    @Query("UPDATE devices SET lastSeen = :timestamp WHERE address = :address")
    suspend fun updateLastSeen(address: String, timestamp: Long)

    @Query("DELETE FROM devices WHERE address = :address")
    suspend fun delete(address: String)

    @Query("DELETE FROM devices WHERE lastSeen < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("UPDATE devices SET customName = :customName WHERE address = :address")
    suspend fun updateCustomName(address: String, customName: String?)
}
