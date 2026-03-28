package com.airpods.manager.domain.usecase

import com.airpods.manager.bluetooth.BluetoothController
import javax.inject.Inject

class DisconnectDeviceUseCase @Inject constructor(
    private val bluetoothController: BluetoothController
) {
    operator fun invoke(deviceAddress: String): Result<Unit> =
        bluetoothController.disconnect(deviceAddress)
}
