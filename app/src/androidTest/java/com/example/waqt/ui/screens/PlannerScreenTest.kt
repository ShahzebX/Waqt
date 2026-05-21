package com.example.waqt.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.waqt.model.Task
import com.example.waqt.ui.theme.WaqtTheme
import com.example.waqt.viewmodel.PlannerUiState
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PlannerScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun plannerContent_showsInlineQuickAdd() {
        composeRule.setContent {
            WaqtTheme {
                PlannerScreenContent(
                    uiState = PlannerUiState(date = LocalDate.of(2026, 5, 16)),
                    now = java.time.LocalTime.of(10, 0),
                    taskInput = "",
                    onTaskInputChange = {},
                    onTaskSubmit = {},
                    onTaskDoneChange = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag(PlannerQuickAddFieldTag).assertIsDisplayed()
        composeRule.onNodeWithText("Quick add").assertIsDisplayed()
    }

    @Test
    fun plannerContent_showsPersistedTask() {
        composeRule.setContent {
            WaqtTheme {
                PlannerScreenContent(
                    uiState = PlannerUiState(
                        date = LocalDate.of(2026, 5, 16),
                        tasks = listOf(
                            Task(
                                id = 1,
                                title = "Review notes",
                                subject = "",
                                slotStart = "--",
                                slotEnd = "--",
                                date = "2026-05-16"
                            )
                        )
                    ),
                    now = java.time.LocalTime.of(10, 0),
                    taskInput = "",
                    onTaskInputChange = {},
                    onTaskSubmit = {},
                    onTaskDoneChange = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithText("Review notes").assertIsDisplayed()
    }
}
