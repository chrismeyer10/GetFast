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
    private val providers: Map<ListingSource, ListingProvider> = defaultProviders(),
) {
    /**
     * Lädt aktuelle Listings entsprechend dem Filter.
     * Fehler einzelner Anbieter führen zu einer leeren Liste
     * für diesen Anbieter.
     */
    suspend fun fetchLatestListings(filter: SearchFilter): List<Listing> {
        val results = mutableListOf<Listing>()
        for (source in filter.sources) {
            val provider = providers[source] ?: continue
            results += provider.fetchListings(filter)
        }
        val maxPrice = filter.maxPrice
        val priceFiltered = if (maxPrice != null) {
            results.filter { listing ->
                val numeric = listing.price.replace("\\D".toRegex(), "")
                val priceValue = numeric.toIntOrNull()
                priceValue != null && priceValue <= maxPrice
            }
        } else {
            results
        }
        val maxDays = filter.maxAgeDays.coerceAtMost(3)
        return priceFiltered.filter { listing ->
            isWithinDays(listing.date, maxDays)
        }
    }

    private fun isWithinDays(dateText: String, maxDays: Int): Boolean {
        val date = parseDate(dateText) ?: return true
        val threshold = LocalDate.now().minusDays(maxDays.toLong())
        return !date.isBefore(threshold)
    }

    private fun parseDate(text: String): LocalDate? {
        val lower = text.lowercase(Locale.getDefault()).trim()
        val today = LocalDate.now()
        return when {
            lower.startsWith("heute") -> today
            lower.startsWith("gestern") -> today.minusDays(1)
            else -> {
                val cleaned = lower.substringBefore(",").substringBefore(" ").trim()
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                runCatching { LocalDate.parse(cleaned, formatter) }.getOrNull()
            }
        }
    }

    companion object {
        private fun defaultProviders(): Map<ListingSource, ListingProvider> {
            val fetcher = JsoupHtmlFetcher()
            val parser = ListingParser()
            return mapOf(
                ListingSource.KLEINANZEIGEN to KleinanzeigenProvider(fetcher, parser),
                ListingSource.IMMOSCOUT to ImmoscoutProvider(fetcher, parser),
                ListingSource.IMMONET to ImmonetProvider(fetcher, parser),
                ListingSource.IMMOWELT to ImmoweltProvider(fetcher, parser),
                ListingSource.WOHNUNGSBOERSE to WohnungsboerseProvider(fetcher, parser),
            )
        }
    }
}

