package com.example.waqt.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FusedLocationProvider(
    context: Context
) : LocationProvider {
    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentCoordinates(): Result<GeoCoordinates> {
        if (!appContext.hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted."))
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (!continuation.isActive) {
                    return@addOnSuccessListener
                }
                if (location == null) {
                    continuation.resume(Result.failure(IllegalStateException("Current location unavailable.")))
                } else {
                    continuation.resume(
                        Result.success(
                            GeoCoordinates(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    )
                }
            }.addOnFailureListener { error ->
                if (!continuation.isActive) {
                    return@addOnFailureListener
                }
                continuation.resume(Result.failure(error))
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
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
