package com.example.waqt.qibla

import com.example.waqt.location.GeoCoordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaCalculatorTest {
    @Test
    fun `bearing from Karachi points roughly east-southeast toward Makkah`() {
        val bearing = QiblaCalculator.bearingFrom(
            GeoCoordinates(latitude = 24.8607, longitude = 67.0011)
        )

        assertTrue(bearing in 260f..290f)
    }

    @Test
    fun `qibla offset is zero when device faces qibla`() {
        val qibla = 280f
        val offset = QiblaCalculator.qiblaOffsetFromScreenTop(qiblaBearing = qibla, deviceAzimuth = qibla)

        assertEquals(0f, offset, 0.01f)
        assertTrue(QiblaCalculator.isFacingQibla(qibla, qibla))
    }

    @Test
    fun `normalize degrees wraps negative values`() {
        assertEquals(90f, QiblaCalculator.normalizeDegrees(-270f), 0.01f)
    }
}
