package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Repository zum Abrufen und Filtern von Wohnungsanzeigen.
 * Die konkreten Schritte zum Laden und Parsen der Anbieter
 * werden an implementierungen von [ListingProvider] delegiert.
 */
class ListingRepository(
    private val providers: Map<ListingSource, ListingProvider> = createDefaultProviders(),
) {
    /**
     * Lädt aktuelle Listings entsprechend dem Filter und kapselt die Filterlogik.
     * Fehler einzelner Anbieter führen zu einer leeren Liste für diesen Anbieter.
     */
    suspend fun fetchLatestListingsMatchingFilter(filter: SearchFilter): List<Listing> {
        val listingsFromProviders = collectListingsFromSelectedSources(filter)
        val priceFilteredListings = applyMaxPriceFilter(listingsFromProviders, filter.maxPrice)
        val maxDays = filter.maxAgeDays.coerceAtMost(3)
        return applyMaxAgeFilter(priceFilteredListings, maxDays)
    }

    /**
     * Lädt Listings für alle im Filter gewählten Quellen.
     */
    private suspend fun collectListingsFromSelectedSources(filter: SearchFilter): List<Listing> {
        val results = mutableListOf<Listing>()
        for (source in filter.sources) {
            val provider = providers[source] ?: continue
            results += provider.fetchListingsForFilter(filter)
        }
        return results
    }

    /**
     * Entfernt Listings, die über dem Maximalpreis liegen.
     */
    private fun applyMaxPriceFilter(listings: List<Listing>, maxPrice: Int?): List<Listing> {
        if (maxPrice == null) {
            return listings
        }
        return listings.filter { listing ->
            val numeric = listing.price.replace("\\D".toRegex(), "")
            val priceValue = numeric.toIntOrNull()
            priceValue != null && priceValue <= maxPrice
        }
    }

    /**
     * Filtert Listings anhand des maximalen Alters in Tagen.
     */
    private fun applyMaxAgeFilter(listings: List<Listing>, maxDays: Int): List<Listing> {
        return listings.filter { listing ->
            isListingWithinMaxAgeDays(listing.date, maxDays)
        }
    }

    /**
     * Prüft, ob das Listing innerhalb der erlaubten Tage liegt.
     */
    private fun isListingWithinMaxAgeDays(dateText: String, maxDays: Int): Boolean {
        val listingDate = parseListingDateText(dateText) ?: return true
        val threshold = LocalDate.now().minusDays(maxDays.toLong())
        return !listingDate.isBefore(threshold)
    }

    /**
     * Wandelt einen Datumstext in ein Datum um.
     */
    private fun parseListingDateText(text: String): LocalDate? {
        val lower = text.lowercase(Locale.getDefault()).trim()
        val today = LocalDate.now()
        return when {
            lower.startsWith("heute") -> today
            lower.startsWith("gestern") -> today.minusDays(1)
            else -> parseFormattedDate(lower)
        }
    }

    /**
     * Liest ein Datum im Format dd.MM.yyyy.
     */
    private fun parseFormattedDate(text: String): LocalDate? {
        val cleaned = text.substringBefore(",").substringBefore(" ").trim()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return runCatching { LocalDate.parse(cleaned, formatter) }.getOrNull()
    }

    companion object {
        /**
         * Erstellt die Standard-Provider mit ihren Parsern.
         */
        private fun createDefaultProviders(): Map<ListingSource, ListingProvider> {
            val fetcher = JsoupHtmlFetcher()
            return mapOf(
                ListingSource.KLEINANZEIGEN to KleinanzeigenProvider(fetcher, KleinanzeigenListingParser()),
                ListingSource.IMMOSCOUT to ImmoscoutProvider(fetcher, ImmoscoutListingParser()),
                ListingSource.IMMONET to ImmonetProvider(fetcher, ImmonetListingParser()),
                ListingSource.IMMOWELT to ImmoweltProvider(fetcher, ImmoweltListingParser()),
                ListingSource.WOHNUNGSBOERSE to WohnungsboerseProvider(fetcher, WohnungsboerseListingParser()),
            )
        }
    }
}

