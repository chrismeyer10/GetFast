package com.example.getfast

import com.example.getfast.model.SearchFilter
import com.example.getfast.model.ListingSource
import com.example.getfast.repository.ListingRepository
import com.example.getfast.repository.HtmlFetcher
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeFetcher(private val html: String) : HtmlFetcher {
    override suspend fun fetch(url: String): Document = Jsoup.parse(html)
}

class ListingRepositoryTest {
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
        <li class='result-list__listing' data-obid='10'>
          <a href='/expose/10' class='result-list-entry__brand-title-container'>Immo 1</a>
          <div class='result-list-entry__primary-criterion'><dd>200 €</dd></div>
          <div class='result-list-entry__address'>Bezirk, Stadt</div>
          <div class='result-list-entry__description'>Beschreibung.</div>
        </li>
        </body></html>
    """.trimIndent()

    private val immonetHtml = """
        <html><body>
        <article class='search-list-entry' data-id='20'>
          <a href='/angebot/20' class='result-item-title'>Net 1</a>
          <div class='result-item-price'>300 €</div>
          <div class='result-item-address'>Bezirk, Stadt</div>
          <div class='result-item-description'>Beschreibung.</div>
        </article>
        </body></html>
    """.trimIndent()

    private val immoweltHtml = """
        <html><body>
        <div class='EstateItem' data-id='30'>
          <a href='/expose/30' class='EstateItem-title'>Welt 1</a>
          <div class='EstateItem-price'>400 €</div>
          <div class='EstateItem-address'>Bezirk, Stadt</div>
          <div class='EstateItem-description'>Beschreibung.</div>
        </div>
        </body></html>
    """.trimIndent()

    private val wohnungsboerseHtml = """
        <html><body>
        <div class='inserate-result' data-id='40'>
          <a href='/detail/40' class='ad-list-item'>
            <h2>Boerse 1</h2>
            <p class='mietpreis'>500 €</p>
            <p class='adresse'>Bezirk, Stadt</p>
            <p class='beschreibung'>Beschreibung.</p>
          </a>
        </div>
        </body></html>
    """.trimIndent()

    private val oldHtml = """
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
          <div class='aditem-main--top--right'>01.01.2000</div>
          <div class='aditem-main--top--left'>Bezirk, Stadt</div>
          <div class='aditem-main--middle--price-shipping'>50 €</div>
          <div class='aditem-main--middle--description'>Beschreibung zwei.</div>
        </article>
        </body></html>
    """.trimIndent()

    @Test
    fun fetchLatestListings_filtersByMaxPrice() = runBlocking {
        val repo = ListingRepository(fetcher = FakeFetcher(html))
        val listings = repo.fetchLatestListings(SearchFilter(maxPrice = 60))
        assertEquals(1, listings.size)
        assertEquals("2", listings[0].id)
    }

    @Test
    fun fetchLatestListings_returnsImmoscoutListings() = runBlocking {
        val repo = ListingRepository(fetcher = FakeFetcher(immoscoutHtml))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOSCOUT))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        assertEquals("Immo 1", listings[0].title)
    }

    @Test
    fun fetchLatestListings_returnsImmonetListings() = runBlocking {
        val repo = ListingRepository(fetcher = FakeFetcher(immonetHtml))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMONET))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        assertEquals("Net 1", listings[0].title)
    }

    @Test
    fun fetchLatestListings_returnsImmoweltListings() = runBlocking {
        val repo = ListingRepository(fetcher = FakeFetcher(immoweltHtml))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOWELT))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        assertEquals("Welt 1", listings[0].title)
    }

    @Test
    fun fetchLatestListings_returnsWohnungsboerseListings() = runBlocking {
        val repo = ListingRepository(fetcher = FakeFetcher(wohnungsboerseHtml))
        val filter = SearchFilter(sources = setOf(ListingSource.WOHNUNGSBOERSE))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        assertEquals("Boerse 1", listings[0].title)
    }

    @Test
    fun fetchLatestListings_filtersByMaxAge() = runBlocking {
        val repo = ListingRepository(fetcher = FakeFetcher(oldHtml))
        val filter = SearchFilter(maxAgeDays = 3)
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        assertEquals("1", listings[0].id)
    }
}
