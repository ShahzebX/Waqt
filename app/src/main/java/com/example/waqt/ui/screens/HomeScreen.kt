package com.example.waqt.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.viewmodel.PrayerViewModel
import com.example.waqt.viewmodel.PrayerViewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val prayerViewModel: PrayerViewModel = viewModel(
        factory = PrayerViewModelFactory(context)
    )
    val uiState by prayerViewModel.uiState.collectAsState()
    var cityInput by rememberSaveable { mutableStateOf("Karachi") }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResult ->
        val locationGranted = permissionResult.values.any { it }
        if (locationGranted) {
            prayerViewModel.loadPrayerTimesFromCurrentLocation()
        } else {
            prayerViewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            prayerViewModel.loadPrayerTimesFromCurrentLocation()
        } else if (!permissionRequested) {
            permissionRequested = true
            locationPermissionLauncher.launch(locationPermissions)
        } else {
            prayerViewModel.onLocationPermissionDenied()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.prayers.isNotEmpty() -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Prayer Times")
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.prayers.forEach { prayer ->
                        val displayTime = formatPrayerTime(prayer.time)
                        Text(text = "${prayer.name}: $displayTime")
                    }
                }
            }
            uiState.requiresManualLocationInput -> {
                ManualCityFallback(
                    city = cityInput,
                    message = uiState.errorMessage,
                    onCityChange = { cityInput = it },
                    onLoadCityPrayerTimes = {
                        prayerViewModel.loadPrayerTimesByCity(cityInput)
                    },
                    onRequestLocation = {
                        locationPermissionLauncher.launch(locationPermissions)
                    }
                )
            }
            uiState.errorMessage != null -> Text(text = uiState.errorMessage ?: "")
            else -> Text(text = "No prayer times loaded.")
        }
    }
}

@Composable
private fun ManualCityFallback(
    city: String,
    message: String?,
    onCityChange: (String) -> Unit,
    onLoadCityPrayerTimes: () -> Unit,
    onRequestLocation: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Allow location for automatic prayer times.")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message ?: "Or enter your city manually.")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            label = { Text("City") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onLoadCityPrayerTimes,
            enabled = city.isNotBlank()
        ) {
            Text(text = "Load by city")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRequestLocation) {
            Text(text = "Use GPS instead")
        }
    }
}

private val prayerInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val prayerOutputFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private fun formatPrayerTime(rawTime: String): String {
    return try {
        LocalTime.parse(rawTime, prayerInputFormatter).format(prayerOutputFormatter)
    } catch (exception: DateTimeParseException) {
        rawTime
    }
}

private fun Context.hasLocationPermission(): Boolean {
    val finePermission = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarsePermission = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return finePermission || coarsePermission
}
