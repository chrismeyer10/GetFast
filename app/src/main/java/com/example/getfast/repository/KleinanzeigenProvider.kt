package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Provider für Anzeigen von eBay Kleinanzeigen.
 */
class KleinanzeigenProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ProviderListingParser = KleinanzeigenListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.KLEINANZEIGEN

    /**
     * Baut die Abfrage-URL und liefert die geparsten Listings zurück.
     */
    override suspend fun fetchListingsForFilter(filter: SearchFilter): List<Listing> {
        val url = buildRequestUrl(filter)
        return runCatching {
            val document = fetcher.fetch(url)
            parser.parseListingsFromDocument(document)
        }.getOrElse { emptyList() }
    }

    /**
     * Erzeugt die URL für die Kleinanzeigen-Suche.
     */
    private fun buildRequestUrl(filter: SearchFilter): String {
        val location = URLEncoder.encode(
            filter.city.pathFor(source),
            StandardCharsets.UTF_8.toString()
        )
        return "https://www.kleinanzeigen.de/s-wohnung-mieten/c203l0?locationStr=$location&radius=0"
    }
}

