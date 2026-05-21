package com.example.waqt.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.model.Prayer
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.ui.components.CalculationMethodSelector
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
    var cityInput by rememberSaveable(uiState.savedCity) { mutableStateOf(uiState.savedCity) }
    var selectedMethod by rememberSaveable(uiState.selectedMethod) {
        mutableIntStateOf(uiState.selectedMethod)
    }
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResult ->
        val locationGranted = permissionResult.values.any { it }
        if (locationGranted) {
            prayerViewModel.loadPrayerTimesFromCurrentLocation(method = selectedMethod)
        } else {
            prayerViewModel.onLocationPermissionDenied()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            prayerViewModel.onNotificationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            prayerViewModel.loadPrayerTimesFromCurrentLocation(method = selectedMethod)
        } else {
            prayerViewModel.onLocationPermissionRequired()
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
        selectedMethod = selectedMethod,
        now = now,
        onCityChange = { cityInput = it },
        onMethodChange = { method ->
            selectedMethod = method
            prayerViewModel.setCalculationMethod(method)
        },
        onLoadCityPrayerTimes = {
            prayerViewModel.loadPrayerTimesByCity(city = cityInput, method = selectedMethod)
        },
        onRequestLocation = { locationPermissionLauncher.launch(locationPermissions) },
        showNotificationPrompt = context.shouldShowNotificationPrompt(uiState),
        onRequestNotifications = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onViewPlanner = onViewPlanner
    )
}

@Composable
internal fun HomeScreenContent(
    uiState: PrayerUiState,
    city: String,
    selectedMethod: Int = PrayerRepository.DEFAULT_METHOD,
    now: LocalDateTime,
    onCityChange: (String) -> Unit,
    onMethodChange: (Int) -> Unit = {},
    onLoadCityPrayerTimes: () -> Unit,
    onRequestLocation: () -> Unit,
    showNotificationPrompt: Boolean = false,
    onRequestNotifications: () -> Unit = {},
    onViewPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading && uiState.prayers.isEmpty() -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary
            )
            uiState.prayers.isNotEmpty() -> {
                val displayDate = uiState.prayers.firstOrNull()?.date.toDisplayDate(now.toLocalDate())
                val nextPrayerInfo = remember(uiState.prayers, now) {
                    calculateNextPrayer(prayers = uiState.prayers, now = now, baseDate = displayDate)
                }
                val remainingBlocks = nextPrayerInfo?.remainingStudyBlocks(uiState.prayers.size) ?: 0

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
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
                    if (showNotificationPrompt) {
                        item {
                            NotificationPermissionCard(onRequestNotifications = onRequestNotifications)
                        }
                    }
                    if (uiState.infoMessage != null) {
                        item {
                            Text(
                                text = uiState.infoMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            uiState.requiresManualLocationInput -> {
                ManualCityFallback(
                    city = city,
                    selectedMethod = selectedMethod,
                    message = uiState.errorMessage,
                    onCityChange = onCityChange,
                    onMethodChange = onMethodChange,
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
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun DateHeader(
    gregorianDate: String,
    hijriDate: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = gregorianDate,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = hijriDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Prayer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = prayerName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prayerTime,
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = countdown,
                modifier = Modifier.testTag(HomeCountdownTag),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontFeatureSettings = "tnum",
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.secondary
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (active) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val containerColor = if (active) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .background(containerColor, shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = prayer.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatPrayerTime(prayer.time),
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(
                text = "Today's Study Plan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = blockLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewPlanner,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
            ) {
                Text(
                    text = "View Planner",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    onRequestNotifications: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = "Enable prayer reminders",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Allow notifications to get reminders 10 minutes before each prayer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onRequestNotifications,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(text = "Allow notifications")
            }
        }
    }
}

@Composable
private fun ManualCityFallback(
    city: String,
    selectedMethod: Int,
    message: String?,
    onCityChange: (String) -> Unit,
    onMethodChange: (Int) -> Unit,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Allow location for automatic prayer times",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message ?: "Or enter your city manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("City") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            CalculationMethodSelector(
                selectedMethod = selectedMethod,
                onMethodChange = onMethodChange
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onLoadCityPrayerTimes,
                enabled = city.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(text = "Load by city")
            }
            TextButton(
                onClick = onRequestLocation,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
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

private fun Context.hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.shouldShowNotificationPrompt(uiState: PrayerUiState): Boolean {
    return uiState.prayers.isNotEmpty() &&
        uiState.notificationsEnabled &&
        !hasNotificationPermission()
}

private data class CalculationMethodOption(
    val id: Int,
    val label: String
)
