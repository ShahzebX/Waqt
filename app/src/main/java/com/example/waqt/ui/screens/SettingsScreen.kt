package com.example.waqt.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.ui.components.CalculationMethodSelector
import com.example.waqt.ui.components.PakistaniCityAutocompleteField
import com.example.waqt.viewmodel.SettingsUiState
import com.example.waqt.viewmodel.SettingsViewModel
import com.example.waqt.viewmodel.SettingsViewModelFactory

internal const val SettingsCityFieldTag = "settings_city_field"
internal const val SettingsNotificationsToggleTag = "settings_notifications_toggle"
internal const val SettingsSaveCityButtonTag = "settings_save_city_button"

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    var cityDraft by rememberSaveable(uiState.city) { mutableStateOf(uiState.city) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onNotificationsEnabledChange(true)
        } else {
            viewModel.onNotificationPermissionDenied()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            viewModel.refreshFromCurrentLocation()
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        cityDraft = cityDraft,
        onCityDraftChange = {
            cityDraft = it
            viewModel.onCityInputChange(it)
        },
        onCitySuggestionSelected = { selected ->
            cityDraft = selected
            viewModel.onCitySuggestionSelected(selected)
        },
        onCalculationMethodSelected = viewModel::onCalculationMethodSelected,
        onNotificationsChange = { enabled ->
            if (enabled && context.needsNotificationPermission() && !context.hasNotificationPermission()) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onNotificationsEnabledChange(enabled)
            }
        },
        onSaveCity = {
            cityDraft = cityDraft.trim()
            viewModel.onCityInputChange(cityDraft)
            viewModel.saveCityAndRefreshPrayerTimes()
        },
        onRequestLocationForSave = {
            if (context.hasLocationPermission()) {
                viewModel.refreshFromCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        onOpenAppSettings = { context.openAppSettings() }
    )
}

@Composable
internal fun SettingsScreenContent(
    uiState: SettingsUiState,
    cityDraft: String,
    onCityDraftChange: (String) -> Unit,
    onCitySuggestionSelected: (String) -> Unit = {},
    onCalculationMethodSelected: (Int) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onSaveCity: () -> Unit,
    onRequestLocationForSave: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Location, prayer calculation, and reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingsSectionCard(title = "Location") {
                Text(
                    text = when {
                        uiState.isLoadingCities -> "Loading Pakistan cities…"
                        uiState.pakistanCityCount > 0 ->
                            "${uiState.pakistanCityCount} cities loaded. Pick one and save to update prayer times."
                        else -> "Choose a city in Pakistan. Prayer times and Qibla update when you save."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                PakistaniCityAutocompleteField(
                    value = cityDraft,
                    onValueChange = onCityDraftChange,
                    suggestions = uiState.citySuggestions,
                    onSuggestionSelected = onCitySuggestionSelected,
                    enabled = !uiState.isSaving,
                    fieldTestTag = SettingsCityFieldTag
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSaveCity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SettingsSaveCityButtonTag),
                    enabled = cityDraft.isNotBlank() && !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Save & update prayer times")
                    }
                }
                TextButton(
                    onClick = onRequestLocationForSave,
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(text = "Refresh using GPS")
                }
            }
        }

        item {
            SettingsSectionCard(title = "Prayer calculation") {
                CalculationMethodSelector(
                    selectedMethod = uiState.calculationMethod,
                    onMethodChange = onCalculationMethodSelected
                )
            }
        }

        item {
            SettingsSectionCard(title = "Notifications") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prayer reminders",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Notify 10 minutes before each prayer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                        modifier = Modifier.testTag(SettingsNotificationsToggleTag),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                if (uiState.notificationsEnabled && LocalContext.current.needsNotificationPermission()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onOpenAppSettings,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(text = "Open notification settings")
                    }
                }
            }
        }

        item {
            SettingsSectionCard(title = "Display") {
                SettingsInfoRow(label = "Theme", value = "Midnight Blue & Gold")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsInfoRow(label = "App version", value = "1.0")
            }
        }

        uiState.successMessage?.let { message ->
            item {
                FeedbackText(text = message, isError = false)
            }
        }
        uiState.infoMessage?.let { message ->
            item {
                FeedbackText(text = message, isError = false)
            }
        }
        uiState.errorMessage?.let { message ->
            item {
                FeedbackText(text = message, isError = true)
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FeedbackText(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
}

private fun Context.hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.needsNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()
}

private fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
