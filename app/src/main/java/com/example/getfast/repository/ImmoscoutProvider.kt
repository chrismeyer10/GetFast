package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für ImmoScout24. */
class ImmoscoutProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.IMMOSCOUT

    override suspend fun fetchListings(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.pathFor(source), StandardCharsets.UTF_8.toString())
        val price = filter.maxPrice?.let { "&price=-$it" } ?: ""
        val url = "https://www.immobilienscout24.de/Suche/radius/wohnung-mieten?centerofsearchaddress=$city$price"
        return runCatching {
            val doc = fetcher.fetch(url)
            parser.parseImmoscout(doc)
        }.getOrElse { emptyList() }
    }
}

