package com.example.waqt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.location.LocationProvider
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.settings.UserSettingsDataSource
import com.example.waqt.worker.PrayerNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val city: String = PrayerRepository.DEFAULT_CITY,
    val calculationMethod: Int = PrayerRepository.DEFAULT_METHOD,
    val notificationsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class SettingsViewModel(
    private val repository: PrayerRepository,
    private val locationProvider: LocationProvider,
    private val settingsDataSource: UserSettingsDataSource,
    private val notificationScheduler: PrayerNotificationScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataSource.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        city = settings.city,
                        calculationMethod = settings.calculationMethod,
                        notificationsEnabled = settings.notificationsEnabled
                    )
                }
            }
        }
    }

    fun onCityInputChange(city: String) {
        _uiState.update { it.copy(city = city, errorMessage = null, successMessage = null) }
    }

    fun onCalculationMethodSelected(method: Int) {
        viewModelScope.launch {
            settingsDataSource.setCalculationMethod(method)
            _uiState.update {
                it.copy(
                    calculationMethod = method,
                    successMessage = "Calculation method updated.",
                    errorMessage = null
                )
            }
        }
    }

    fun onNotificationsEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setNotificationsEnabled(enabled)
            _uiState.update {
                it.copy(
                    notificationsEnabled = enabled,
                    successMessage = if (enabled) {
                        "Prayer reminders enabled."
                    } else {
                        "Prayer reminders disabled."
                    },
                    errorMessage = null,
                    infoMessage = null
                )
            }
        }
    }

    fun onNotificationPermissionDenied() {
        viewModelScope.launch {
            settingsDataSource.setNotificationsEnabled(false)
            _uiState.update {
                it.copy(
                    notificationsEnabled = false,
                    infoMessage = "Allow notifications in system settings to receive prayer reminders."
                )
            }
        }
    }

    fun saveCityAndRefreshPrayerTimes() {
        val city = _uiState.value.city.trim()
        if (city.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter a city name.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    infoMessage = null
                )
            }

            settingsDataSource.setCity(city)
            val method = _uiState.value.calculationMethod

            repository.getPrayerTimesByCity(city = city, method = method)
                .onSuccess { prayers ->
                    if (_uiState.value.notificationsEnabled) {
                        notificationScheduler.schedule(prayers)
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            city = city,
                            successMessage = "Prayer times updated for $city."
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Could not update prayer times."
                        )
                    }
                }
        }
    }

    fun refreshFromCurrentLocation() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    infoMessage = null
                )
            }

            locationProvider.getCurrentCoordinates()
                .onSuccess { coordinates ->
                    val method = _uiState.value.calculationMethod
                    repository.getPrayerTimes(
                        latitude = coordinates.latitude,
                        longitude = coordinates.longitude,
                        method = method
                    ).onSuccess { prayers ->
                        if (_uiState.value.notificationsEnabled) {
                            notificationScheduler.schedule(prayers)
                        }
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                successMessage = "Prayer times updated from GPS."
                            )
                        }
                    }.onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = throwable.message ?: "Could not update prayer times."
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Location unavailable. Save a city instead.",
                            infoMessage = null
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null, infoMessage = null) }
    }
}
