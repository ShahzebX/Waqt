package com.example.waqt.repository

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.model.Prayer
import com.example.waqt.network.AladhanApi
import com.example.waqt.network.DateInfo
import com.example.waqt.network.PrayerResponse
import com.example.waqt.network.Timings
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class PrayerRepository(
    private val api: AladhanApi,
    private val prayerDao: PrayerDao,
    private val currentDateProvider: () -> LocalDate = LocalDate::now
) {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = DEFAULT_METHOD
    ): Result<List<Prayer>> {
        return fetchPrayerTimes {
            api.getPrayerTimes(latitude = latitude, longitude = longitude, method = method)
        }
    }

    suspend fun getPrayerTimesByCity(
        city: String,
        country: String = DEFAULT_COUNTRY,
        method: Int = DEFAULT_METHOD
    ): Result<List<Prayer>> {
        return fetchPrayerTimes {
            api.getPrayerTimesByCity(city = city, country = country, method = method)
        }
    }

    suspend fun getCachedPrayers(date: String): List<PrayerEntity> {
        return prayerDao.getPrayersByDate(date)
    }

    private suspend fun fetchPrayerTimes(
        apiRequest: suspend () -> PrayerResponse
    ): Result<List<Prayer>> {
        return try {
            val response = apiRequest()
            val date = response.data.date.toIsoDate()
            val prayers = response.data.timings.toEntityList(date)
            prayerDao.insertPrayers(prayers)
            Result.success(prayers.map(PrayerEntity::toDomain))
        } catch (exception: IOException) {
            loadCachedOrFail(exception)
        } catch (exception: HttpException) {
            loadCachedOrFail(exception)
        } catch (exception: JsonParseException) {
            loadCachedOrFail(exception)
        } catch (exception: DateTimeParseException) {
            loadCachedOrFail(exception)
        }
    }

    private suspend fun loadCachedOrFail(exception: Exception): Result<List<Prayer>> {
        val cachedPrayers = prayerDao.getPrayersByDate(currentDateProvider().toString())
        return if (cachedPrayers.isNotEmpty()) {
            Result.success(cachedPrayers.map(PrayerEntity::toDomain))
        } else {
            Result.failure(exception)
        }
    }

    companion object {
        const val DEFAULT_METHOD = 18
        const val DEFAULT_COUNTRY = "PK"
    }
}

private val aladhanDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
private val prayerTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

private fun DateInfo.toIsoDate(): String {
    return LocalDate.parse(gregorian.date, aladhanDateFormatter).toString()
}

private fun Timings.toEntityList(date: String): List<PrayerEntity> {
    val zoneId = ZoneId.systemDefault()
    val localDate = LocalDate.parse(date)
    val values = listOf(fajr, dhuhr, asr, maghrib, isha)

    return prayerNames.zip(values).map { (name, rawTime) ->
        val normalizedTime = rawTime.substringBefore(" ").trim()
        val localTime = LocalTime.parse(normalizedTime, prayerTimeFormatter)
        val epochMs = LocalDateTime.of(localDate, localTime)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        PrayerEntity(
            id = "${date}_$name",
            name = name,
            time = normalizedTime,
            date = date,
            epochMs = epochMs
        )
    }
}

private fun PrayerEntity.toDomain(): Prayer {
    return Prayer(
        name = name,
        time = time,
        date = date
    )
}
