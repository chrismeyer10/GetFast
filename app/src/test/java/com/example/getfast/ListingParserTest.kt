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

    private val immonetHtml = """
        <html><body>
        <article class='immonet-entry' data-id='20'>
          <a href='/expose/20' class='immonet-link'>Net 1</a>
          <div class='immonet-price'>300 €</div>
          <div class='immonet-address'>Bezirk, Stadt</div>
          <div class='immonet-desc'>Beschreibung.</div>
          <img src='img.jpg'/>
        </article>
        </body></html>
    """.trimIndent()

    private val immoweltHtml = """
        <html><body>
        <article class='immowelt-entry' data-id='30'>
          <a href='/expose/30' class='immowelt-link'>Welt 1</a>
          <div class='immowelt-price'>400 €</div>
          <div class='immowelt-address'>Bezirk, Stadt</div>
          <div class='immowelt-desc'>Beschreibung.</div>
          <img src='img.jpg'/>
        </article>
        </body></html>
    """.trimIndent()

    private val wohnungsboerseHtml = """
        <html><body>
        <article class='wohnungsboerse-entry' data-id='40'>
          <a href='/expose/40' class='wohnungsboerse-link'>Boerse 1</a>
          <div class='wohnungsboerse-price'>500 €</div>
          <div class='wohnungsboerse-address'>Bezirk, Stadt</div>
          <div class='wohnungsboerse-desc'>Beschreibung.</div>
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

    @Test
    fun parseImmonet_returnsListings() {
        val parser = ListingParser()
        val doc = Jsoup.parse(immonetHtml)
        val listings = parser.parseImmonet(doc)
        assertEquals(1, listings.size)
        assertEquals("Net 1", listings[0].title)
    }

    @Test
    fun parseImmowelt_returnsListings() {
        val parser = ListingParser()
        val doc = Jsoup.parse(immoweltHtml)
        val listings = parser.parseImmowelt(doc)
        assertEquals(1, listings.size)
        assertEquals("Welt 1", listings[0].title)
    }

    @Test
    fun parseWohnungsboerse_returnsListings() {
        val parser = ListingParser()
        val doc = Jsoup.parse(wohnungsboerseHtml)
        val listings = parser.parseWohnungsboerse(doc)
        assertEquals(1, listings.size)
        assertEquals("Boerse 1", listings[0].title)
    }
}
