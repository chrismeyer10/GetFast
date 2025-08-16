package com.example.getfast

import com.example.getfast.repository.ListingParser
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class ListingParserTest {
    private val html = """
        <html><body>
        <article class='aditem' data-adid='1'>
          <a href='/ad1' class='ellipsis'>Titel 1</a>
          <div class='aditem-main--top--right'>Heute, 10:00</div>
          <div class='aditem-main--top--left'>Bezirk, Stadt</div>
          <div class='aditem-main--middle--price-shipping'>100 €</div>
          <div class='aditem-main--middle--description'>Beschreibung eins.</div>
        </article>
        <article class='aditem' data-adid='2'>
          <a href='/ad2' class='ellipsis'>Titel 2</a>
          <div class='aditem-main--top--right'>Heute, 11:00</div>
          <div class='aditem-main--top--left'>Bezirk, Stadt</div>
          <div class='aditem-main--middle--price-shipping'>50 €</div>
          <div class='aditem-main--middle--description'>Beschreibung zwei.</div>
        </article>
        </body></html>
    """.trimIndent()

    private val immoscoutHtml = """
        <html><body>
        <article class='result-list-entry' data-obid='10'>
          <a href='/expose/10' class='result-list-entry__brand-title-container'>Immo 1</a>
          <div class='result-list-entry__primary-criterion'>200 €</div>
          <div class='result-list-entry__address'>Bezirk, Stadt</div>
          <div class='result-list-entry__description'>Beschreibung.</div>
          <img src='img.jpg'/>
        </article>
        </body></html>
    """.trimIndent()

    @Test
    fun parse_returnsAllListings() {
        val parser = ListingParser()
        val doc = Jsoup.parse(html)
        val listings = parser.parse(doc)
        assertEquals(2, listings.size)
        assertEquals("Titel 1", listings[0].title)
    }

    @Test
    fun parseImmoscout_returnsListings() {
        val parser = ListingParser()
        val doc = Jsoup.parse(immoscoutHtml)
        val listings = parser.parseImmoscout(doc)
        assertEquals(1, listings.size)
        assertEquals("Immo 1", listings[0].title)
    }
}
