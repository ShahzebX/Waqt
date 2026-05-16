package com.example.waqt.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.model.Prayer
import com.example.waqt.network.RetrofitInstance
import com.example.waqt.repository.PrayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrayerUiState(
    val prayers: List<Prayer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PrayerRepository = PrayerRepository(
        api = RetrofitInstance.api,
        prayerDao = InMemoryPrayerDao()
    )

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()
    private val _prayers = MutableStateFlow<List<Prayer>>(emptyList())
    val prayers: StateFlow<List<Prayer>> = _prayers.asStateFlow()

    private var hasLoadedDefaults = false

    fun loadDefaultPrayerTimes() {
        if (hasLoadedDefaults) {
            return
        }
        hasLoadedDefaults = true
        loadPrayerTimes(defaultLatitude, defaultLongitude)
    }

    fun loadPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = PrayerRepository.DEFAULT_METHOD
    ) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }

            repository.getPrayerTimes(latitude = latitude, longitude = longitude, method = method)
                .onSuccess { prayers ->
                    Log.d(TAG, "Aladhan response received for $latitude,$longitude")
                    _prayers.value = prayers
                    _uiState.value = PrayerUiState(prayers = prayers, isLoading = false)
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to fetch prayer times", throwable)
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: defaultErrorMessage
                        )
                    }
                }
        }
    }

    companion object {
        private const val TAG = "PrayerViewModel"
        private const val defaultLatitude = 24.8607
        private const val defaultLongitude = 67.0011
        private const val defaultErrorMessage = "Could not load prayer times."
    }
}

private class InMemoryPrayerDao : PrayerDao {
    private val cachedPrayers = mutableListOf<PrayerEntity>()

    override suspend fun insertPrayers(prayers: List<PrayerEntity>) {
        prayers.forEach { prayer ->
            cachedPrayers.removeAll { it.id == prayer.id }
            cachedPrayers.add(prayer)
        }
    }

    override suspend fun getPrayersByDate(date: String): List<PrayerEntity> {
        return cachedPrayers.filter { it.date == date }.sortedBy { it.epochMs }
    }
}
