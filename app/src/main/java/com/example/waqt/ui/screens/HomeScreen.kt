package com.example.waqt.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.model.Prayer
import com.example.waqt.ui.theme.DarkGreen
import com.example.waqt.viewmodel.PrayerUiState
import com.example.waqt.viewmodel.PrayerViewModel
import com.example.waqt.viewmodel.PrayerViewModelFactory
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.chrono.HijrahChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

internal const val HomeCountdownTag = "home_countdown"
internal const val HomeManualFallbackTag = "home_manual_fallback"

@Composable
fun HomeScreen(
    onViewPlanner: () -> Unit = {}
) {
    val context = LocalContext.current
    val prayerViewModel: PrayerViewModel = viewModel(factory = PrayerViewModelFactory(context))
    val uiState by prayerViewModel.uiState.collectAsState()
    var cityInput by rememberSaveable { mutableStateOf("Karachi") }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }

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

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    HomeScreenContent(
        uiState = uiState,
        city = cityInput,
        now = now,
        onCityChange = { cityInput = it },
        onLoadCityPrayerTimes = { prayerViewModel.loadPrayerTimesByCity(cityInput) },
        onRequestLocation = { locationPermissionLauncher.launch(locationPermissions) },
        onViewPlanner = onViewPlanner
    )
}

