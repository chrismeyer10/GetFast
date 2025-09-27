package com.example.getfast

import com.example.getfast.repository.ImmonetListingParser
import com.example.getfast.repository.ImmoscoutListingParser
import com.example.getfast.repository.ImmoweltListingParser
import com.example.getfast.repository.KleinanzeigenListingParser
import com.example.getfast.repository.WohnungsboerseListingParser
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class ListingParserTest {
    private fun loadHtml(name: String): String =
        javaClass.getResource("/html/$name")!!.readText()

    @Test
    fun parseKleinanzeigen_returnsAllListings() {
        val parser = KleinanzeigenListingParser()
        val doc = Jsoup.parse(loadHtml("kleinanzeigen.html"))
        val listings = parser.parseListingsFromDocument(doc)
        assertEquals(2, listings.size)
        val first = listings[0]
        assertEquals("Titel 1", first.title)
        assertEquals("100 €", first.price)
        assertEquals("Bezirk", first.district)
        assertEquals("Stadt", first.city)
    }

    @Test
    fun parseImmoscout_returnsListings() {
        val parser = ImmoscoutListingParser()
        val doc = Jsoup.parse(loadHtml("immoscout.html"))
        val listings = parser.parseListingsFromDocument(doc)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Immo 1", first.title)
        assertEquals("200 €", first.price)
        assertEquals("Bezirk", first.district)
    }

    @Test
    fun parseImmonet_returnsListings() {
        val parser = ImmonetListingParser()
        val doc = Jsoup.parse(loadHtml("immonet.html"))
        val listings = parser.parseListingsFromDocument(doc)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Net 1", first.title)
        assertEquals("300 €", first.price)
        assertEquals("Bezirk", first.district)
    }

    @Test
    fun parseImmowelt_returnsListings() {
        val parser = ImmoweltListingParser()
        val doc = Jsoup.parse(loadHtml("immowelt.html"))
        val listings = parser.parseListingsFromDocument(doc)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Welt 1", first.title)
        assertEquals("400 €", first.price)
        assertEquals("Bezirk", first.district)
    }

    @Test
    fun parseWohnungsboerse_returnsListings() {
        val parser = WohnungsboerseListingParser()
        val doc = Jsoup.parse(loadHtml("wohnungsboerse.html"))
        val listings = parser.parseListingsFromDocument(doc)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Boerse 1", first.title)
        assertEquals("500 €", first.price)
        assertEquals("Bezirk", first.district)
    }
}
