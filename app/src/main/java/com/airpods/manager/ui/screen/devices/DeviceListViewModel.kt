package com.airpods.manager.ui.screen.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airpods.manager.data.repository.DeviceRepository
import com.airpods.manager.domain.model.AirPodsDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val devices: StateFlow<List<AirPodsDevice>> = deviceRepository.observeSavedDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDevice(address: String) {
        viewModelScope.launch {
            deviceRepository.deleteDevice(address)
        }
    }
}
