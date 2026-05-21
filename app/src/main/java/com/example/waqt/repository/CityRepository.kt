package com.example.waqt.repository

import com.example.waqt.location.GeoCoordinates
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.CountriesNowApi
import com.example.waqt.network.CountryCitiesRequest
import com.example.waqt.settings.PakistaniCities
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class CityRepository(
    private val countriesNowApi: CountriesNowApi,
    private val aladhanApi: AladhanApi,
    private val country: String = PrayerRepository.DEFAULT_COUNTRY
) {
    private var pakistanCities: List<String>? = null
    private val coordinatesCache = ConcurrentHashMap<String, GeoCoordinates>()

    suspend fun loadPakistanCities(): Result<List<String>> {
        pakistanCities?.let { return Result.success(it) }
        return try {
            val response = countriesNowApi.getCitiesByCountry(CountryCitiesRequest(country))
            if (response.error || response.data.isNullOrEmpty()) {
                fallbackCities()
            } else {
                val sorted = response.data
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                pakistanCities = sorted
                Result.success(sorted)
            }
        } catch (_: IOException) {
            fallbackCities()
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun suggestionsFor(query: String, limit: Int = 15): List<String> {
        val cities = loadPakistanCities().getOrElse { PakistaniCities.allNames }
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return cities.take(limit)
        }
        return cities
            .asSequence()
            .filter { it.contains(trimmed, ignoreCase = true) }
            .sortedWith(
                compareBy<String> { !it.startsWith(trimmed, ignoreCase = true) }
                    .thenBy { it }
            )
            .take(limit)
            .toList()
    }

    suspend fun resolveCityName(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val cities = loadPakistanCities().getOrNull() ?: return PakistaniCities.canonicalize(trimmed)
        return cities.firstOrNull { it.equals(trimmed, ignoreCase = true) }
    }

    suspend fun coordinatesFor(city: String): GeoCoordinates? {
        val canonical = resolveCityName(city) ?: city.trim()
        if (canonical.isEmpty()) return null
        coordinatesCache[canonical.lowercase()]?.let { return it }
        PakistaniCities.coordinatesFor(canonical)?.let { cached ->
            coordinatesCache[canonical.lowercase()] = cached
            return cached
        }
        return try {
            val info = aladhanApi.getCityInfo(city = canonical, country = country)
            val coords = GeoCoordinates(
                latitude = info.data.latitude,
                longitude = info.data.longitude
            )
            coordinatesCache[canonical.lowercase()] = coords
            coords
        } catch (_: Exception) {
            null
        }
    }

    fun cachedCityCount(): Int = pakistanCities?.size ?: 0

    private fun fallbackCities(): Result<List<String>> {
        val fallback = PakistaniCities.allNames
        pakistanCities = fallback
        return Result.success(fallback)
    }
}
