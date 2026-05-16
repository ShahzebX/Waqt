package com.example.waqt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.model.Prayer
import com.example.waqt.repository.PrayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class SlotType {
    SHORT_STUDY,
    STUDY,
    BREAK
}

data class StudySlot(
    val start: LocalTime,
    val end: LocalTime,
    val duration: Duration,
    val anchorPrayer: String,
    val slotType: SlotType = SlotType.STUDY
)

data class PlannerUiState(
    val date: LocalDate = LocalDate.now(),
    val prayers: List<Prayer> = emptyList(),
    val slots: List<StudySlot> = emptyList(),
    val tasks: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PlannerViewModel(
    private val repository: PrayerRepository,
    private val currentDateProvider: () -> LocalDate = LocalDate::now
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    fun loadPlannerForToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val date = currentDateProvider()
            val cached = repository.getCachedPrayers(date.toString())
            if (cached.isEmpty()) {
                _uiState.update {
                    it.copy(
                        date = date,
                        prayers = emptyList(),
                        slots = emptyList(),
                        isLoading = false,
                        errorMessage = "Prayer times unavailable. Load from Home first."
                    )
                }
            } else {
                val prayers = cached.sortedBy { it.epochMs }.map { it.toDomain() }
                val slots = generateStudySlots(prayers)
                _uiState.update {
                    it.copy(
                        date = date,
                        prayers = prayers,
                        slots = slots,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun addTask(task: String) {
        val trimmed = task.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { current ->
            current.copy(tasks = current.tasks + trimmed)
        }
    }

    fun generateStudySlots(prayers: List<Prayer>): List<StudySlot> {
        val sorted = prayers.mapNotNull { prayer ->
            val time = prayer.parseTime() ?: return@mapNotNull null
            prayer to time
        }.sortedBy { it.second }

        if (sorted.size < 2) return emptyList()

        return buildList {
            sorted.zipWithNext().forEach { (current, next) ->
                val start = current.second
                val end = next.second
                if (!end.isAfter(start)) return@forEach

                val gapMinutes = Duration.between(start, end).toMinutes()
                when {
                    gapMinutes < 45 -> Unit
                    gapMinutes < 90 -> add(
                        StudySlot(
                            start = start,
                            end = end,
                            duration = Duration.between(start, end),
                            anchorPrayer = current.first.name,
                            slotType = SlotType.SHORT_STUDY
                        )
                    )
                    gapMinutes < 180 -> add(
                        StudySlot(
                            start = start,
                            end = end,
                            duration = Duration.between(start, end),
                            anchorPrayer = current.first.name,
                            slotType = SlotType.STUDY
                        )
                    )
                    else -> {
                        val studyEnd = start.plusMinutes(90)
                        add(
                            StudySlot(
                                start = start,
                                end = studyEnd,
                                duration = Duration.between(start, studyEnd),
                                anchorPrayer = current.first.name,
                                slotType = SlotType.STUDY
                            )
                        )
                        if (end.isAfter(studyEnd)) {
                            add(
                                StudySlot(
                                    start = studyEnd,
                                    end = end,
                                    duration = Duration.between(studyEnd, end),
                                    anchorPrayer = current.first.name,
                                    slotType = SlotType.BREAK
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private val prayerInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")

private fun Prayer.parseTime(): LocalTime? {
    return try {
        LocalTime.parse(time, prayerInputFormatter)
    } catch (exception: DateTimeParseException) {
        null
    }
}

private fun PrayerEntity.toDomain(): Prayer {
    return Prayer(
        name = name,
        time = time,
        date = date
    )
}
