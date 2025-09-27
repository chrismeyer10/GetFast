package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für ImmoScout24. */
class ImmoscoutProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ProviderListingParser = ImmoscoutListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.IMMOSCOUT

    /**
     * Kombiniert Filterparameter zu einer URL und liefert die Listings.
     */
    override suspend fun fetchListingsForFilter(filter: SearchFilter): List<Listing> {
        val url = buildRequestUrl(filter)
        return runCatching {
            val document = fetcher.fetch(url)
            parser.parseListingsFromDocument(document)
        }.getOrElse { emptyList() }
    }

    /**
     * Erstellt die Such-URL für ImmoScout24.
     */
    private fun buildRequestUrl(filter: SearchFilter): String {
        val city = URLEncoder.encode(filter.city.pathFor(source), StandardCharsets.UTF_8.toString())
        val price = filter.maxPrice?.let { "&price=-$it" } ?: ""
        return "https://www.immobilienscout24.de/Suche/radius/wohnung-mieten?centerofsearchaddress=$city$price"
    }
}

