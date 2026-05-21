package com.example.waqt.network

import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {
    @GET("timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = AladhanConstants.DEFAULT_METHOD
    ): PrayerResponse

    @GET("timingsByCity")
    suspend fun getPrayerTimesByCity(
        @Query("city") city: String,
        @Query("country") country: String = AladhanConstants.DEFAULT_COUNTRY,
        @Query("method") method: Int = AladhanConstants.DEFAULT_METHOD
    ): PrayerResponse
}
