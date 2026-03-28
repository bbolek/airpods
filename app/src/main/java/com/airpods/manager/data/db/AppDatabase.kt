package com.airpods.manager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.airpods.manager.data.db.dao.BatteryHistoryDao
import com.airpods.manager.data.db.dao.DeviceDao
import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import com.airpods.manager.data.db.entity.DeviceEntity

@Database(
    entities = [DeviceEntity::class, BatteryHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun batteryHistoryDao(): BatteryHistoryDao
}
