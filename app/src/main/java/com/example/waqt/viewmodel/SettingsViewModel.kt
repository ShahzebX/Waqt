package com.example.waqt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.location.LocationProvider
import com.example.waqt.repository.CityRepository
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.settings.UserSettingsDataSource
import com.example.waqt.worker.PrayerNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val city: String = PrayerRepository.DEFAULT_CITY,
    val citySuggestions: List<String> = emptyList(),
    val pakistanCityCount: Int = 0,
    val isLoadingCities: Boolean = true,
    val calculationMethod: Int = PrayerRepository.DEFAULT_METHOD,
    val notificationsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class SettingsViewModel(
    private val repository: PrayerRepository,
    private val cityRepository: CityRepository,
    private val locationProvider: LocationProvider,
    private val settingsDataSource: UserSettingsDataSource,
    private val notificationScheduler: PrayerNotificationScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val citiesResult = cityRepository.loadPakistanCities()
            val initial = settingsDataSource.settingsFlow.first()
            val suggestions = cityRepository.suggestionsFor(initial.city)
            _uiState.update {
                it.copy(
                    city = initial.city,
                    citySuggestions = suggestions,
                    pakistanCityCount = cityRepository.cachedCityCount(),
                    isLoadingCities = false,
                    calculationMethod = initial.calculationMethod,
                    notificationsEnabled = initial.notificationsEnabled,
                    infoMessage = citiesResult.fold(
                        onSuccess = { null },
                        onFailure = { "Could not load full city list. Showing offline cities." }
                    )
                )
            }
            settingsDataSource.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        calculationMethod = settings.calculationMethod,
                        notificationsEnabled = settings.notificationsEnabled
                    )
                }
            }
        }
    }

    fun onCityInputChange(city: String) {
        _uiState.update {
            it.copy(
                city = city,
                errorMessage = null,
                successMessage = null
            )
        }
        viewModelScope.launch {
            val suggestions = cityRepository.suggestionsFor(city)
            _uiState.update { it.copy(citySuggestions = suggestions) }
        }
    }

    fun onCitySuggestionSelected(city: String) {
        _uiState.update {
            it.copy(
                city = city,
                citySuggestions = emptyList(),
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onCalculationMethodSelected(method: Int) {
        viewModelScope.launch {
            val city = cityRepository.resolveCityName(_uiState.value.city)
                ?: _uiState.value.city.trim()
            if (city.isNotEmpty()) {
                repository.getPrayerTimesByCity(city = city, method = method)
                    .onSuccess { prayers ->
                        settingsDataSource.setCalculationMethod(method)
                        if (_uiState.value.notificationsEnabled) {
                            notificationScheduler.schedule(prayers)
                        }
                    }
            } else {
                settingsDataSource.setCalculationMethod(method)
            }
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
        val cityInput = _uiState.value.city.trim()
        if (cityInput.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter a city name.") }
            return
        }

        viewModelScope.launch {
            val resolvedCity = cityRepository.resolveCityName(cityInput)
            if (resolvedCity == null) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Select a city from the Pakistan suggestions.",
                        citySuggestions = cityRepository.suggestionsFor(cityInput)
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    infoMessage = null,
                    citySuggestions = emptyList()
                )
            }

            val method = _uiState.value.calculationMethod
            repository.getPrayerTimesByCity(city = resolvedCity, method = method)
                .onSuccess { prayers ->
                    settingsDataSource.setCity(resolvedCity)
                    if (_uiState.value.notificationsEnabled) {
                        notificationScheduler.schedule(prayers)
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            city = resolvedCity,
                            successMessage = "Prayer times updated for $resolvedCity."
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
