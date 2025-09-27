package com.example.getfast.model

/**
 * Represents user-defined search filtering options.
 *
 * @property city      City to search within. Controls the remote search URL.
 * @property maxPrice  Optional maximum price in Euro. Listings above this price
 *                     will be filtered out after retrieval.
 * @property maxAgeDays  Maximum age of listings in days. Older listings will
 *                       be discarded. The value is capped at 3 days.
 */
data class SearchFilter(
    val city: City = CityCatalog.defaultCity,
    val maxPrice: Int? = null,
    val maxAgeDays: Int = 3,
    val sources: Set<ListingSource> = ListingSource.values().toSet(),
)

/**
 * Supported listing sources.
 */
enum class ListingSource {
    KLEINANZEIGEN,
    IMMOSCOUT,
    IMMONET,
    IMMOWELT,
    WOHNUNGSBOERSE,
}
