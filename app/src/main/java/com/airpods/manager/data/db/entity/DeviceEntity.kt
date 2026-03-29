package com.airpods.manager.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val address: String,
    val name: String,
    val modelId: Int,
    val lastSeen: Long,
    val isFavorite: Boolean = false,
    val customName: String? = null
)
