package com.example.getfast.repository

import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies that listings are returned even when Kleinanzeigen is excluded.
 */
class ListingRepositoryTest {

    private fun providerFor(source: ListingSource, file: String): ListingProvider {
        val fetcher = object : HtmlFetcher {
            override suspend fun fetch(url: String): Document {
                val html = this::class.java.getResource("/html/$file")!!.readText()
                return Jsoup.parse(html)
            }
        }
        return when (source) {
            ListingSource.IMMOSCOUT -> ImmoscoutProvider(fetcher, ImmoscoutListingParser())
            ListingSource.IMMONET -> ImmonetProvider(fetcher, ImmonetListingParser())
            ListingSource.IMMOWELT -> ImmoweltProvider(fetcher, ImmoweltListingParser())
            ListingSource.WOHNUNGSBOERSE -> WohnungsboerseProvider(fetcher, WohnungsboerseListingParser())
            ListingSource.KLEINANZEIGEN -> KleinanzeigenProvider(fetcher, KleinanzeigenListingParser())
        }
    }

    @Test
    fun `fetches listings without kleinanzeigen`() = runBlocking {
        val repo = ListingRepository(
            providers = mapOf(
                ListingSource.IMMOSCOUT to providerFor(ListingSource.IMMOSCOUT, "immoscout.html"),
            )
        )
        val filter = SearchFilter(sources = setOf(ListingSource.IMMOSCOUT))
        val listings = repo.fetchLatestListingsMatchingFilter(filter)
        assertTrue(listings.isNotEmpty(), "Expected listings from ImmoScout without Kleinanzeigen")
    }
}
