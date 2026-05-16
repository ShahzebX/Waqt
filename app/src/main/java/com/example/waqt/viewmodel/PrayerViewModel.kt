package com.example.waqt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.model.Prayer
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.location.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrayerUiState(
    val prayers: List<Prayer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val requiresManualLocationInput: Boolean = false
)

class PrayerViewModel(
    private val repository: PrayerRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    fun loadPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = PrayerRepository.DEFAULT_METHOD
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            repository.getPrayerTimes(latitude = latitude, longitude = longitude, method = method)
                .onSuccess { prayers ->
                    _uiState.value = PrayerUiState(prayers = prayers, isLoading = false)
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
                    errorMessage = null
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
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            requiresManualLocationInput = true,
                            errorMessage = manualFallbackMessage
                        )
                    }
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
                    requiresManualLocationInput = true
                )
            }

            repository.getPrayerTimesByCity(city = normalizedCity, country = country, method = method)
                .onSuccess { prayers ->
                    _uiState.value = PrayerUiState(prayers = prayers, isLoading = false)
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

    companion object {
        private const val defaultErrorMessage = "Could not load prayer times."
        private const val permissionDeniedMessage =
            "Location permission denied. Enter your city manually."
        private const val manualFallbackMessage =
            "Unable to read your location. Enter your city manually."
        private const val emptyCityMessage = "Enter a city name."
    }
}
