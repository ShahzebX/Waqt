package com.example.waqt.network

import com.example.waqt.repository.CityRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val ALADHAN_BASE_URL = "https://api.aladhan.com/v1/"
    private const val COUNTRIES_NOW_BASE_URL = "https://countriesnow.space/api/v0.1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val gsonConverter = GsonConverterFactory.create()

    val api: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl(ALADHAN_BASE_URL)
            .client(client)
            .addConverterFactory(gsonConverter)
            .build()
            .create(AladhanApi::class.java)
    }

    val countriesNowApi: CountriesNowApi by lazy {
        Retrofit.Builder()
            .baseUrl(COUNTRIES_NOW_BASE_URL)
            .client(client)
            .addConverterFactory(gsonConverter)
            .build()
            .create(CountriesNowApi::class.java)
    }

    val cityRepository: CityRepository by lazy {
        CityRepository(
            countriesNowApi = countriesNowApi,
            aladhanApi = api
        )
    }
}
