package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für Wohnungsboerse.net. */
class WohnungsboerseProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.WOHNUNGSBOERSE

    override suspend fun fetchListings(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.displayName, StandardCharsets.UTF_8)
        val price = filter.maxPrice?.let { "?maxMiete=$it" } ?: ""
        val url = if (price.isEmpty()) {
            "https://www.wohnungsboerse.net/$city/mietwohnungen"
        } else {
            "https://www.wohnungsboerse.net/$city/mietwohnungen$price"
        }
        return runCatching {
            val doc = fetcher.fetch(url)
            parser.parseWohnungsboerse(doc)
        }.getOrElse { emptyList() }
    }
}

