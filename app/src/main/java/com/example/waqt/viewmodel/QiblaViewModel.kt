package com.example.waqt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.location.GeoCoordinates
import com.example.waqt.location.LocationProvider
import com.example.waqt.qibla.CityCoordinatesResolver
import com.example.waqt.qibla.CompassSensorProvider
import com.example.waqt.qibla.CompassSensorStatus
import com.example.waqt.qibla.QiblaCalculator
import com.example.waqt.settings.UserSettingsDataSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QiblaLocationSource {
    Gps,
    SavedCity,
    DefaultCity
}

data class QiblaUiState(
    val qiblaBearing: Float = 0f,
    val azimuth: Float = 0f,
    /** Clockwise offset from screen top to Qibla; 0° = facing Makkah. */
    val qiblaOffsetDegrees: Float = 0f,
    val isFacingQibla: Boolean = false,
    /** @deprecated Use [qiblaOffsetDegrees]; kept for tests. */
    val needleRotation: Float = 0f,
    val compassSensorStatus: CompassSensorStatus = CompassSensorStatus.Unavailable,
    val locationSource: QiblaLocationSource? = null,
    val locationLabel: String? = null,
    val isLoadingLocation: Boolean = false,
    val requiresManualLocation: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

class QiblaViewModel(
    private val locationProvider: LocationProvider,
    private val compassSensorProvider: CompassSensorProvider,
    private val settingsDataSource: UserSettingsDataSource
) : ViewModel() {
    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private var compassCollectionJob: Job? = null

    init {
        _uiState.update { current ->
            current.copy(compassSensorStatus = compassSensorProvider.status)
        }
        startCompassReadings()
    }

    fun refreshLocation(useGps: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingLocation = true,
                    errorMessage = null,
                    infoMessage = null,
                    requiresManualLocation = false
                )
            }

            if (useGps) {
                locationProvider.getCurrentCoordinates()
                    .onSuccess { coordinates ->
                        applyCoordinates(
                            coordinates = coordinates,
                            source = QiblaLocationSource.Gps,
                            label = "Current location"
                        )
                    }
                    .onFailure {
                        onLocationUnavailable()
                    }
            } else {
                onLocationUnavailable()
            }
        }
    }

    fun onLocationPermissionDenied() {
        viewModelScope.launch {
            val settings = settingsDataSource.settingsFlow.first()
            val cityCoordinates = CityCoordinatesResolver.resolve(settings.city)
            if (cityCoordinates != null) {
                applyCoordinates(
                    coordinates = cityCoordinates,
                    source = QiblaLocationSource.SavedCity,
                    label = settings.city,
                    infoMessage = locationDeniedUsingCityMessage
                )
            } else {
                val fallback = CityCoordinatesResolver.defaultFallback()
                applyCoordinates(
                    coordinates = fallback,
                    source = QiblaLocationSource.DefaultCity,
                    label = settings.city.ifBlank { "Karachi" },
                    requiresManualLocation = true,
                    infoMessage = locationDeniedUnknownCityMessage
                )
            }
        }
    }

    fun onLocationPermissionRequired() {
        _uiState.update {
            it.copy(
                isLoadingLocation = false,
                requiresManualLocation = true,
                infoMessage = locationRationaleMessage,
                errorMessage = null
            )
        }
    }

    private suspend fun onLocationUnavailable() {
        val settings = settingsDataSource.settingsFlow.first()
        val cityCoordinates = CityCoordinatesResolver.resolve(settings.city)
        if (cityCoordinates != null) {
            applyCoordinates(
                coordinates = cityCoordinates,
                source = QiblaLocationSource.SavedCity,
                label = settings.city,
                infoMessage = gpsUnavailableUsingCityMessage
            )
        } else {
            val fallback = CityCoordinatesResolver.defaultFallback()
            applyCoordinates(
                coordinates = fallback,
                source = QiblaLocationSource.DefaultCity,
                label = settings.city.ifBlank { "Karachi" },
                requiresManualLocation = true,
                infoMessage = gpsUnavailableUnknownCityMessage
            )
        }
    }

    private fun applyCoordinates(
        coordinates: GeoCoordinates,
        source: QiblaLocationSource,
        label: String,
        requiresManualLocation: Boolean = false,
        infoMessage: String? = null
    ) {
        val bearing = QiblaCalculator.bearingFrom(coordinates)
        _uiState.update { current ->
            val offset = QiblaCalculator.qiblaOffsetFromScreenTop(bearing, current.azimuth)
            current.copy(
                qiblaBearing = bearing,
                qiblaOffsetDegrees = offset,
                needleRotation = offset,
                isFacingQibla = QiblaCalculator.isFacingQibla(bearing, current.azimuth),
                locationSource = source,
                locationLabel = label,
                isLoadingLocation = false,
                requiresManualLocation = requiresManualLocation,
                infoMessage = infoMessage,
                errorMessage = null
            )
        }
    }

    private fun startCompassReadings() {
        if (compassSensorProvider.status == CompassSensorStatus.Unavailable) {
            _uiState.update { current ->
                current.copy(
                    infoMessage = current.infoMessage ?: compassUnavailableMessage
                )
            }
            return
        }

        compassCollectionJob?.cancel()
        compassCollectionJob = viewModelScope.launch {
            compassSensorProvider.azimuthDegrees().collect { azimuth ->
                _uiState.update { current ->
                    val offset = QiblaCalculator.qiblaOffsetFromScreenTop(
                        qiblaBearing = current.qiblaBearing,
                        deviceAzimuth = azimuth
                    )
                    current.copy(
                        azimuth = azimuth,
                        qiblaOffsetDegrees = offset,
                        needleRotation = offset,
                        isFacingQibla = QiblaCalculator.isFacingQibla(
                            qiblaBearing = current.qiblaBearing,
                            deviceAzimuth = azimuth
                        ),
                        compassSensorStatus = CompassSensorStatus.Available
                    )
                }
            }
        }
    }

    override fun onCleared() {
        compassCollectionJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val locationRationaleMessage =
            "Allow location for an accurate Qibla bearing from your position."
        const val locationDeniedUsingCityMessage =
            "Location off — Qibla bearing uses your saved city."
        const val locationDeniedUnknownCityMessage =
            "Location off — using default coordinates. Set your city on Home for better accuracy."
        const val gpsUnavailableUsingCityMessage =
            "GPS unavailable — Qibla bearing uses your saved city."
        const val gpsUnavailableUnknownCityMessage =
            "GPS unavailable — using default coordinates. Set your city on Home for better accuracy."
        const val compassUnavailableMessage =
            "Compass sensor unavailable. Bearing is shown; rotate your phone to align manually."
    }
}
