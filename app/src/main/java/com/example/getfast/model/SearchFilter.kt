package com.example.getfast.model

/**
 * Represents user-defined search filtering options.
 *
 * @property city      City to search within. Controls the remote search URL.
 * @property maxPrice  Optional maximum price in Euro. Listings above this price
 *                     will be filtered out after retrieval.
 */
data class SearchFilter(
    val city: City = City.BERLIN,
    val maxPrice: Int? = null,
    val sources: Set<ListingSource> = setOf(ListingSource.KLEINANZEIGEN),
)

/**
 * Limited set of supported cities and their respective URL paths on Kleinanzeigen.
 * The structure makes it easy to extend with additional cities in the future.
 */
enum class City(val displayName: String, val urlPath: String) {
    BERLIN("Berlin", "berlin/c203l3331"),
    HAMBURG("Hamburg", "hamburg/c203l9409"),
    MUNICH("München", "muenchen/c203l6436"),
}

/**
 * Supported listing sources.
 */
enum class ListingSource {
    KLEINANZEIGEN,
    IMMOSCOUT,
}
