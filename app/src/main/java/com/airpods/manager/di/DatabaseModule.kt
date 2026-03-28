package com.airpods.manager.di

import android.content.Context
import androidx.room.Room
import com.airpods.manager.data.db.AppDatabase
import com.airpods.manager.data.db.dao.BatteryHistoryDao
import com.airpods.manager.data.db.dao.DeviceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "airpods_manager.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDeviceDao(db: AppDatabase): DeviceDao = db.deviceDao()

    @Provides
    fun provideBatteryHistoryDao(db: AppDatabase): BatteryHistoryDao = db.batteryHistoryDao()
}
