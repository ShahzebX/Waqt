package com.example.waqt.qibla

import com.example.waqt.location.GeoCoordinates
import com.example.waqt.repository.PrayerRepository

object CityCoordinatesResolver {
    private val knownCities = mapOf(
        "karachi" to GeoCoordinates(24.8607, 67.0011),
        "lahore" to GeoCoordinates(31.5204, 74.3587),
        "islamabad" to GeoCoordinates(33.6844, 73.0479),
        "rawalpindi" to GeoCoordinates(33.5651, 73.0169),
        "faisalabad" to GeoCoordinates(31.4504, 73.1350),
        "multan" to GeoCoordinates(30.1575, 71.5249),
        "peshawar" to GeoCoordinates(34.0151, 71.5249),
        "quetta" to GeoCoordinates(30.1798, 66.9750),
        "dubai" to GeoCoordinates(25.2048, 55.2708),
        "riyadh" to GeoCoordinates(24.7136, 46.6753),
        "jeddah" to GeoCoordinates(21.4858, 39.1925),
        "istanbul" to GeoCoordinates(41.0082, 28.9784),
        "london" to GeoCoordinates(51.5074, -0.1278),
        "new york" to GeoCoordinates(40.7128, -74.0060)
    )

    fun resolve(city: String): GeoCoordinates? {
        val normalized = city.trim().lowercase()
        if (normalized.isEmpty()) return null
        return knownCities[normalized]
    }

    fun defaultFallback(): GeoCoordinates {
        return resolve(PrayerRepository.DEFAULT_CITY)
            ?: GeoCoordinates(24.8607, 67.0011)
    }
}
