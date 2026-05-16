package com.example.waqt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waqt.database.WaqtDatabase
import com.example.waqt.location.FusedLocationProvider
import com.example.waqt.network.RetrofitInstance
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.settings.DataStoreUserSettingsDataSource
import com.example.waqt.worker.WorkManagerPrayerNotificationScheduler

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
            val settingsDataSource = DataStoreUserSettingsDataSource(appContext)
            val notificationScheduler = WorkManagerPrayerNotificationScheduler(appContext)
            @Suppress("UNCHECKED_CAST")
            return PrayerViewModel(
                repository = repository,
                locationProvider = locationProvider,
                settingsDataSource = settingsDataSource,
                notificationScheduler = notificationScheduler
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
