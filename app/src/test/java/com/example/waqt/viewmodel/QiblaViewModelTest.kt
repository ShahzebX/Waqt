package com.example.waqt.viewmodel

import com.example.waqt.location.GeoCoordinates
import com.example.waqt.location.LocationProvider
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.CityInfoData
import com.example.waqt.network.CityInfoResponse
import com.example.waqt.network.CountriesNowApi
import com.example.waqt.network.CountryCitiesRequest
import com.example.waqt.network.CountryCitiesResponse
import com.example.waqt.network.PrayerResponse
import com.example.waqt.qibla.CompassSensorProvider
import com.example.waqt.qibla.CompassSensorStatus
import com.example.waqt.settings.InMemoryUserSettingsDataSource
import com.example.waqt.settings.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QiblaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `gps success updates qibla bearing`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            locationProvider = QiblaFakeLocationProvider(
                Result.success(GeoCoordinates(24.8607, 67.0011))
            )
        )

        viewModel.refreshLocation(useGps = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingLocation)
        assertEquals(QiblaLocationSource.Gps, state.locationSource)
        assertTrue(state.qiblaBearing > 0f)
        assertEquals(state.qiblaOffsetDegrees, state.needleRotation)
    }

    @Test
    fun `permission denied uses saved city coordinates`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            settings = UserSettings(city = "Karachi")
        )

        viewModel.onLocationPermissionDenied()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(QiblaLocationSource.SavedCity, state.locationSource)
        assertEquals("Karachi", state.locationLabel)
        assertNotNull(state.infoMessage)
    }

    @Test
    fun `unavailable compass surfaces fallback message`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            compassSensorProvider = UnavailableCompassSensorProvider()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CompassSensorStatus.Unavailable, state.compassSensorStatus)
        assertEquals(QiblaViewModel.compassUnavailableMessage, state.infoMessage)
    }

    private fun createViewModel(
        locationProvider: LocationProvider = QiblaFakeLocationProvider(
            Result.failure(SecurityException("denied"))
        ),
        compassSensorProvider: CompassSensorProvider = UnavailableCompassSensorProvider(),
        settings: UserSettings = UserSettings(city = "Karachi")
    ): QiblaViewModel {
        return QiblaViewModel(
            locationProvider = locationProvider,
            compassSensorProvider = compassSensorProvider,
            settingsDataSource = InMemoryUserSettingsDataSource(settings),
            cityRepository = com.example.waqt.repository.CityRepository(
                countriesNowApi = QiblaFakeCountriesNowApi(),
                aladhanApi = QiblaFakeAladhanApi()
            )
        )
    }
}

private class QiblaFakeLocationProvider(
    private val result: Result<GeoCoordinates>
) : LocationProvider {
    override suspend fun getCurrentCoordinates(): Result<GeoCoordinates> = result
}

private class UnavailableCompassSensorProvider : CompassSensorProvider {
    override val status: CompassSensorStatus = CompassSensorStatus.Unavailable

    override fun azimuthDegrees(): Flow<Float> = emptyFlow()
}

private class QiblaFakeCountriesNowApi : CountriesNowApi {
    override suspend fun getCitiesByCountry(request: CountryCitiesRequest): CountryCitiesResponse {
        return CountryCitiesResponse(
            error = false,
            msg = "ok",
            data = listOf("Karachi", "Lahore")
        )
    }
}

private class QiblaFakeAladhanApi : AladhanApi {
    override suspend fun getPrayerTimes(latitude: Double, longitude: Double, method: Int): PrayerResponse {
        error("Not used")
    }

    override suspend fun getPrayerTimesByCity(city: String, country: String, method: Int): PrayerResponse {
        error("Not used")
    }

    override suspend fun getCityInfo(city: String, country: String): CityInfoResponse {
        return CityInfoResponse(
            code = 200,
            status = "OK",
            data = CityInfoData(
                latitude = 24.8607,
                longitude = 67.0011,
                timezone = "Asia/Karachi"
            )
        )
    }
}
