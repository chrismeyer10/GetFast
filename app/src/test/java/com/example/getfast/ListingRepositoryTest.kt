package com.example.getfast

import com.example.getfast.model.CityCatalog
import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import com.example.getfast.repository.HtmlFetcher
import com.example.getfast.repository.ImmoscoutListingParser
import com.example.getfast.repository.ImmoscoutProvider
import com.example.getfast.repository.ImmonetProvider
import com.example.getfast.repository.ImmoweltProvider
import com.example.getfast.repository.KleinanzeigenProvider
import com.example.getfast.repository.ImmonetListingParser
import com.example.getfast.repository.ImmoweltListingParser
import com.example.getfast.repository.ListingRepository
import com.example.getfast.repository.WohnungsboerseProvider
import com.example.getfast.repository.KleinanzeigenListingParser
import com.example.getfast.repository.WohnungsboerseListingParser
import com.example.getfast.repository.ListingProvider
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeFetcher(private val html: String) : HtmlFetcher {
    override suspend fun fetch(url: String): Document = Jsoup.parse(html)
}

private class FakeListingProvider(
    override val source: ListingSource,
    private val listings: List<Listing>,
) : ListingProvider {
    override suspend fun fetchListingsForFilter(filter: SearchFilter): List<Listing> = listings
}

class ListingRepositoryTest {
    private fun loadHtml(name: String): String =
        javaClass.getResource("/html/$name")!!.readText()

    @Test
    fun fetchLatestListings_filtersByMaxPrice() = runBlocking {
        val provider = KleinanzeigenProvider(
            fetcher = FakeFetcher(loadHtml("kleinanzeigen.html")),
            parser = KleinanzeigenListingParser(),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.KLEINANZEIGEN to provider))
        val listings = repo.fetchLatestListingsMatchingFilter(SearchFilter(maxPrice = 60))
        assertEquals(1, listings.size)
        assertEquals("2", listings[0].id)
    }

    @Test
    fun fetchLatestListings_returnsImmoscoutListings() = runBlocking {
        val provider = ImmoscoutProvider(
            fetcher = FakeFetcher(loadHtml("immoscout.html")),
            parser = ImmoscoutListingParser(),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.IMMOSCOUT to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOSCOUT))
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Immo 1", first.title)
        assertEquals("200 €", first.price)
    }

    @Test
    fun fetchLatestListings_returnsImmonetListings() = runBlocking {
        val provider = ImmonetProvider(
            fetcher = FakeFetcher(loadHtml("immonet.html")),
            parser = ImmonetListingParser(),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.IMMONET to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMONET))
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Net 1", first.title)
        assertEquals("300 €", first.price)
    }

    @Test
    fun fetchLatestListings_returnsImmoweltListings() = runBlocking {
        val provider = ImmoweltProvider(
            fetcher = FakeFetcher(loadHtml("immowelt.html")),
            parser = ImmoweltListingParser(),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.IMMOWELT to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOWELT))
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Welt 1", first.title)
        assertEquals("400 €", first.price)
    }

    @Test
    fun fetchLatestListings_returnsWohnungsboerseListings() = runBlocking {
        val provider = WohnungsboerseProvider(
            fetcher = FakeFetcher(loadHtml("wohnungsboerse.html")),
            parser = WohnungsboerseListingParser(),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.WOHNUNGSBOERSE to provider))
        val filter = SearchFilter(sources = setOf(ListingSource.WOHNUNGSBOERSE))
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        assertEquals(1, listings.size)
        val first = listings[0]
        assertEquals("Boerse 1", first.title)
        assertEquals("500 €", first.price)
    }

    @Test
    fun fetchLatestListings_filtersBySelectedCity() = runBlocking {
        val provider = FakeListingProvider(
            source = ListingSource.KLEINANZEIGEN,
            listings = listOf(
                Listing(
                    id = "1",
                    title = "Hamburg Mitte",
                    url = "https://example.com/1",
                    date = "",
                    district = "Mitte",
                    city = "Hamburg",
                    price = "100 €",
                    summary = "",
                ),
                Listing(
                    id = "2",
                    title = "Berlin Mitte",
                    url = "https://example.com/2",
                    date = "",
                    district = "Mitte",
                    city = "Berlin",
                    price = "100 €",
                    summary = "",
                ),
                Listing(
                    id = "3",
                    title = "Hamburg Barmbek",
                    url = "https://example.com/3",
                    date = "",
                    district = "Barmbek",
                    city = "Hamburg - Barmbek",
                    price = "100 €",
                    summary = "",
                ),
            ),
        )
        val hamburg = CityCatalog.findByName("Hamburg")!!
        val filter = SearchFilter(
            city = hamburg,
            sources = setOf(ListingSource.KLEINANZEIGEN),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.KLEINANZEIGEN to provider))

        val listings = repo.fetchLatestListingsMatchingFilter(filter)

        val ids = listings.map { it.id }.sorted()
        assertEquals(listOf("1", "3"), ids)
    }

    @Test
    fun fetchLatestListings_filtersByMaxAge() = runBlocking {
        val provider = KleinanzeigenProvider(
            fetcher = FakeFetcher(loadHtml("kleinanzeigen_old.html")),
            parser = KleinanzeigenListingParser(),
        )
        val repo = ListingRepository(providers = mapOf(ListingSource.KLEINANZEIGEN to provider))
        val filter = SearchFilter(maxAgeDays = 3)
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        assertEquals(1, listings.size)
        assertEquals("1", listings[0].id)
    }
}
