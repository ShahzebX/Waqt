package com.example.waqt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waqt.database.WaqtDatabase
import com.example.waqt.location.FusedLocationProvider
import com.example.waqt.network.RetrofitInstance
import com.example.waqt.repository.PrayerRepository

class PrayerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            val appContext = context.applicationContext
            val repository = PrayerRepository(
                api = RetrofitInstance.api,
                prayerDao = WaqtDatabase.getInstance(appContext).prayerDao()
            )
            val locationProvider = FusedLocationProvider(appContext)
            @Suppress("UNCHECKED_CAST")
            return PrayerViewModel(repository, locationProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
