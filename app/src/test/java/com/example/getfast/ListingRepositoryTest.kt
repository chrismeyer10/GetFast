package com.example.getfast

import com.example.getfast.model.SearchFilter
import com.example.getfast.model.ListingSource
import com.example.getfast.repository.HtmlFetcher
import com.example.getfast.repository.ImmoscoutProvider
import com.example.getfast.repository.ImmonetProvider
import com.example.getfast.repository.ImmoweltProvider
import com.example.getfast.repository.KleinanzeigenProvider
import com.example.getfast.repository.ListingParser
import com.example.getfast.repository.ListingRepository
import com.example.getfast.repository.WohnungsboerseProvider
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeFetcher(private val html: String) : HtmlFetcher {
    override suspend fun fetch(url: String): Document = Jsoup.parse(html)
}

class ListingRepositoryTest {
    private fun loadHtml(name: String): String =
        javaClass.getResource("/html/$name")!!.readText()

    @Test
    fun fetchLatestListings_filtersByMaxPrice() = runBlocking {
        val provider = KleinanzeigenProvider(fetcher = FakeFetcher(loadHtml("kleinanzeigen.html")), parser = ListingParser())
        val repo = ListingRepository(providers = mapOf(ListingSource.KLEINANZEIGEN to provider))
        val listings = repo.fetchLatestListings(SearchFilter(maxPrice = 60))
        assertEquals(1, listings.size)
        assertEquals("2", listings[0].id)
    }

    @Test
    fun fetchLatestListings_returnsImmoscoutListings() = runBlocking {
        val provider = ImmoscoutProvider(fetcher = FakeFetcher(loadHtml("immoscout.html")), parser = ListingParser())
        val repo = ListingRepository(providers = mapOf(ListingSource.IMMOSCOUT to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOSCOUT))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Immo 1", first.title)
        assertEquals("200 €", first.price)
    }

    @Test
    fun fetchLatestListings_returnsImmonetListings() = runBlocking {
        val provider = ImmonetProvider(fetcher = FakeFetcher(loadHtml("immonet.html")), parser = ListingParser())
        val repo = ListingRepository(providers = mapOf(ListingSource.IMMONET to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMONET))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Net 1", first.title)
        assertEquals("300 €", first.price)
    }

    @Test
    fun fetchLatestListings_returnsImmoweltListings() = runBlocking {
        val provider = ImmoweltProvider(fetcher = FakeFetcher(loadHtml("immowelt.html")), parser = ListingParser())
        val repo = ListingRepository(providers = mapOf(ListingSource.IMMOWELT to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOWELT))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Welt 1", first.title)
        assertEquals("400 €", first.price)
    }

    @Test
    fun fetchLatestListings_returnsWohnungsboerseListings() = runBlocking {
        val provider = WohnungsboerseProvider(fetcher = FakeFetcher(loadHtml("wohnungsboerse.html")), parser = ListingParser())
        val repo = ListingRepository(providers = mapOf(ListingSource.WOHNUNGSBOERSE to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.WOHNUNGSBOERSE))
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Boerse 1", first.title)
        assertEquals("500 €", first.price)
    }

    @Test
    fun fetchLatestListings_filtersByMaxAge() = runBlocking {
        val provider = KleinanzeigenProvider(fetcher = FakeFetcher(loadHtml("kleinanzeigen_old.html")), parser = ListingParser())
        val repo = ListingRepository(providers = mapOf(ListingSource.KLEINANZEIGEN to provider))
        val filter = SearchFilter(maxAgeDays = 3)
        val listings = repo.fetchLatestListings(filter)
        assertEquals(1, listings.size)
        assertEquals("1", listings[0].id)
    }
}
