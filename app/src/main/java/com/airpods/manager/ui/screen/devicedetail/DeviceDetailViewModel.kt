package com.airpods.manager.ui.screen.devicedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airpods.manager.data.db.entity.BatteryHistoryEntity
import com.airpods.manager.data.repository.DeviceRepository
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.usecase.ConnectDeviceUseCase
import com.airpods.manager.domain.usecase.DisconnectDeviceUseCase
import com.airpods.manager.domain.usecase.GetBatteryHistoryUseCase
import com.airpods.manager.domain.usecase.ObserveNearbyDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceDetailUiState(
    val device: AirPodsDevice? = null,
    val batteryHistory: List<BatteryHistoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showRenameDialog: Boolean = false,
    val renameInput: String = "",
    val showDeleteConfirm: Boolean = false,
    val deviceDeleted: Boolean = false
)

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeNearbyDevices: ObserveNearbyDevicesUseCase,
    private val connectDevice: ConnectDeviceUseCase,
    private val disconnectDevice: DisconnectDeviceUseCase,
    private val getBatteryHistory: GetBatteryHistoryUseCase,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val address: String = checkNotNull(savedStateHandle["address"])

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState

    init {
        observeDevice()
        loadHistory()
    }

    private fun observeDevice() {
        viewModelScope.launch {
            observeNearbyDevices()
                .catch { /* Silently handle */ }
                .collect { device ->
                    if (device.address == address) {
                        _uiState.value = _uiState.value.copy(
                            device = device,
                            isLoading = false
                        )
                    }
                }
        }

        // Also load from saved DB
        viewModelScope.launch {
            deviceRepository.observeSavedDevices()
                .catch { }
                .collect { devices ->
                    val saved = devices.firstOrNull { it.address == address }
                    if (saved != null && _uiState.value.device == null) {
                        _uiState.value = _uiState.value.copy(
                            device = saved,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getBatteryHistory(address).collect { history ->
                _uiState.value = _uiState.value.copy(batteryHistory = history)
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            connectDevice(address)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            disconnectDevice(address)
        }
    }

    fun openRenameDialog() {
        _uiState.value = _uiState.value.copy(
            showRenameDialog = true,
            renameInput = _uiState.value.device?.name ?: ""
        )
    }

    fun onRenameInputChange(input: String) {
        _uiState.value = _uiState.value.copy(renameInput = input)
    }

    fun confirmRename() {
        val name = _uiState.value.renameInput.trim()
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                deviceRepository.renameDevice(address, name)
            }
        }
        _uiState.value = _uiState.value.copy(showRenameDialog = false)
    }

    fun dismissRenameDialog() {
        _uiState.value = _uiState.value.copy(showRenameDialog = false)
    }

    fun openDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun dismissDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun confirmDelete() {
        viewModelScope.launch {
            deviceRepository.deleteDevice(address)
            _uiState.value = _uiState.value.copy(showDeleteConfirm = false, deviceDeleted = true)
        }
    }
}
