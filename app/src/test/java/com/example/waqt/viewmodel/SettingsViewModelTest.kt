package com.example.waqt.viewmodel

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.location.GeoCoordinates
import com.example.waqt.location.LocationProvider
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.DateInfo
import com.example.waqt.network.GregorianDate
import com.example.waqt.network.HijriDate
import com.example.waqt.network.HijriMonth
import com.example.waqt.network.PrayerData
import com.example.waqt.network.PrayerResponse
import com.example.waqt.network.Timings
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.settings.InMemoryUserSettingsDataSource
import com.example.waqt.settings.UserSettings
import com.example.waqt.worker.PrayerNotificationScheduler
import com.example.waqt.model.Prayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
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
    fun `toggling notifications off persists setting`() = runTest(dispatcher) {
        val settings = InMemoryUserSettingsDataSource(UserSettings(notificationsEnabled = true))
        val viewModel = createViewModel(settings = settings)

        viewModel.onNotificationsEnabledChange(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.notificationsEnabled)
        assertEquals("Prayer reminders disabled.", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `calculation method change updates datastore`() = runTest(dispatcher) {
        val settings = InMemoryUserSettingsDataSource()
        val viewModel = createViewModel(settings = settings)

        viewModel.onCalculationMethodSelected(PrayerRepository.METHOD_ISNA)
        advanceUntilIdle()

        assertEquals(PrayerRepository.METHOD_ISNA, viewModel.uiState.value.calculationMethod)
    }

    @Test
    fun `save city refreshes prayer times`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            api = SettingsFakeAladhanApi(response = settingsSamplePrayerResponse())
        )
        viewModel.onCityInputChange("Karachi")

        viewModel.saveCityAndRefreshPrayerTimes()
        advanceUntilIdle()

        assertEquals("Prayer times updated for Karachi.", viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `empty city shows validation error`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onCityInputChange("   ")

        viewModel.saveCityAndRefreshPrayerTimes()
        advanceUntilIdle()

        assertEquals("Enter a city name.", viewModel.uiState.value.errorMessage)
    }

    private fun createViewModel(
        settings: InMemoryUserSettingsDataSource = InMemoryUserSettingsDataSource(),
        api: AladhanApi = SettingsFakeAladhanApi(response = settingsSamplePrayerResponse()),
        locationProvider: LocationProvider = SettingsFakeLocationProvider(
            Result.success(GeoCoordinates(24.8607, 67.0011))
        ),
        scheduler: PrayerNotificationScheduler = RecordingNotificationScheduler()
    ): SettingsViewModel {
        return SettingsViewModel(
            repository = PrayerRepository(api = api, prayerDao = SettingsFakePrayerDao()),
            locationProvider = locationProvider,
            settingsDataSource = settings,
            notificationScheduler = scheduler
        )
    }
}

private class SettingsFakeLocationProvider(
    private val result: Result<GeoCoordinates>
) : LocationProvider {
    override suspend fun getCurrentCoordinates(): Result<GeoCoordinates> = result
}

private class RecordingNotificationScheduler : PrayerNotificationScheduler {
    var scheduledCount: Int = 0

    override suspend fun schedule(prayers: List<Prayer>) {
        scheduledCount = prayers.size
    }
}

private class SettingsFakeAladhanApi(
    private val response: PrayerResponse? = null,
    private val error: Exception? = null
) : AladhanApi {
    override suspend fun getPrayerTimes(latitude: Double, longitude: Double, method: Int): PrayerResponse {
        error?.let { throw it }
        return checkNotNull(response)
    }

    override suspend fun getPrayerTimesByCity(city: String, country: String, method: Int): PrayerResponse {
        error?.let { throw it }
        return checkNotNull(response)
    }
}

private class SettingsFakePrayerDao : PrayerDao {
    override suspend fun insertPrayers(prayers: List<PrayerEntity>): List<Long> {
        return prayers.mapIndexed { index, _ -> index.toLong() + 1L }
    }

    override suspend fun getPrayersByDate(date: String): List<PrayerEntity> = emptyList()
}

private fun settingsSamplePrayerResponse(): PrayerResponse {
    return PrayerResponse(
        data = PrayerData(
            timings = Timings(
                fajr = "04:22",
                sunrise = "05:47",
                dhuhr = "12:15",
                asr = "15:42",
                maghrib = "18:43",
                isha = "20:02"
            ),
            date = DateInfo(
                readable = "07 May 2026",
                hijri = HijriDate(
                    date = "10-11-1447",
                    month = HijriMonth(en = "Dhu al-Qadah", ar = "ذُو ٱلْقَعْدَة")
                ),
                gregorian = GregorianDate(date = "07-05-2026")
            )
        )
    )
}
