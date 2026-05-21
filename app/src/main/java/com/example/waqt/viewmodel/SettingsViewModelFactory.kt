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

class SettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val appContext = context.applicationContext
            val repository = PrayerRepository(
                api = RetrofitInstance.api,
                prayerDao = WaqtDatabase.getInstance(appContext).prayerDao()
            )
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                repository = repository,
                locationProvider = FusedLocationProvider(appContext),
                settingsDataSource = DataStoreUserSettingsDataSource(appContext),
                notificationScheduler = WorkManagerPrayerNotificationScheduler(appContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
