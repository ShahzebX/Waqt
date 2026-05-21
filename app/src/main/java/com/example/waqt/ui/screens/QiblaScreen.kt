package com.example.waqt.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waqt.qibla.CompassSensorStatus
import com.example.waqt.ui.theme.PrimaryNavy
import com.example.waqt.ui.theme.SecondaryGold
import com.example.waqt.ui.theme.SoftIceBlue
import com.example.waqt.viewmodel.QiblaLocationSource
import com.example.waqt.viewmodel.QiblaUiState
import com.example.waqt.viewmodel.QiblaViewModel
import com.example.waqt.viewmodel.QiblaViewModelFactory

internal const val QiblaCompassTag = "qibla_compass_canvas"
internal const val QiblaBearingTag = "qibla_bearing_value"
internal const val QiblaMarkerTag = "qibla_direction_marker"
internal const val QiblaAlignedTag = "qibla_aligned_indicator"

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun QiblaScreen() {
    val context = LocalContext.current
    val qiblaViewModel: QiblaViewModel = viewModel(factory = QiblaViewModelFactory(context))
    val uiState by qiblaViewModel.uiState.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResult ->
        val granted = permissionResult.values.any { it }
        if (granted) {
            qiblaViewModel.refreshLocation(useGps = true)
        } else {
            qiblaViewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            qiblaViewModel.refreshLocation(useGps = true)
        } else {
            qiblaViewModel.onLocationPermissionRequired()
            qiblaViewModel.onLocationPermissionDenied()
        }
    }

    QiblaScreenContent(
        uiState = uiState,
        onRequestLocation = { locationPermissionLauncher.launch(locationPermissions) }
    )
}

@Composable
internal fun QiblaScreenContent(
    uiState: QiblaUiState,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Qibla Compass",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Rotate your phone until the needle points to the Kaaba",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        when {
            uiState.isLoadingLocation -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
            else -> {
                QiblaCompassCard(
                    uiState = uiState,
                    modifier = Modifier.testTag(QiblaCompassTag)
                )
            }
        }

        QiblaInfoCard(uiState = uiState)

        if (uiState.requiresManualLocation) {
            Button(
                onClick = onRequestLocation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(text = "Use GPS for Qibla")
            }
        }

        uiState.infoMessage?.let { message ->
            StatusMessageCard(message = message)
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QiblaCompassCard(
    uiState: QiblaUiState,
    modifier: Modifier = Modifier
) {
    val compassActive = uiState.compassSensorStatus == CompassSensorStatus.Available
    val qiblaOffset = if (compassActive) {
        uiState.qiblaOffsetDegrees
    } else {
        uiState.qiblaBearing
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isFacingQibla) {
                Text(
                    text = "Facing Qibla",
                    modifier = Modifier.testTag(QiblaAlignedTag),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f
                    drawCompassFace(center = center, radius = radius)
                    rotate(degrees = qiblaOffset, pivot = center) {
                        drawNeedle(center = center, radius = radius)
                    }
                }
                Text(
                    text = "🕋",
                    fontSize = 32.sp,
                    modifier = Modifier
                        .testTag(QiblaMarkerTag)
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )
            }

            Text(
                text = if (compassActive) {
                    "Align the golden needle with the Kaaba above"
                } else {
                    "Compass unavailable — use the bearing below"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

private fun DrawScope.drawCompassFace(center: Offset, radius: Float) {
    drawCircle(
        color = SoftIceBlue,
        radius = radius,
        center = center
    )
    drawCircle(
        color = PrimaryNavy,
        radius = radius,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
    drawCircle(
        color = PrimaryNavy.copy(alpha = 0.15f),
        radius = radius * 0.08f,
        center = center
    )
}

/** Simple compass needle — points toward the fixed Kaaba when [qiblaOffset] is 0°. */
private fun DrawScope.drawNeedle(center: Offset, radius: Float) {
    val needleLength = radius * 0.72f
    val tip = Offset(center.x, center.y - needleLength)
    drawLine(
        color = SecondaryGold,
        start = center,
        end = tip,
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = PrimaryNavy.copy(alpha = 0.35f),
        start = center,
        end = Offset(center.x, center.y + needleLength * 0.32f),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Composable
private fun QiblaInfoCard(uiState: QiblaUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Bearing to Makkah",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${uiState.qiblaBearing.toInt()}°",
                modifier = Modifier.testTag(QiblaBearingTag),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            uiState.locationLabel?.let { label ->
                Text(
                    text = locationDescription(uiState.locationSource, label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = sensorDescription(uiState.compassSensorStatus),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.compassSensorStatus == CompassSensorStatus.Available) {
                Text(
                    text = "Offset from top: ${uiState.qiblaOffsetDegrees.toInt()}°",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun locationDescription(source: QiblaLocationSource?, label: String): String {
    return when (source) {
        QiblaLocationSource.Gps -> "Based on GPS near $label"
        QiblaLocationSource.SavedCity -> "Based on saved city: $label"
        QiblaLocationSource.DefaultCity -> "Based on default coordinates ($label)"
        null -> "Location pending"
    }
}

private fun sensorDescription(status: CompassSensorStatus): String {
    return when (status) {
        CompassSensorStatus.Available -> "Compass active — rotate until the needle points to the Kaaba."
        CompassSensorStatus.LowAccuracy -> "Compass accuracy is low — move away from metal objects."
        CompassSensorStatus.Unavailable ->
            "Compass sensor unavailable — bearing shown for manual alignment."
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
