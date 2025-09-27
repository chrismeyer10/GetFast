package com.example.getfast

import com.example.getfast.model.City
import com.example.getfast.model.CityCatalog
import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.repository.HtmlFetcher
import com.example.getfast.repository.KleinanzeigenListingParser
import com.example.getfast.repository.KleinanzeigenProvider
import com.example.getfast.repository.ListingRepository
import com.example.getfast.ui.createInitialSettingsScreenState
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
    fun fetchLatestListings_returnsKleinanzeigenListings() = runBlocking {
        val provider = KleinanzeigenProvider(
            fetcher = FakeFetcher(loadHtml("kleinanzeigen.html")),
            parser = KleinanzeigenListingParser(),
        )
        val repo = ListingRepository { filter -> provider.fetchListingsForFilter(filter) }
        val listings = repo.fetchLatestListingsMatchingFilter(SearchFilter())
        assertEquals(2, listings.size)
        val first = listings.first()
        assertEquals("1", first.id)
        assertEquals("Titel 1", first.title)
    }

    @Test
    fun fetchLatestListings_filtersBySelectedCity() = runBlocking {
        val hamburg = City("Hamburg")
        val berlin = City("Berlin")
        val repo = ListingRepository {
            listOf(
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
            )
        }
        val filter = SearchFilter(city = hamburg)
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        val ids = listings.map { it.id }.sorted()
        assertEquals(listOf("1", "3"), ids)

        val berlinListings = repo.fetchLatestListingsMatchingFilter(SearchFilter(city = berlin))
        assertEquals(listOf("2"), berlinListings.map { it.id })
    }

    @Test
    fun createInitialState_prefersCatalogCity() {
        val filter = SearchFilter(city = City.custom(" Hamburg "))
        val state = createInitialSettingsScreenState(filter)
        assertEquals(CityCatalog.findByName("Hamburg"), state.selectedCity)
    }
}
