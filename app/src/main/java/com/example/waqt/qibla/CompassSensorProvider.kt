package com.example.waqt.qibla

import kotlinx.coroutines.flow.Flow

enum class CompassSensorStatus {
    Available,
    Unavailable,
    LowAccuracy
}

interface CompassSensorProvider {
    val status: CompassSensorStatus

    fun azimuthDegrees(): Flow<Float>
}
