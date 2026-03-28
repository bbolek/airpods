package com.airpods.manager.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.usecase.ObserveNearbyDevicesUseCase
import com.airpods.manager.domain.usecase.SaveDeviceUseCase
import com.airpods.manager.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val activeDevice: AirPodsDevice? = null,
    val isScanning: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeNearbyDevices: ObserveNearbyDevicesUseCase,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        startScanning()
    }

    private fun startScanning() {
        _uiState.value = _uiState.value.copy(isScanning = true, errorMessage = null)
        viewModelScope.launch {
            observeNearbyDevices()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        errorMessage = e.message
                    )
                }
                .collect { device ->
                    _uiState.value = _uiState.value.copy(
                        activeDevice = device,
                        isScanning = true
                    )
                    // Auto-save seen device
                    deviceRepository.saveDevice(
                        com.airpods.manager.ble.model.AirPodsAdvertisement(
                            model = device.model,
                            leftBattery = device.battery.left,
                            rightBattery = device.battery.right,
                            caseBattery = device.battery.case,
                            leftCharging = device.battery.leftCharging,
                            rightCharging = device.battery.rightCharging,
                            caseCharging = device.battery.caseCharging,
                            leftInEar = device.leftInEar,
                            rightInEar = device.rightInEar,
                            lidOpen = device.lidOpen,
                            bothInCase = false,
                            rssi = device.rssi,
                            deviceAddress = device.address,
                            deviceName = device.name
                        )
                    )
                }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
