package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter

/** Provider für Wohnungsboerse.net. */
class WohnungsboerseProvider(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ProviderListingParser = WohnungsboerseListingParser(),
) : ListingProvider {
    override val source: ListingSource = ListingSource.WOHNUNGSBOERSE

    /**
     * Bereitet die Anfrage vor und liefert die Listings.
     */
    override suspend fun fetchListingsForFilter(filter: SearchFilter): List<Listing> {
        val url = buildRequestUrl(filter)
        return runCatching {
            val document = fetcher.fetch(url)
            parser.parseListingsFromDocument(document)
        }.getOrElse { emptyList() }
    }

    /**
     * Baut die URL für Wohnungsboerse.net inklusive Preisfilter.
     */
    private fun buildRequestUrl(filter: SearchFilter): String {
        val city = filter.city.pathFor(source)
        val priceSuffix = filter.maxPrice?.let { "?maxMiete=$it" } ?: ""
        return if (priceSuffix.isEmpty()) {
            "https://www.wohnungsboerse.net/$city/mietwohnungen"
        } else {
            "https://www.wohnungsboerse.net/$city/mietwohnungen$priceSuffix"
        }
    }
}

