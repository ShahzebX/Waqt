package com.example.waqt.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.ui.theme.WaqtTheme
import com.example.waqt.viewmodel.SettingsUiState
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsContent_showsCityFieldAndNotificationsToggle() {
        composeRule.setContent {
            WaqtTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState(
                        city = "Karachi",
                        calculationMethod = PrayerRepository.METHOD_KARACHI,
                        notificationsEnabled = true
                    ),
                    cityDraft = "Karachi",
                    onCityDraftChange = {},
                    onCalculationMethodSelected = {},
                    onNotificationsChange = {},
                    onSaveCity = {},
                    onRequestLocationForSave = {},
                    onOpenAppSettings = {}
                )
            }
        }

        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsCityFieldTag).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsNotificationsToggleTag).assertIsDisplayed()
        composeRule.onNodeWithText("Karachi").assertIsDisplayed()
    }

    @Test
    fun settingsContent_notificationsToggleCanBeClicked() {
        var notificationsEnabled = true

        composeRule.setContent {
            WaqtTheme {
                SettingsScreenContent(
                    uiState = SettingsUiState(notificationsEnabled = notificationsEnabled),
                    cityDraft = "Karachi",
                    onCityDraftChange = {},
                    onCalculationMethodSelected = {},
                    onNotificationsChange = { notificationsEnabled = it },
                    onSaveCity = {},
                    onRequestLocationForSave = {},
                    onOpenAppSettings = {}
                )
            }
        }

        composeRule.onNodeWithTag(SettingsNotificationsToggleTag).performClick()
        composeRule.runOnIdle {
            assert(!notificationsEnabled)
        }
    }
}
