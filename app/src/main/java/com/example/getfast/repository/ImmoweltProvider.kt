package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für Immowelt. */
class ImmoweltProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.IMMOWELT

    override suspend fun fetchListings(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.pathFor(source), StandardCharsets.UTF_8.toString())
        val price = filter.maxPrice?.let { "&maxprice=$it" } ?: ""
        val url = "https://www.immowelt.de/suche/wohnung-mieten?city=$city$price"
        return runCatching {
            val doc = fetcher.fetch(url)
            parser.parseImmowelt(doc)
        }.getOrElse { emptyList() }
    }
}

