package com.example.waqt.network

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class PrayerResponseParsingTest {
    @Test
    fun `parses aladhan timings payload`() {
        val json = """
            {
              "data": {
                "timings": {
                  "Fajr": "04:22 (PKT)",
                  "Sunrise": "05:47 (PKT)",
                  "Dhuhr": "12:15 (PKT)",
                  "Asr": "15:42 (PKT)",
                  "Maghrib": "18:43 (PKT)",
                  "Isha": "20:02 (PKT)"
                },
                "date": {
                  "readable": "07 May 2026",
                  "hijri": {
                    "date": "10-11-1447",
                    "month": {
                      "en": "Dhu al-Qadah",
                      "ar": "ذُو ٱلْقَعْدَة"
                    }
                  },
                  "gregorian": {
                    "date": "07-05-2026"
                  }
                }
              }
            }
        """.trimIndent()

        val response = Gson().fromJson(json, PrayerResponse::class.java)

        assertEquals("04:22 (PKT)", response.data.timings.fajr)
        assertEquals("20:02 (PKT)", response.data.timings.isha)
        assertEquals("07-05-2026", response.data.date.gregorian.date)
        assertEquals("Dhu al-Qadah", response.data.date.hijri.month.en)
    }
}
