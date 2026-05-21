package com.example.waqt.viewmodel

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.location.GeoCoordinates
import com.example.waqt.location.LocationProvider
import com.example.waqt.model.Prayer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `permission denied enables manual fallback state`() {
        val viewModel = createViewModel()

        viewModel.onLocationPermissionDenied()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.requiresManualLocationInput)
        assertEquals("Location permission denied. Enter your city manually.", uiState.errorMessage)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `loading from gps success populates prayers`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            locationProvider = FakeLocationProvider(Result.success(GeoCoordinates(24.8607, 67.0011))),
            api = FakeAladhanApi(response = samplePrayerResponse())
        )

        viewModel.loadPrayerTimesFromCurrentLocation()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.requiresManualLocationInput)
        assertTrue(uiState.errorMessage == null)
        assertEquals(5, uiState.prayers.size)
        assertEquals("Fajr", uiState.prayers.first().name)
    }

    @Test
    fun `gps failure switches to manual fallback`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            locationProvider = FakeLocationProvider(Result.failure(IOException("No location fix")))
        )

        viewModel.loadPrayerTimesFromCurrentLocation()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.requiresManualLocationInput)
        assertEquals(
            "Allow location to auto-load prayer times, or enter your city manually.",
            uiState.errorMessage
        )
        assertTrue(uiState.prayers.isEmpty())
    }

    @Test
    fun `manual city load clears fallback and returns prayers`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            api = FakeAladhanApi(response = samplePrayerResponse())
        )
        viewModel.onLocationPermissionDenied()

        viewModel.loadPrayerTimesByCity("Karachi")
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.requiresManualLocationInput)
        assertEquals(5, uiState.prayers.size)
    }

    @Test
    fun `successful load schedules notifications when enabled`() = runTest(dispatcher) {
        val scheduler = RecordingScheduler()
        val viewModel = createViewModel(
            api = FakeAladhanApi(response = samplePrayerResponse()),
            notificationScheduler = scheduler
        )

        viewModel.loadPrayerTimesByCity("Karachi")
        advanceUntilIdle()

        assertEquals(1, scheduler.scheduledBatches)
        assertEquals(5, scheduler.lastBatchSize)
    }

    private fun createViewModel(
        locationProvider: LocationProvider = FakeLocationProvider(Result.success(GeoCoordinates(24.8607, 67.0011))),
        api: AladhanApi = FakeAladhanApi(response = samplePrayerResponse()),
        settingsDataSource: InMemoryUserSettingsDataSource = InMemoryUserSettingsDataSource(
            UserSettings(notificationsEnabled = true)
        ),
        notificationScheduler: PrayerNotificationScheduler = RecordingScheduler()
    ): PrayerViewModel {
        val repository = PrayerRepository(
            api = api,
            prayerDao = FakePrayerDao()
        )
        return PrayerViewModel(
            repository = repository,
            locationProvider = locationProvider,
            settingsDataSource = settingsDataSource,
            notificationScheduler = notificationScheduler
        )
    }
}

private class FakeLocationProvider(
    private val result: Result<GeoCoordinates>
) : LocationProvider {
    override suspend fun getCurrentCoordinates(): Result<GeoCoordinates> = result
}

private class FakeAladhanApi(
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

    override suspend fun getCityInfo(city: String, country: String) =
        com.example.waqt.testutil.TestCityInfo.karachi()
}

private class FakePrayerDao : PrayerDao {
    private val stored = mutableListOf<PrayerEntity>()

    override suspend fun insertPrayers(prayers: List<PrayerEntity>): List<Long> {
        prayers.forEach { prayer ->
            stored.removeAll { it.id == prayer.id }
            stored.add(prayer)
        }
        return prayers.mapIndexed { index, _ -> index.toLong() + 1L }
    }

    override suspend fun getPrayersByDate(date: String): List<PrayerEntity> {
        return stored.filter { it.date == date }.sortedBy { it.epochMs }
    }
}

private class RecordingScheduler : PrayerNotificationScheduler {
    var scheduledBatches: Int = 0
    var lastBatchSize: Int = 0

    override suspend fun schedule(prayers: List<Prayer>) {
        scheduledBatches += 1
        lastBatchSize = prayers.size
    }
}

private fun samplePrayerResponse(): PrayerResponse {
    return PrayerResponse(
        data = PrayerData(
            timings = Timings(
                fajr = "04:22 (PKT)",
                sunrise = "05:47 (PKT)",
                dhuhr = "12:15 (PKT)",
                asr = "15:42 (PKT)",
                maghrib = "18:43 (PKT)",
                isha = "20:02 (PKT)"
            ),
            date = DateInfo(
                readable = "07 May 2026",
                hijri = HijriDate(
                    date = "10-11-1447",
                    month = HijriMonth(
                        en = "Dhu al-Qadah",
                        ar = "ذُو ٱلْقَعْدَة"
                    )
                ),
                gregorian = GregorianDate(date = "07-05-2026")
            )
        )
    )
}
