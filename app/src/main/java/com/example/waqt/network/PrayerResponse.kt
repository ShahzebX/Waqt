package com.example.waqt.network

import com.google.gson.annotations.SerializedName

data class PrayerResponse(
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings,
    val date: DateInfo
)

data class Timings(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String
)

data class DateInfo(
    val readable: String,
    val hijri: HijriDate
)

data class HijriDate(
    val date: String,
    val month: HijriMonth
)

data class HijriMonth(
    val en: String,
    val ar: String
)
