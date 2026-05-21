package com.example.waqt.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PakistaniCitiesTest {
    @Test
    fun `suggestions filter by prefix and contains`() {
        val suggestions = PakistaniCities.suggestionsFor("lah")
        assertTrue(suggestions.contains("Lahore"))
    }

    @Test
    fun `canonicalize matches case insensitive`() {
        assertEquals("Karachi", PakistaniCities.canonicalize("karachi"))
        assertEquals("Islamabad", PakistaniCities.canonicalize("ISLAMABAD"))
    }

    @Test
    fun `unknown city returns null canonical`() {
        assertNull(PakistaniCities.canonicalize("Paris"))
    }

    @Test
    fun `coordinates available for known city`() {
        assertNotNull(PakistaniCities.coordinatesFor("Multan"))
    }
}
