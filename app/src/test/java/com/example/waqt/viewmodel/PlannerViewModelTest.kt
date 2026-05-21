package com.example.waqt.viewmodel

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.TaskDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.database.entities.TaskEntity
import com.example.waqt.model.Prayer
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.PrayerResponse
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {
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
    fun `generateStudySlots skips gaps shorter than 45 minutes`() {
        val viewModel = createViewModel()
        val prayers = listOf(
            Prayer(name = "Fajr", time = "05:00", date = "2026-05-16"),
            Prayer(name = "Dhuhr", time = "05:30", date = "2026-05-16")
        )

        val slots = viewModel.generateStudySlots(prayers)

        assertTrue(slots.isEmpty())
    }

    @Test
    fun `generateStudySlots creates short study slot for medium gap`() {
        val viewModel = createViewModel()
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
        val viewModel = createViewModel()
        val prayers = listOf(
            Prayer(name = "Fajr", time = "05:00", date = "2026-05-16"),
            Prayer(name = "Dhuhr", time = "09:10", date = "2026-05-16")
        )

        val slots = viewModel.generateStudySlots(prayers)

        assertEquals(2, slots.size)
        assertEquals(SlotType.STUDY, slots[0].slotType)
        assertEquals(SlotType.BREAK, slots[1].slotType)
    }

    @Test
    fun `addTask persists through task repository`() = runTest(dispatcher) {
        val taskDao = FakeTaskDao()
        val viewModel = PlannerViewModel(
            prayerRepository = PrayerRepository(
                api = UnusedAladhanApi(),
                prayerDao = EmptyPrayerDao()
            ),
            taskRepository = TaskRepository(taskDao),
            currentDateProvider = { java.time.LocalDate.parse("2026-05-16") }
        )

        viewModel.addTask("Review Fiqh")
        advanceUntilIdle()

        assertEquals(1, taskDao.tasks.size)
        assertEquals("Review Fiqh", taskDao.tasks.first().title)
    }

    private fun createViewModel(): PlannerViewModel {
        return PlannerViewModel(
            prayerRepository = PrayerRepository(
                api = UnusedAladhanApi(),
                prayerDao = EmptyPrayerDao()
            ),
            taskRepository = TaskRepository(FakeTaskDao())
        )
    }
}

private class EmptyPrayerDao : PrayerDao {
    override suspend fun insertPrayers(prayers: List<PrayerEntity>): List<Long> = emptyList()

    override suspend fun getPrayersByDate(date: String): List<PrayerEntity> = emptyList()
}

private class FakeTaskDao : TaskDao {
    val tasks = mutableListOf<TaskEntity>()
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())

    override suspend fun insertTask(task: TaskEntity): Long {
        val entity = task.copy(id = tasks.size + 1)
        tasks.add(entity)
        flow.value = tasks.toList()
        return entity.id.toLong()
    }

    override fun getTasksForDate(date: String): Flow<List<TaskEntity>> = flow

    override suspend fun updateTaskDone(id: Int, isDone: Boolean): Int {
        val index = tasks.indexOfFirst { it.id == id }
        if (index < 0) return 0
        tasks[index] = tasks[index].copy(isDone = isDone)
        flow.value = tasks.toList()
        return 1
    }
}

private class UnusedAladhanApi : AladhanApi {
    override suspend fun getPrayerTimes(latitude: Double, longitude: Double, method: Int): PrayerResponse {
        error("Not needed for planner slot tests")
    }

    override suspend fun getPrayerTimesByCity(city: String, country: String, method: Int): PrayerResponse {
        error("Not needed for planner slot tests")
    }

    override suspend fun getCityInfo(city: String, country: String) =
        com.example.waqt.testutil.TestCityInfo.karachi()
}
