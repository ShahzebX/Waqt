package com.example.waqt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waqt.database.WaqtDatabase
import com.example.waqt.network.RetrofitInstance
import com.example.waqt.repository.PrayerRepository

class PlannerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            val appContext = context.applicationContext
            val repository = PrayerRepository(
                api = RetrofitInstance.api,
                prayerDao = WaqtDatabase.getInstance(appContext).prayerDao()
            )
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
