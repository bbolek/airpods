package com.airpods.manager

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airpods.manager.data.db.AppDatabase
import com.airpods.manager.data.db.entity.DeviceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadDevice() = runTest {
        val device = DeviceEntity(
            address = "AA:BB:CC:DD:EE:FF",
            name = "Test AirPods",
            modelId = 0x200F,
            lastSeen = System.currentTimeMillis()
        )
        db.deviceDao().upsert(device)
        val retrieved = db.deviceDao().getByAddress("AA:BB:CC:DD:EE:FF")
        assertNotNull(retrieved)
        assertEquals("Test AirPods", retrieved?.name)
    }

    @Test
    fun observeAllDevices() = runTest {
        val device = DeviceEntity(
            address = "AA:BB:CC:DD:EE:FF",
            name = "Test AirPods",
            modelId = 0x200F,
            lastSeen = System.currentTimeMillis()
        )
        db.deviceDao().upsert(device)
        val devices = db.deviceDao().observeAll().first()
        assertEquals(1, devices.size)
    }
}
