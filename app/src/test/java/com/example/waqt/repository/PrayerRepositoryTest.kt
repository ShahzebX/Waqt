package com.example.waqt.repository

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.DateInfo
import com.example.waqt.network.GregorianDate
import com.example.waqt.network.HijriDate
import com.example.waqt.network.HijriMonth
import com.example.waqt.network.PrayerData
import com.example.waqt.network.PrayerResponse
import com.example.waqt.network.Timings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class PrayerRepositoryTest {
    @Test
    fun `network success returns normalized prayers and caches them`() = runBlocking {
        val dao = FakePrayerDao()
        val api = FakeAladhanApi(response = samplePrayerResponse())
        val repository = PrayerRepository(api = api, prayerDao = dao) { LocalDate.parse("2026-05-07") }

        val result = repository.getPrayerTimes(latitude = 24.8607, longitude = 67.0011)

        assertTrue(result.isSuccess)
        val prayers = result.getOrThrow()
        assertEquals(5, prayers.size)
        assertEquals("Fajr", prayers.first().name)
        assertEquals("04:22", prayers.first().time)
        assertEquals(5, dao.insertedPrayers.size)
    }

    @Test
    fun `returns cached prayers when network fails`() = runBlocking {
        val cachedDate = LocalDate.parse("2026-05-07")
        val cachedEntity = PrayerEntity(
            id = "${cachedDate}_Fajr",
            name = "Fajr",
            time = "04:30",
            date = cachedDate.toString(),
            epochMs = 0L
        )
        val dao = FakePrayerDao(initial = listOf(cachedEntity))
        val api = FakeAladhanApi(error = IOException("No internet"))
        val repository = PrayerRepository(api = api, prayerDao = dao) { cachedDate }

        val result = repository.getPrayerTimes(latitude = 24.8607, longitude = 67.0011)

        assertTrue(result.isSuccess)
        assertEquals("04:30", result.getOrThrow().first().time)
    }

    @Test
    fun `returns failure when network fails and cache is empty`() = runBlocking {
        val today = LocalDate.parse("2026-05-07")
        val dao = FakePrayerDao()
        val api = FakeAladhanApi(error = IOException("No internet"))
        val repository = PrayerRepository(api = api, prayerDao = dao) { today }

        val result = repository.getPrayerTimes(latitude = 24.8607, longitude = 67.0011)

        assertFalse(result.isSuccess)
    }
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
}

private class FakePrayerDao(initial: List<PrayerEntity> = emptyList()) : PrayerDao {
    private val stored = initial.toMutableList()
    val insertedPrayers: List<PrayerEntity>
        get() = stored

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
