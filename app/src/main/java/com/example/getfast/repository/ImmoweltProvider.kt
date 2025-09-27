package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für Immowelt. */
class ImmoweltProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ProviderListingParser = ImmoweltListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.IMMOWELT

    /**
     * Kombiniert Filterparameter und liefert Immowelt-Listings.
     */
    override suspend fun fetchListingsForFilter(filter: SearchFilter): List<Listing> {
        val url = buildRequestUrl(filter)
        return runCatching {
            val document = fetcher.fetch(url)
            parser.parseListingsFromDocument(document)
        }.getOrElse { emptyList() }
    }

    /**
     * Erstellt die Such-URL für Immowelt.
     */
    private fun buildRequestUrl(filter: SearchFilter): String {
        val city = URLEncoder.encode(filter.city.pathFor(source), StandardCharsets.UTF_8.toString())
        val price = filter.maxPrice?.let { "&maxprice=$it" } ?: ""
        return "https://www.immowelt.de/suche/wohnung-mieten?city=$city$price"
    }
}

