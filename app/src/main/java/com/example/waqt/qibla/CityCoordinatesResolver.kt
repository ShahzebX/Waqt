package com.example.waqt.qibla

import com.example.waqt.location.GeoCoordinates
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.settings.PakistaniCities

object CityCoordinatesResolver {
    fun resolve(city: String): GeoCoordinates? {
        return PakistaniCities.coordinatesFor(city)
    }

    fun defaultFallback(): GeoCoordinates {
        return resolve(PrayerRepository.DEFAULT_CITY)
            ?: GeoCoordinates(24.8607, 67.0011)
    }
}