@Composable
internal fun HomeScreenContent(
    uiState: PrayerUiState,
    city: String,
    now: LocalDateTime,
    onCityChange: (String) -> Unit,
    onLoadCityPrayerTimes: () -> Unit,
    onRequestLocation: () -> Unit,
    onViewPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreen),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading && uiState.prayers.isEmpty() -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primaryContainer
            )
            uiState.prayers.isNotEmpty() -> {
                val displayDate = uiState.prayers.firstOrNull()?.date.toDisplayDate(now.toLocalDate())
                val nextPrayerInfo = remember(uiState.prayers, now) {
                    calculateNextPrayer(prayers = uiState.prayers, now = now, baseDate = displayDate)
                }
                val remainingBlocks = nextPrayerInfo?.remainingStudyBlocks(uiState.prayers.size) ?: 0

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DateHeader(
                            gregorianDate = displayDate.format(gregorianDateFormatter),
                            hijriDate = displayDate.toHijriDate()
                        )
                    }
                    item {
                        NextPrayerCard(
                            prayerName = nextPrayerInfo?.prayer?.name ?: "No upcoming prayer",
                            prayerTime = nextPrayerInfo?.prayer?.time?.let(::formatPrayerTime) ?: "--",
                            countdown = nextPrayerInfo?.remainingDuration?.let(::formatCountdown) ?: "--:--:--"
                        )
                    }
                    item {
                        PrayerTimesRow(
                            prayers = uiState.prayers,
                            activePrayerName = nextPrayerInfo?.prayer?.name
                        )
                    }
                    item {
                        PlannerSummaryCard(
                            remainingBlocks = remainingBlocks,
                            onViewPlanner = onViewPlanner
                        )
                    }
                }
            }
            uiState.requiresManualLocationInput -> {
                ManualCityFallback(
                    city = city,
                    message = uiState.errorMessage,
                    onCityChange = onCityChange,
                    onLoadCityPrayerTimes = onLoadCityPrayerTimes,
                    onRequestLocation = onRequestLocation
                )
            }
            uiState.errorMessage != null -> Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
            else -> Text(
                text = "No prayer times loaded.",
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DateHeader(
    gregorianDate: String,
    hijriDate: String
) {
    Column {
        Text(
            text = gregorianDate,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text = hijriDate,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun NextPrayerCard(
    prayerName: String,
    prayerTime: String,
    countdown: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Prayer",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = prayerName,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prayerTime,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = countdown,
                modifier = Modifier.testTag(HomeCountdownTag),
                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
private fun PrayerTimesRow(
    prayers: List<Prayer>,
    activePrayerName: String?
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(prayers, key = { it.name }) { prayer ->
            val active = prayer.name == activePrayerName
            PrayerTimePill(
                prayer = prayer,
                active = active
            )
        }
    }
}

@Composable
private fun PrayerTimePill(
    prayer: Prayer,
    active: Boolean
) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.White.copy(alpha = 0.24f)
    }
    val containerAlpha = if (active) 0.18f else 0.10f

    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = containerAlpha), shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = prayer.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Text(
            text = formatPrayerTime(prayer.time),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun PlannerSummaryCard(
    remainingBlocks: Int,
    onViewPlanner: () -> Unit
) {
    val blockLabel = if (remainingBlocks == 1) {
        "1 study block remaining today"
    } else {
        "$remainingBlocks study blocks remaining today"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.18f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = blockLabel,
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(
                onClick = onViewPlanner,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(text = "View Planner")
            }
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
    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .testTag(HomeManualFallbackTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Allow location for automatic prayer times",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message ?: "Or enter your city manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("City") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onLoadCityPrayerTimes, enabled = city.isNotBlank()) {
                Text(text = "Load by city")
            }
            TextButton(
                onClick = onRequestLocation,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(text = "Use GPS instead")
            }
        }
    }
}

private val prayerInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val prayerOutputFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val gregorianDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
private val hijriDateFormatter: DateTimeFormatter =
    DateTimeFormatter
        .ofPattern("d MMMM yyyy", Locale.getDefault())
        .withChronology(HijrahChronology.INSTANCE)

private data class NextPrayerInfo(
    val prayer: Prayer,
    val index: Int,
    val remainingDuration: Duration,
    val isTomorrow: Boolean
) {
    fun remainingStudyBlocks(totalPrayers: Int): Int {
        if (isTomorrow) return 0
        return (totalPrayers - index - 1).coerceAtLeast(0)
    }
}

private fun calculateNextPrayer(
    prayers: List<Prayer>,
    now: LocalDateTime,
    baseDate: LocalDate
): NextPrayerInfo? {
    val schedule = prayers.mapIndexedNotNull { index, prayer ->
        val prayerTime = prayer.parseTime() ?: return@mapIndexedNotNull null
        Triple(index, prayer, LocalDateTime.of(baseDate, prayerTime))
    }

    if (schedule.isEmpty()) return null

    val nextToday = schedule.firstOrNull { (_, _, dateTime) -> !dateTime.isBefore(now) }
    if (nextToday != null) {
        val remaining = Duration.between(now, nextToday.third).nonNegative()
        return NextPrayerInfo(
            prayer = nextToday.second,
            index = nextToday.first,
            remainingDuration = remaining,
            isTomorrow = false
        )
    }

    val firstPrayer = schedule.first()
    val firstTomorrowDateTime = firstPrayer.third.plusDays(1)
    val remaining = Duration.between(now, firstTomorrowDateTime).nonNegative()
    return NextPrayerInfo(
        prayer = firstPrayer.second,
        index = firstPrayer.first,
        remainingDuration = remaining,
        isTomorrow = true
    )
}

private fun Duration.nonNegative(): Duration {
    return if (isNegative) Duration.ZERO else this
}

private fun formatCountdown(duration: Duration): String {
    val totalSeconds = duration.seconds.coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun String?.toDisplayDate(fallback: LocalDate): LocalDate {
    if (this.isNullOrBlank()) return fallback
    return try {
        LocalDate.parse(this)
    } catch (exception: DateTimeParseException) {
        fallback
    }
}

private fun LocalDate.toHijriDate(): String {
    return try {
        hijriDateFormatter.format(this)
    } catch (exception: RuntimeException) {
        "Hijri date unavailable"
    }
}

private fun Prayer.parseTime(): LocalTime? {
    return try {
        LocalTime.parse(time, prayerInputFormatter)
    } catch (exception: DateTimeParseException) {
        null
    }
}

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
