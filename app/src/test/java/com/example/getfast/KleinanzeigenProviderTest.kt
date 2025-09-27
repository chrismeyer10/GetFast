package com.example.getfast

import com.example.getfast.model.City
import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.repository.HtmlFetcher
import com.example.getfast.repository.KleinanzeigenProvider
import com.example.getfast.repository.ProviderListingParser
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test

private class StaticHtmlFetcher : HtmlFetcher {
    override suspend fun fetch(url: String): Document = Jsoup.parse("<html></html>")
}

private class StubParser(private val listings: List<Listing>) : ProviderListingParser {
    override fun parseListingsFromDocument(document: Document): List<Listing> = listings
}

class KleinanzeigenProviderTest {
    @Test
    fun fetchListingsForFilter_populatesMissingCityFromFilter() = runBlocking {
        val filter = SearchFilter(city = City("Berlin"))
        val listingWithoutCity = Listing(
            id = "1",
            title = "Test",
            url = "https://example.com",
            date = "",
            district = "Mitte",
            city = "",
            price = "100 €",
            summary = "",
        )
        val provider = KleinanzeigenProvider(
            fetcher = StaticHtmlFetcher(),
            parser = StubParser(listOf(listingWithoutCity)),
        )

        val listings = provider.fetchListingsForFilter(filter)

        assertEquals(listOf("Berlin"), listings.map { it.city })
    }
}
