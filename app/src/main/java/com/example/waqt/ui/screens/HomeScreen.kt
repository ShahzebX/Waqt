package com.example.waqt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.viewmodel.PrayerViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun HomeScreen(prayerViewModel: PrayerViewModel = viewModel()) {
    val uiState by prayerViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        prayerViewModel.loadDefaultPrayerTimes()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> Text(text = uiState.errorMessage ?: "")
            uiState.prayers.isEmpty() -> Text(text = "No prayer times loaded.")
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Karachi Prayer Times")
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.prayers.forEach { prayer ->
                        val displayTime = formatPrayerTime(prayer.time)
                        Text(text = "${prayer.name}: $displayTime")
                    }
                }
            }
        }
    }
}

private val prayerInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val prayerOutputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private fun formatPrayerTime(rawTime: String): String {
    return try {
        LocalTime.parse(rawTime, prayerInputFormatter).format(prayerOutputFormatter)
    } catch (exception: DateTimeParseException) {
        rawTime
    }
}
