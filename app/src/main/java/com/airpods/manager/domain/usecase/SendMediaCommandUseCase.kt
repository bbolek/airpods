package com.airpods.manager.domain.usecase

import com.airpods.manager.bluetooth.BluetoothController
import com.airpods.manager.bluetooth.MediaCommand
import javax.inject.Inject

class SendMediaCommandUseCase @Inject constructor(
    private val bluetoothController: BluetoothController
) {
    operator fun invoke(command: MediaCommand) {
        bluetoothController.sendMediaCommand(command)
    }
}
