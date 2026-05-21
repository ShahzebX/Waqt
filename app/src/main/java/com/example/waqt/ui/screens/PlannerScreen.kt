package com.example.waqt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.model.Task
import com.example.waqt.ui.components.WaqtCard
import com.example.waqt.ui.components.WaqtCardVariant
import com.example.waqt.ui.components.WaqtEmptyState
import com.example.waqt.ui.components.WaqtLoadingState
import com.example.waqt.ui.components.WaqtPulsingDot
import com.example.waqt.ui.components.WaqtScreenHeader
import com.example.waqt.ui.components.WaqtSectionTitle
import com.example.waqt.ui.theme.CardShape
import com.example.waqt.ui.theme.GoldGlow
import com.example.waqt.ui.theme.SecondaryGold
import com.example.waqt.viewmodel.PlannerUiState
import com.example.waqt.viewmodel.PlannerViewModel
import com.example.waqt.viewmodel.PlannerViewModelFactory
import com.example.waqt.viewmodel.StudySlot
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal const val PlannerSlotsEmptyStateTag = "planner_slots_empty_state"
internal const val PlannerTasksEmptyStateTag = "planner_tasks_empty_state"
internal const val PlannerQuickAddFieldTag = "planner_quick_add_field"

@Composable
fun PlannerScreen() {
    val context = LocalContext.current
    val plannerViewModel: PlannerViewModel = viewModel(factory = PlannerViewModelFactory(context))
    val uiState by plannerViewModel.uiState.collectAsState()
    var taskInput by rememberSaveable { mutableStateOf("") }
    var now by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        plannerViewModel.loadPlannerForToday()
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(60_000)
        }
    }

    PlannerScreenContent(
        uiState = uiState,
        now = now,
        taskInput = taskInput,
        onTaskInputChange = { taskInput = it },
        onTaskSubmit = {
            plannerViewModel.addTask(taskInput)
            taskInput = ""
        },
        onTaskDoneChange = plannerViewModel::toggleTaskDone
    )
}

@Composable
internal fun PlannerScreenContent(
    uiState: PlannerUiState,
    now: LocalTime,
    taskInput: String,
    onTaskInputChange: (String) -> Unit,
    onTaskSubmit: () -> Unit,
    onTaskDoneChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> WaqtLoadingState()
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        WaqtScreenHeader(
                            title = "Planner",
                            subtitle = uiState.date.format(dateFormatter),
                            badge = "${uiState.slots.size} slots"
                        )
                    }
                    item {
                        InlineTaskInput(
                            value = taskInput,
                            onValueChange = onTaskInputChange,
                            onSubmit = onTaskSubmit
                        )
                    }
                    item {
                        TimelineContainer {
                            if (uiState.slots.isEmpty()) {
                                WaqtEmptyState(
                                    modifier = Modifier.testTag(PlannerSlotsEmptyStateTag),
                                    title = "No study slots yet",
                                    message = "Load prayer times from Home to generate today's timeline."
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    uiState.slots.forEach { slot ->
                                        StudySlotRow(slot = slot, isActive = now.isWithin(slot))
                                    }
                                }
                            }
                        }
                    }
                    item {
                        if (uiState.tasks.isEmpty()) {
                            WaqtEmptyState(
                                modifier = Modifier.testTag(PlannerTasksEmptyStateTag),
                                title = "No tasks added",
                                message = "Use quick add above to set today's focus."
                            )
                        } else {
                            TaskList(
                                tasks = uiState.tasks,
                                onTaskDoneChange = onTaskDoneChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineTaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    WaqtCard(variant = WaqtCardVariant.Glass) {
        WaqtSectionTitle(text = "Quick add focus")
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PlannerQuickAddFieldTag)
                .onKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                        onSubmit()
                        true
                    } else {
                        false
                    }
                },
            placeholder = { Text(text = "Press Enter to add task") },
            singleLine = true,
            shape = CardShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SecondaryGold,
                focusedLabelColor = SecondaryGold,
                cursorColor = SecondaryGold
            )
        )
    }
}

@Composable
private fun TimelineContainer(content: @Composable () -> Unit) {
    Row {
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(3.dp)
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun StudySlotRow(
    slot: StudySlot,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        WaqtPulsingDot(
            active = isActive,
            modifier = Modifier.offset(y = 18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        WaqtCard(
            modifier = Modifier.fillMaxWidth(),
            variant = if (isActive) WaqtCardVariant.Glass else WaqtCardVariant.Elevated
        ) {
            Text(
                text = "After ${slot.anchorPrayer}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${slot.start.format(timeFormatter)} – ${slot.end.format(timeFormatter)}",
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${slot.duration.toMinutes()} min focus window",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "● Active now",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onTaskDoneChange: (Int, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WaqtSectionTitle(text = "Today's focus")
        tasks.forEach { task ->
            WaqtCard(
                modifier = Modifier.fillMaxWidth(),
                variant = if (task.isDone) WaqtCardVariant.Glass else WaqtCardVariant.Elevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { checked -> onTaskDoneChange(task.id, checked) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SecondaryGold,
                            checkmarkColor = MaterialTheme.colorScheme.onSecondary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (task.isDone) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (task.isDone) "Done" else "Planned",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryGold,
                        modifier = Modifier
                            .background(GoldGlow, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun LocalTime.isWithin(slot: StudySlot): Boolean {
    return !isBefore(slot.start) && isBefore(slot.end)
}

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
