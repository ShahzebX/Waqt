package com.example.waqt.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waqt.database.WaqtDatabase
import com.example.waqt.model.Prayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val date = LocalDate.now().toString()
                val prayers = WaqtDatabase.getInstance(context)
                    .prayerDao()
                    .getPrayersByDate(date)
                    .map { entity ->
                        Prayer(
                            name = entity.name,
                            time = entity.time,
                            date = entity.date
                        )
                    }

                WorkManagerPrayerNotificationScheduler(context).schedule(prayers)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
