package com.airpods.manager.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.airpods.manager.ble.model.AirPodsAdvertisement
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    private var scanCallback: ScanCallback? = null

    fun scanForAirPods(): Flow<AirPodsAdvertisement> = callbackFlow {
        if (!hasBluetoothPermission()) {
            close(SecurityException("Bluetooth scan permission not granted"))
            return@callbackFlow
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("BLE scanner not available"))
            return@callbackFlow
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setReportDelay(0)
            .build()

        // No UUID filter — AirPods don't advertise standard GATT service UUIDs
        val filters = emptyList<ScanFilter>()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val manufacturerData = result.scanRecord
                    ?.getManufacturerSpecificData(0x004C)
                    ?: return

                if (!AirPodsAdvertisementParser.isAirPodsAdvertisement(manufacturerData)) return

                // result.device.name is only populated from the Android BT name cache (requires
                // a prior classic BT connection). The scan record local name is available
                // directly from the advertisement payload and works without a prior connection.
                val deviceName = result.device.name ?: result.scanRecord?.deviceName

                val advertisement = AirPodsAdvertisementParser.parse(
                    manufacturerData = manufacturerData,
                    rssi = result.rssi,
                    deviceAddress = result.device.address,
                    deviceName = deviceName
                ) ?: return

                trySend(advertisement)
            }

            override fun onScanFailed(errorCode: Int) {
                close(Exception("BLE scan failed with error code: $errorCode"))
            }
        }

        scanCallback = callback
        scanner.startScan(filters, settings, callback)

        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (_: Exception) {}
            scanCallback = null
        }
    }

    fun stopScan() {
        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
        val callback = scanCallback ?: return
        try {
            scanner.stopScan(callback)
        } catch (_: Exception) {}
        scanCallback = null
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
