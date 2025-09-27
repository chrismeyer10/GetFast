package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter

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
        val path = filter.city.pathFor(source)
        return "https://www.kleinanzeigen.de/s-wohnung-mieten/$path"
    }
}

