package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.model.ListingSource

/**
 * Repository zum Abrufen und Filtern von Wohnungsanzeigen.
 * Die konkreten Schritte wie Netzwerkzugriff und Parsing
 * werden über eigene Klassen delegiert, damit die Klasse
 * klein und erweiterbar bleibt.
 */
class ListingRepository(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) {
    /**
     * Lädt aktuelle Listings entsprechend dem Filter.
     * Fehler führen zu einer leeren Liste.
     */
    suspend fun fetchLatestListings(filter: SearchFilter): List<Listing> {
        val results = mutableListOf<Listing>()
        if (ListingSource.KLEINANZEIGEN in filter.sources) {
            results += fetchKleinanzeigen(filter)
        }
        if (ListingSource.IMMOSCOUT in filter.sources) {
            results += fetchImmoscout(filter)
        }
        val maxPrice = filter.maxPrice
        return if (maxPrice != null) {
            results.filter { listing ->
                val numeric = listing.price.replace("\\D".toRegex(), "")
                val priceValue = numeric.toIntOrNull()
                priceValue != null && priceValue <= maxPrice
            }
        } else {
            results
        }
    }

    private suspend fun fetchKleinanzeigen(filter: SearchFilter): List<Listing> {
        val url = "https://www.kleinanzeigen.de/s-wohnung-mieten/${filter.city.urlPath}"
        return try {
            val doc = fetcher.fetch(url)
            parser.parse(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchImmoscout(filter: SearchFilter): List<Listing> {
        val url = "https://www.immobilienscout24.de/Suche/radius/wohnung-mieten?centerofsearchaddress=${filter.city.displayName}"
        return try {
            val doc = fetcher.fetch(url)
            parser.parseImmoscout(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
