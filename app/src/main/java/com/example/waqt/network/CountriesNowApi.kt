package com.example.waqt.network

import retrofit2.http.Body
import retrofit2.http.POST

interface CountriesNowApi {
    @POST("countries/cities")
    suspend fun getCitiesByCountry(@Body request: CountryCitiesRequest): CountryCitiesResponse
}

data class CountryCitiesRequest(
    val country: String
)

data class CountryCitiesResponse(
    val error: Boolean,
    val msg: String,
    val data: List<String>?
)
