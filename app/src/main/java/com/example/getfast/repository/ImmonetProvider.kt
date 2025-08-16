package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für Immonet. */
class ImmonetProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.IMMONET

    override suspend fun fetchListings(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.displayName, StandardCharsets.UTF_8.toString())
        val price = filter.maxPrice?.let { "&toprice=$it" } ?: ""
        val url = "https://www.immonet.de/wohnung-mieten.html?city=$city$price"
        return runCatching {
            val doc = fetcher.fetch(url)
            parser.parseImmonet(doc)
        }.getOrElse { emptyList() }
    }
}

