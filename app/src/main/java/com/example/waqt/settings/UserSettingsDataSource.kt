package com.example.waqt.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.waqt.repository.PrayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

data class UserSettings(
    val city: String = PrayerRepository.DEFAULT_CITY,
    val calculationMethod: Int = PrayerRepository.DEFAULT_METHOD,
    val notificationsEnabled: Boolean = true
)

interface UserSettingsDataSource {
    val settingsFlow: Flow<UserSettings>

    suspend fun setCity(city: String)

    suspend fun setCalculationMethod(method: Int)

    suspend fun setNotificationsEnabled(enabled: Boolean)
}

private val Context.userSettingsDataStore by preferencesDataStore(name = "user_settings")

class DataStoreUserSettingsDataSource(
    private val context: Context
) : UserSettingsDataSource {
    private val cityKey = stringPreferencesKey("city")
    private val calculationMethodKey = intPreferencesKey("calculation_method")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")

    override val settingsFlow: Flow<UserSettings> = context.userSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map(::toUserSettings)

    override suspend fun setCity(city: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[cityKey] = city.trim()
        }
    }

    override suspend fun setCalculationMethod(method: Int) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[calculationMethodKey] = method
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }

    private fun toUserSettings(preferences: Preferences): UserSettings {
        return UserSettings(
            city = preferences[cityKey] ?: PrayerRepository.DEFAULT_CITY,
            calculationMethod = preferences[calculationMethodKey] ?: PrayerRepository.DEFAULT_METHOD,
            notificationsEnabled = preferences[notificationsEnabledKey] ?: true
        )
    }
}

class InMemoryUserSettingsDataSource(
    initial: UserSettings = UserSettings()
) : UserSettingsDataSource {
    private val state = MutableStateFlow(initial)

    override val settingsFlow: Flow<UserSettings> = state

    override suspend fun setCity(city: String) {
        state.value = state.value.copy(city = city.trim())
    }

    override suspend fun setCalculationMethod(method: Int) {
        state.value = state.value.copy(calculationMethod = method)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        state.value = state.value.copy(notificationsEnabled = enabled)
    }

    suspend fun current(): UserSettings {
        return settingsFlow.first()
    }
}
