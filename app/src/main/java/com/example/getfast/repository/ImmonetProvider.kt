package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Provider für Immonet. */
class ImmonetProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ProviderListingParser = ImmonetListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.IMMONET

    /**
     * Baut die Immonet-URL und liefert die geparsten Listings.
     */
    override suspend fun fetchListingsForFilter(filter: SearchFilter): List<Listing> {
        val url = buildRequestUrl(filter)
        return runCatching {
            val document = fetcher.fetch(url)
            parser.parseListingsFromDocument(document)
        }.getOrElse { emptyList() }
    }

    /**
     * Erstellt die Such-URL für Immonet.
     */
    private fun buildRequestUrl(filter: SearchFilter): String {
        val city = URLEncoder.encode(filter.city.pathFor(source), StandardCharsets.UTF_8.toString())
        val price = filter.maxPrice?.let { "&toprice=$it" } ?: ""
        return "https://www.immonet.de/wohnung-mieten.html?city=$city$price"
    }
}

