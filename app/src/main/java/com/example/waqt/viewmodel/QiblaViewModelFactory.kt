package com.example.waqt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waqt.location.FusedLocationProvider
import com.example.waqt.qibla.AndroidCompassSensorProvider
import com.example.waqt.network.RetrofitInstance
import com.example.waqt.settings.DataStoreUserSettingsDataSource

class QiblaViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QiblaViewModel::class.java)) {
            val appContext = context.applicationContext
            @Suppress("UNCHECKED_CAST")
            return QiblaViewModel(
                locationProvider = FusedLocationProvider(appContext),
                compassSensorProvider = AndroidCompassSensorProvider(appContext),
                settingsDataSource = DataStoreUserSettingsDataSource(appContext),
                cityRepository = RetrofitInstance.cityRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
