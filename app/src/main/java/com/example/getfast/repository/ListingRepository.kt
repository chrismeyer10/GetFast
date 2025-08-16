package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.model.ListingSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        if (ListingSource.IMMONET in filter.sources) {
            results += fetchImmonet(filter)
        }
        if (ListingSource.IMMOWELT in filter.sources) {
            results += fetchImmowelt(filter)
        }
        if (ListingSource.WOHNUNGSBOERSE in filter.sources) {
            results += fetchWohnungsboerse(filter)
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

    private suspend fun fetchKleinanzeigen(filter: SearchFilter): List<Listing> {
        val path = filter.city.pathFor(ListingSource.KLEINANZEIGEN)
        val url = "https://www.kleinanzeigen.de/s-wohnung-mieten/$path"
        return try {
            val doc = fetcher.fetch(url)
            parser.parse(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchImmoscout(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.displayName, StandardCharsets.UTF_8)
        val price = filter.maxPrice?.let { "&price=-$it" } ?: ""
        val url = "https://www.immobilienscout24.de/Suche/radius/wohnung-mieten?centerofsearchaddress=$city$price"
        return try {
            val doc = fetcher.fetch(url)
            parser.parseImmoscout(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchImmonet(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.displayName, StandardCharsets.UTF_8)
        val price = filter.maxPrice?.let { "&toprice=$it" } ?: ""
        val url = "https://www.immonet.de/wohnung-mieten.html?city=$city$price"

        return try {
            val doc = fetcher.fetch(url)
            parser.parseImmonet(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchImmowelt(filter: SearchFilter): List<Listing> {

        val city = URLEncoder.encode(filter.city.displayName, StandardCharsets.UTF_8)
        val price = filter.maxPrice?.let { "&maxprice=$it" } ?: ""
        val url = "https://www.immowelt.de/suche/wohnung-mieten?city=$city$price"
        return try {
            val doc = fetcher.fetch(url)
            parser.parseImmowelt(doc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchWohnungsboerse(filter: SearchFilter): List<Listing> {
        val city = URLEncoder.encode(filter.city.displayName, StandardCharsets.UTF_8)
        val price = filter.maxPrice?.let { "?maxMiete=$it" } ?: ""
        val url = if (price.isEmpty()) {
            "https://www.wohnungsboerse.net/$city/mietwohnungen"
        } else {
            "https://www.wohnungsboerse.net/$city/mietwohnungen$price"
        }
        return try {
            val doc = fetcher.fetch(url)
            parser.parseWohnungsboerse(doc)
        } catch (e: Exception) {
            emptyList()
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
}
