package com.airpods.manager.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.airpods.manager.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val LOW_BATTERY_NOTIFICATIONS = booleanPreferencesKey("low_battery_notifications")
        val AUTO_START_MONITORING = booleanPreferencesKey("auto_start_monitoring")
        val SHOW_NOTIFICATION_ON_CONNECT = booleanPreferencesKey("show_notification_on_connect")
        val PERSISTENT_NOTIFICATION = booleanPreferencesKey("persistent_notification")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            lowBatteryThreshold = prefs[Keys.LOW_BATTERY_THRESHOLD] ?: 20,
            lowBatteryNotificationsEnabled = prefs[Keys.LOW_BATTERY_NOTIFICATIONS] ?: true,
            autoStartMonitoring = prefs[Keys.AUTO_START_MONITORING] ?: true,
            showNotificationOnConnect = prefs[Keys.SHOW_NOTIFICATION_ON_CONNECT] ?: true,
            persistentNotification = prefs[Keys.PERSISTENT_NOTIFICATION] ?: true
        )
    }

    suspend fun updateLowBatteryThreshold(threshold: Int) {
        context.dataStore.edit { it[Keys.LOW_BATTERY_THRESHOLD] = threshold }
    }

    suspend fun updateLowBatteryNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOW_BATTERY_NOTIFICATIONS] = enabled }
    }

    suspend fun updateAutoStartMonitoring(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_START_MONITORING] = enabled }
    }

    suspend fun updatePersistentNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PERSISTENT_NOTIFICATION] = enabled }
    }
}
