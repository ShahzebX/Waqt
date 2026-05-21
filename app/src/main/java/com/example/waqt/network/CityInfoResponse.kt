package com.example.waqt.network

data class CityInfoResponse(
    val code: Int,
    val status: String,
    val data: CityInfoData
)

data class CityInfoData(
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)
