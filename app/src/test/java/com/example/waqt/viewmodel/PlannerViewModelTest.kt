package com.example.waqt.viewmodel

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.model.Prayer
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.PrayerResponse
import com.example.waqt.repository.PrayerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerViewModelTest {
    private val viewModel = PlannerViewModel(
        repository = PrayerRepository(
            api = UnusedAladhanApi(),
            prayerDao = EmptyPrayerDao()
        )
    )

    @Test
    fun `generateStudySlots skips gaps shorter than 45 minutes`() {
        val prayers = listOf(
            Prayer(name = "Fajr", time = "05:00", date = "2026-05-16"),
            Prayer(name = "Dhuhr", time = "05:30", date = "2026-05-16")
        )

        val slots = viewModel.generateStudySlots(prayers)

        assertTrue(slots.isEmpty())
    }

    @Test
    fun `generateStudySlots creates short study slot for medium gap`() {
        val prayers = listOf(
            Prayer(name = "Fajr", time = "05:00", date = "2026-05-16"),
            Prayer(name = "Dhuhr", time = "06:10", date = "2026-05-16")
        )

        val slots = viewModel.generateStudySlots(prayers)

        assertEquals(1, slots.size)
        assertEquals(SlotType.SHORT_STUDY, slots.first().slotType)
    }

    @Test
    fun `generateStudySlots splits long gap into study and break`() {
        val prayers = listOf(
            Prayer(name = "Fajr", time = "05:00", date = "2026-05-16"),
            Prayer(name = "Dhuhr", time = "09:10", date = "2026-05-16")
        )

        val slots = viewModel.generateStudySlots(prayers)

        assertEquals(2, slots.size)
        assertEquals(SlotType.STUDY, slots[0].slotType)
        assertEquals(SlotType.BREAK, slots[1].slotType)
    }
}

private class EmptyPrayerDao : PrayerDao {
    override suspend fun insertPrayers(prayers: List<PrayerEntity>): List<Long> = emptyList()

    override suspend fun getPrayersByDate(date: String): List<PrayerEntity> = emptyList()
}

private class UnusedAladhanApi : AladhanApi {
    override suspend fun getPrayerTimes(latitude: Double, longitude: Double, method: Int): PrayerResponse {
        error("Not needed for planner slot tests")
    }

    override suspend fun getPrayerTimesByCity(city: String, country: String, method: Int): PrayerResponse {
        error("Not needed for planner slot tests")
    }
}
