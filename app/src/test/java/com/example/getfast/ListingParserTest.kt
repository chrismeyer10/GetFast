package com.example.getfast

import com.example.getfast.repository.KleinanzeigenListingParser
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun parseKleinanzeigen_marksSearchListings() {
        val parser = KleinanzeigenListingParser()
        val html = """
            <html><body>
                <article class=\"aditem\" data-adid=\"1\">
                    <a href=\"/ad1\" class=\"ellipsis\">Suche Wohnung</a>
                    <div class=\"aditem-main--top--right\">Heute</div>
                    <div class=\"aditem-main--top--left\">Bezirk, Stadt</div>
                    <div class=\"aditem-main--middle--price-shipping\">VB</div>
                    <div class=\"aditem-main--middle--description\">Wir suchen dringend eine Wohnung.</div>
                </article>
            </body></html>
        """.trimIndent()
        val doc = Jsoup.parse(html)
        val listings = parser.parseListingsFromDocument(doc)
        assertEquals(1, listings.size)
        assertTrue(listings.first().isSearch)
    }
}
