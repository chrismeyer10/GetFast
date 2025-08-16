package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter

/**
 * Repository zum Abrufen und Filtern von Wohnungsanzeigen.
 * Die konkreten Schritte wie Netzwerkzugriff und Parsing
 * werden über eigene Klassen delegiert, damit die Klasse
 * klein und erweiterbar bleibt.
 */
class EbayRepository(
    private val fetcher: HtmlFetcher = JsoupHtmlFetcher(),
    private val parser: ListingParser = ListingParser(),
) {
    /**
     * Lädt aktuelle Listings entsprechend dem Filter.
     * Fehler führen zu einer leeren Liste.
     */
    suspend fun fetchLatestListings(filter: SearchFilter): List<Listing> {
        val url = "https://www.kleinanzeigen.de/s-wohnung-mieten/${filter.city.urlPath}"
        return try {
            val doc = fetcher.fetch(url)
            val listings = parser.parse(doc)
            val maxPrice = filter.maxPrice
            if (maxPrice != null) {
                listings.filter { listing ->
                    val numeric = listing.price.replace("\\D".toRegex(), "")
                    val priceValue = numeric.toIntOrNull()
                    priceValue != null && priceValue <= maxPrice
                }
            } else {
                listings
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
