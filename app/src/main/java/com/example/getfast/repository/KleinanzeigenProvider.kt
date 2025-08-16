package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter

/**
 * Provider für Anzeigen von eBay Kleinanzeigen.
 */
class KleinanzeigenProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.KLEINANZEIGEN

    override suspend fun fetchListings(filter: SearchFilter): List<Listing> {
        val path = filter.city.pathFor(source)
        val url = "https://www.kleinanzeigen.de/s-wohnung-mieten/$path"
        return runCatching {
            val doc = fetcher.fetch(url)
            parser.parse(doc)
        }.getOrElse { emptyList() }
    }
}

