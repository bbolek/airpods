package com.airpods.manager.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battery_history",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["address"],
            childColumns = ["deviceAddress"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceAddress")]
)
data class BatteryHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceAddress: String,
    val timestamp: Long,
    val leftBattery: Int,
    val rightBattery: Int,
    val caseBattery: Int
)
