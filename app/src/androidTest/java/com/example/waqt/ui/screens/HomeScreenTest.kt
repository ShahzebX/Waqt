package com.example.waqt.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.waqt.model.Prayer
import com.example.waqt.ui.theme.WaqtTheme
import com.example.waqt.viewmodel.PrayerUiState
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeContent_showsNextPrayerCountdown() {
        val uiState = PrayerUiState(
            prayers = listOf(
                Prayer(name = "Fajr", time = "04:22", date = "2026-05-07"),
                Prayer(name = "Dhuhr", time = "12:15", date = "2026-05-07"),
                Prayer(name = "Asr", time = "15:42", date = "2026-05-07"),
                Prayer(name = "Maghrib", time = "18:43", date = "2026-05-07"),
                Prayer(name = "Isha", time = "20:02", date = "2026-05-07")
            )
        )

        composeRule.setContent {
            WaqtTheme {
                HomeScreenContent(
                    uiState = uiState,
                    city = "Karachi",
                    now = LocalDateTime.of(2026, 5, 7, 11, 15, 0),
                    onCityChange = {},
                    onLoadCityPrayerTimes = {},
                    onRequestLocation = {},
                    onViewPlanner = {}
                )
            }
        }

        composeRule.onNodeWithText("Next Prayer").assertIsDisplayed()
        composeRule.onNodeWithText("Dhuhr").assertIsDisplayed()
        composeRule.onNodeWithTag(HomeCountdownTag).assertTextEquals("01:00:00")
    }

    @Test
    fun homeContent_showsManualFallbackWhenLocationDenied() {
        val uiState = PrayerUiState(
            requiresManualLocationInput = true,
            errorMessage = "Location permission denied. Enter your city manually."
        )

        composeRule.setContent {
            WaqtTheme {
                HomeScreenContent(
                    uiState = uiState,
                    city = "Karachi",
                    now = LocalDateTime.of(2026, 5, 7, 11, 15, 0),
                    onCityChange = {},
                    onLoadCityPrayerTimes = {},
                    onRequestLocation = {},
                    onViewPlanner = {}
                )
            }
        }

        composeRule.onNodeWithTag(HomeManualFallbackTag).assertIsDisplayed()
        composeRule.onNodeWithText("Load by city").assertIsDisplayed()
        composeRule.onNodeWithText("Use GPS instead").assertIsDisplayed()
    }
}
