package com.example.waqt.network

import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {
    @GET("timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 18
    ): PrayerResponse

    @GET("timingsByCity")
    suspend fun getPrayerTimesByCity(
        @Query("city") city: String,
        @Query("country") country: String = "PK",
        @Query("method") method: Int = 18
    ): PrayerResponse
}
