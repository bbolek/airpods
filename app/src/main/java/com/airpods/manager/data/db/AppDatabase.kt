package com.airpods.manager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.airpods.manager.data.db.dao.BatteryHistoryDao
import com.airpods.manager.data.db.dao.DeviceDao
import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import com.airpods.manager.data.db.entity.DeviceEntity

@Database(
    entities = [DeviceEntity::class, BatteryHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun batteryHistoryDao(): BatteryHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN customName TEXT DEFAULT NULL")
            }
        }
    }
}
