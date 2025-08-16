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
    val city: City = City.BERLIN,
    val maxPrice: Int? = null,
    val maxAgeDays: Int = 3,
    val sources: Set<ListingSource> = ListingSource.values().toSet(),
)

/**
 * Supported cities and the provider specific URL paths or identifiers
 * used to build search URLs.
 */
enum class City(
    val displayName: String,
    private val providerPaths: Map<ListingSource, String>
) {
    BERLIN(
        "Berlin",
        mapOf(
            ListingSource.KLEINANZEIGEN to "berlin/c203l3331",
            ListingSource.IMMOSCOUT to "Berlin",
            ListingSource.IMMONET to "berlin",
            ListingSource.IMMOWELT to "berlin",
            ListingSource.WOHNUNGSBOERSE to "berlin",
        ),
    ),
    HAMBURG(
        "Hamburg",
        mapOf(
            ListingSource.KLEINANZEIGEN to "hamburg/c203l9409",
            ListingSource.IMMOSCOUT to "Hamburg",
            ListingSource.IMMONET to "hamburg",
            ListingSource.IMMOWELT to "hamburg",
            ListingSource.WOHNUNGSBOERSE to "hamburg",
        ),
    ),
    MUNICH(
        "München",
        mapOf(
            ListingSource.KLEINANZEIGEN to "muenchen/c203l6436",
            ListingSource.IMMOSCOUT to "München",
            ListingSource.IMMONET to "muenchen",
            ListingSource.IMMOWELT to "muenchen",
            ListingSource.WOHNUNGSBOERSE to "muenchen",
        ),
    ),
    COLOGNE(
        "Köln",
        mapOf(
            ListingSource.KLEINANZEIGEN to "koeln/c203l9480",
            ListingSource.IMMOSCOUT to "Köln",
            ListingSource.IMMONET to "koeln",
            ListingSource.IMMOWELT to "koeln",
            ListingSource.WOHNUNGSBOERSE to "koeln",
        ),
    ),
    FRANKFURT(
        "Frankfurt am Main",
        mapOf(
            ListingSource.KLEINANZEIGEN to "frankfurt-main/c203l1723",
            ListingSource.IMMOSCOUT to "Frankfurt am Main",
            ListingSource.IMMONET to "frankfurt-am-main",
            ListingSource.IMMOWELT to "frankfurt-am-main",
            ListingSource.WOHNUNGSBOERSE to "frankfurt-am-main",
        ),
    ),
    STUTTGART(
        "Stuttgart",
        mapOf(
            ListingSource.KLEINANZEIGEN to "stuttgart/c203l11538",
            ListingSource.IMMOSCOUT to "Stuttgart",
            ListingSource.IMMONET to "stuttgart",
            ListingSource.IMMOWELT to "stuttgart",
            ListingSource.WOHNUNGSBOERSE to "stuttgart",
        ),
    ),
    DUSSELDORF(
        "Düsseldorf",
        mapOf(
            ListingSource.KLEINANZEIGEN to "duesseldorf/c203l9435",
            ListingSource.IMMOSCOUT to "Düsseldorf",
            ListingSource.IMMONET to "duesseldorf",
            ListingSource.IMMOWELT to "duesseldorf",
            ListingSource.WOHNUNGSBOERSE to "duesseldorf",
        ),
    );

    fun pathFor(source: ListingSource): String =
        providerPaths[source] ?: displayName
}

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
