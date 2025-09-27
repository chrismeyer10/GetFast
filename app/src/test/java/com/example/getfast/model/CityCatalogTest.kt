package com.example.getfast.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CityCatalogTest {

    @Test
    fun germany_containsExpectedCities() {
        val cityNames = CityCatalog.germany.map { it.displayName }
        assertEquals(listOf("Berlin", "Hamburg", "München", "Köln"), cityNames)
    }

    @Test
    fun findByName_matchesDiacriticInsensitiveInput() {
        val munich = CityCatalog.findByName("Munchen")
        val cologne = CityCatalog.findByName("koln")

        assertNotNull(munich)
        assertEquals("München", munich!!.displayName)

        assertNotNull(cologne)
        assertEquals("Köln", cologne!!.displayName)
    }

    @Test
    fun matchesQuery_handlesNormalizedInput() {
        val munich = City("München")
        assertTrue(munich.matchesQuery("Mun"))
        assertTrue(munich.matchesQuery("munchen"))
    }
}

