package com.example.waqt.location

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
)

interface LocationProvider {
    suspend fun getCurrentCoordinates(): Result<GeoCoordinates>
}
