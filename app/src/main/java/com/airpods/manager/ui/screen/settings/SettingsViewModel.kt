package com.airpods.manager.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airpods.manager.data.preferences.PreferencesDataStore
import com.airpods.manager.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesDataStore.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesDataStore.updateLowBatteryThreshold(threshold)
        }
    }

    fun setLowBatteryNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateLowBatteryNotifications(enabled)
        }
    }

    fun setAutoStartMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateAutoStartMonitoring(enabled)
        }
    }

    fun setPersistentNotification(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updatePersistentNotification(enabled)
        }
    }
}
