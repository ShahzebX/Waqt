package com.example.waqt.qibla

import com.example.waqt.location.GeoCoordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaCalculator {
    const val MECCA_LATITUDE = 21.4225
    const val MECCA_LONGITUDE = 39.8262

    fun bearingFrom(coordinates: GeoCoordinates): Float {
        return bearingFrom(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        )
    }

    fun bearingFrom(latitude: Double, longitude: Double): Float {
        val userLat = Math.toRadians(latitude)
        val userLng = Math.toRadians(longitude)
        val meccaLat = Math.toRadians(MECCA_LATITUDE)
        val meccaLng = Math.toRadians(MECCA_LONGITUDE)
        val deltaLng = meccaLng - userLng

        val y = sin(deltaLng) * cos(meccaLat)
        val x = cos(userLat) * sin(meccaLat) - sin(userLat) * cos(meccaLat) * cos(deltaLng)
        val bearingRadians = atan2(y, x)
        return normalizeDegrees(Math.toDegrees(bearingRadians).toFloat())
    }

    /**
     * Degrees clockwise from the top of the screen to the Qibla direction.
     * 0° means the phone top is already facing Makkah.
     */
    fun qiblaOffsetFromScreenTop(qiblaBearing: Float, deviceAzimuth: Float): Float {
        return normalizeSignedDegrees(qiblaBearing - deviceAzimuth)
    }

    fun isFacingQibla(qiblaBearing: Float, deviceAzimuth: Float, toleranceDegrees: Float = 10f): Boolean {
        return kotlin.math.abs(qiblaOffsetFromScreenTop(qiblaBearing, deviceAzimuth)) <= toleranceDegrees
    }

    @Deprecated("Use qiblaOffsetFromScreenTop", ReplaceWith("qiblaOffsetFromScreenTop(qiblaBearing, deviceAzimuth)"))
    fun needleRotation(qiblaBearing: Float, deviceAzimuth: Float): Float {
        return qiblaOffsetFromScreenTop(qiblaBearing, deviceAzimuth)
    }

    fun normalizeDegrees(degrees: Float): Float {
        var normalized = degrees % 360f
        if (normalized < 0f) normalized += 360f
        return normalized
    }

    fun normalizeSignedDegrees(degrees: Float): Float {
        var normalized = degrees % 360f
        if (normalized > 180f) normalized -= 360f
        if (normalized < -180f) normalized += 360f
        return normalized
    }
}
