package com.example.waqt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.model.Prayer
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.location.LocationProvider
import com.example.waqt.settings.InMemoryUserSettingsDataSource
import com.example.waqt.settings.UserSettingsDataSource
import com.example.waqt.worker.NoOpPrayerNotificationScheduler
import com.example.waqt.worker.PrayerNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrayerUiState(
    val prayers: List<Prayer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val requiresManualLocationInput: Boolean = false,
    val infoMessage: String? = null,
    val selectedMethod: Int = PrayerRepository.DEFAULT_METHOD,
    val savedCity: String = PrayerRepository.DEFAULT_CITY,
    val notificationsEnabled: Boolean = true
)

class PrayerViewModel(
    private val repository: PrayerRepository,
    private val locationProvider: LocationProvider,
    private val settingsDataSource: UserSettingsDataSource = InMemoryUserSettingsDataSource(),
    private val notificationScheduler: PrayerNotificationScheduler = NoOpPrayerNotificationScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataSource.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        selectedMethod = settings.calculationMethod,
                        savedCity = settings.city,
                        notificationsEnabled = settings.notificationsEnabled
                    )
                }
            }
        }
    }

    fun loadPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = PrayerRepository.DEFAULT_METHOD
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            repository.getPrayerTimes(latitude = latitude, longitude = longitude, method = method)
                .onSuccess { prayers ->
                    settingsDataSource.setCalculationMethod(method)
                    schedulePrayerNotifications(prayers)
                    _uiState.update { current ->
                        current.copy(
                            prayers = prayers,
                            isLoading = false,
                            requiresManualLocationInput = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: defaultErrorMessage
                        )
                    }
                }
        }
    }

    fun loadPrayerTimesFromCurrentLocation(
        method: Int = PrayerRepository.DEFAULT_METHOD
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            locationProvider.getCurrentCoordinates()
                .onSuccess { coordinates ->
                    loadPrayerTimes(
                        latitude = coordinates.latitude,
                        longitude = coordinates.longitude,
                        method = method
                    )
                }
                .onFailure {
                    onLocationPermissionRequired()
                }
        }
    }

    fun loadPrayerTimesByCity(
        city: String,
        country: String = PrayerRepository.DEFAULT_COUNTRY,
        method: Int = PrayerRepository.DEFAULT_METHOD
    ) {
        val normalizedCity = city.trim()
        if (normalizedCity.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    requiresManualLocationInput = true,
                    errorMessage = emptyCityMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null,
                    requiresManualLocationInput = true
                )
            }

            settingsDataSource.setCity(normalizedCity)
            settingsDataSource.setCalculationMethod(method)

            repository.getPrayerTimesByCity(city = normalizedCity, country = country, method = method)
                .onSuccess { prayers ->
                    schedulePrayerNotifications(prayers)
                    _uiState.update { current ->
                        current.copy(
                            prayers = prayers,
                            isLoading = false,
                            requiresManualLocationInput = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: defaultErrorMessage,
                            requiresManualLocationInput = true
                        )
                    }
                }
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update { current ->
            current.copy(
                isLoading = false,
                requiresManualLocationInput = true,
                errorMessage = permissionDeniedMessage
            )
        }
    }

    fun onLocationPermissionRequired() {
        _uiState.update { current ->
            current.copy(
                isLoading = false,
                requiresManualLocationInput = true,
                errorMessage = locationRationaleMessage
            )
        }
    }

    fun setCalculationMethod(method: Int) {
        viewModelScope.launch {
            settingsDataSource.setCalculationMethod(method)
        }
    }

    fun onNotificationPermissionDenied() {
        _uiState.update { current ->
            current.copy(
                infoMessage = notificationPermissionDeniedMessage
            )
        }
    }

    private suspend fun schedulePrayerNotifications(prayers: List<Prayer>) {
        if (_uiState.value.notificationsEnabled) {
            notificationScheduler.schedule(prayers)
        }
    }

    companion object {
        private const val defaultErrorMessage = "Could not load prayer times."
        private const val permissionDeniedMessage =
            "Location permission denied. Enter your city manually."
        private const val locationRationaleMessage =
            "Allow location to auto-load prayer times, or enter your city manually."
        private const val notificationPermissionDeniedMessage =
            "Notifications are off. You can enable them any time from Home."
        private const val emptyCityMessage = "Enter a city name."
    }
}
