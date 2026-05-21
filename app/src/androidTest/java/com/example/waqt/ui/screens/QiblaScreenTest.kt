package com.example.waqt.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.waqt.qibla.CompassSensorStatus
import com.example.waqt.ui.theme.WaqtTheme
import com.example.waqt.viewmodel.QiblaLocationSource
import com.example.waqt.viewmodel.QiblaUiState
import org.junit.Rule
import org.junit.Test

class QiblaScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun qiblaContent_showsCompassAndBearing() {
        composeRule.setContent {
            WaqtTheme {
                QiblaScreenContent(
                    uiState = QiblaUiState(
                        qiblaBearing = 281f,
                        azimuth = 12f,
                        needleRotation = 269f,
                        compassSensorStatus = CompassSensorStatus.Available,
                        locationSource = QiblaLocationSource.SavedCity,
                        locationLabel = "Karachi"
                    ),
                    onRequestLocation = {}
                )
            }
        }

        composeRule.onNodeWithText("Qibla Compass").assertIsDisplayed()
        composeRule.onNodeWithTag(QiblaCompassTag).assertIsDisplayed()
        composeRule.onNodeWithTag(QiblaBearingTag).assertIsDisplayed()
        composeRule.onNodeWithText("281°").assertIsDisplayed()
    }

    @Test
    fun qiblaContent_showsGpsFallbackActionWhenManualLocationRequired() {
        composeRule.setContent {
            WaqtTheme {
                QiblaScreenContent(
                    uiState = QiblaUiState(
                        qiblaBearing = 281f,
                        requiresManualLocation = true,
                        compassSensorStatus = CompassSensorStatus.Unavailable,
                        infoMessage = "Location off — Qibla bearing uses your saved city."
                    ),
                    onRequestLocation = {}
                )
            }
        }

        composeRule.onNodeWithText("Use GPS for Qibla").assertIsDisplayed()
    }
}
