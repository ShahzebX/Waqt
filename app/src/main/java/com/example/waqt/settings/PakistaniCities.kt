package com.example.waqt.settings

import com.example.waqt.location.GeoCoordinates

/**
 * Major Pakistani cities supported for Aladhan `timingsByCity` (country PK) and Qibla fallback.
 */
object PakistaniCities {
    private data class City(val name: String, val latitude: Double, val longitude: Double)

    private val cities = listOf(
        City("Abbottabad", 34.1688, 73.2215),
        City("Bahawalpur", 29.3956, 71.6836),
        City("Chiniot", 31.7204, 72.9789),
        City("Dera Ghazi Khan", 30.0458, 70.6403),
        City("Faisalabad", 31.4504, 73.1350),
        City("Gujranwala", 32.1877, 74.1945),
        City("Gujrat", 32.5742, 74.0754),
        City("Gwadar", 25.1216, 62.3254),
        City("Hafizabad", 32.0709, 73.6880),
        City("Hyderabad", 25.3960, 68.3578),
        City("Islamabad", 33.6844, 73.0479),
        City("Jacobabad", 28.2819, 68.4386),
        City("Jhelum", 32.9405, 73.7276),
        City("Kamoke", 31.9753, 74.2231),
        City("Karachi", 24.8607, 67.0011),
        City("Kasur", 31.1156, 74.4466),
        City("Khanewal", 30.3017, 71.9321),
        City("Kohat", 33.5819, 71.4493),
        City("Lahore", 31.5204, 74.3587),
        City("Larkana", 27.5600, 68.2141),
        City("Mardan", 34.1979, 72.0497),
        City("Mirpur", 33.1478, 73.7519),
        City("Multan", 30.1575, 71.5249),
        City("Muzaffarabad", 34.3700, 73.4708),
        City("Nawabshah", 26.2442, 68.4100),
        City("Okara", 30.8081, 73.4458),
        City("Peshawar", 34.0151, 71.5249),
        City("Quetta", 30.1798, 66.9750),
        City("Rawalpindi", 33.5651, 73.0169),
        City("Sahiwal", 30.6667, 73.1000),
        City("Sargodha", 32.0836, 72.6711),
        City("Sheikhupura", 31.7167, 73.9850),
        City("Shikarpur", 27.9556, 68.6382),
        City("Sialkot", 32.4945, 74.5229),
        City("Sukkur", 27.7032, 68.8589),
        City("Turbat", 26.0031, 63.0562)
    )

    val allNames: List<String> = cities.map { it.name }.sorted()

    fun suggestionsFor(query: String, limit: Int = 8): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return allNames.take(limit)
        }
        return allNames
            .asSequence()
            .filter { it.contains(trimmed, ignoreCase = true) }
            .sortedWith(
                compareBy<String> { !it.startsWith(trimmed, ignoreCase = true) }
                    .thenBy { it }
            )
            .take(limit)
            .toList()
    }

    fun canonicalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        return allNames.firstOrNull { it.equals(trimmed, ignoreCase = true) }
    }

    fun coordinatesFor(city: String): GeoCoordinates? {
        val canonical = canonicalize(city) ?: return null
        val match = cities.firstOrNull { it.name == canonical } ?: return null
        return GeoCoordinates(match.latitude, match.longitude)
    }
}
