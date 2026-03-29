package com.airpods.manager.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.airpods.manager.domain.model.ConnectionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class MediaCommand {
    PLAY_PAUSE, NEXT_TRACK, PREVIOUS_TRACK, VOLUME_UP, VOLUME_DOWN
}

@Singleton
class BluetoothController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    private val _connectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = _connectionStates

    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
            device ?: return
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED ->
                    updateState(device.address, ConnectionState.CONNECTED)
                BluetoothDevice.ACTION_ACL_DISCONNECTED ->
                    updateState(device.address, ConnectionState.DISCONNECTED)
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(connectionReceiver, filter)
    }

    fun connect(deviceAddress: String): Result<Unit> {
        if (!hasConnectPermission()) return Result.failure(SecurityException("BLUETOOTH_CONNECT not granted"))
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            ?: return Result.failure(IllegalArgumentException("Device not found: $deviceAddress"))
        return try {
            device.createBond()
            updateState(deviceAddress, ConnectionState.CONNECTING)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect(deviceAddress: String): Result<Unit> {
        if (!hasConnectPermission()) return Result.failure(SecurityException("BLUETOOTH_CONNECT not granted"))
        updateState(deviceAddress, ConnectionState.DISCONNECTED)
        return Result.success(Unit)
    }

    fun getConnectedDevices(): List<BluetoothDevice> {
        if (!hasConnectPermission()) return emptyList()
        return try {
            val connectedAddresses = _connectionStates.value
                .filter { it.value == ConnectionState.CONNECTED }
                .keys
            bluetoothAdapter.bondedDevices
                ?.filter { it.address in connectedAddresses }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isDeviceConnected(address: String): Boolean =
        _connectionStates.value[address] == ConnectionState.CONNECTED

    fun getPairedAirPods(): List<BluetoothDevice> {
        if (!hasConnectPermission()) return emptyList()
        return try {
            bluetoothAdapter.bondedDevices?.filter { device ->
                // AirPods typically show up with specific device class or name patterns
                device.name?.contains("AirPods", ignoreCase = true) == true ||
                        device.name?.contains("Beats", ignoreCase = true) == true ||
                        device.name?.contains("Powerbeats", ignoreCase = true) == true
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun sendMediaCommand(command: MediaCommand) {
        // Media commands are sent via AudioManager / MediaSession from the notification
        // The actual playback control happens through the system media session
    }

    fun getConnectionState(deviceAddress: String): ConnectionState =
        _connectionStates.value[deviceAddress] ?: ConnectionState.DISCONNECTED

    private fun updateState(address: String, state: ConnectionState) {
        _connectionStates.value = _connectionStates.value.toMutableMap().also {
            it[address] = state
        }
    }

    private fun hasConnectPermission(): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else true
}
