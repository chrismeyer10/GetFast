package com.example.getfast.repository

import com.example.getfast.model.City
import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter

/**
 * Repository zum Abrufen und Filtern von Wohnungsanzeigen.
 * Die konkreten Schritte zum Laden und Parsen der Anbieter
 * werden an [KleinanzeigenProvider] delegiert.
 */
class ListingRepository(
    private val fetchListings: suspend (SearchFilter) -> List<Listing> = defaultFetcher(),
) {
    /**
     * Lädt aktuelle Listings entsprechend dem Filter und kapselt die Filterlogik.
     */
    suspend fun fetchLatestListingsMatchingFilter(filter: SearchFilter): List<Listing> {
        val listingsFromProvider = fetchListings(filter)
        return applyCityFilter(listingsFromProvider, filter.city)
    }

    /**
     * Entfernt Listings, die nicht zur ausgewählten Stadt passen.
     */
    private fun applyCityFilter(listings: List<Listing>, city: City): List<Listing> {
        val normalizedTarget = City.normalize(city.displayName)
        if (normalizedTarget.isEmpty()) {
            return listings
        }
        return listings.filter { listing ->
            val normalizedListingCity = City.normalize(listing.city)
            normalizedListingCity.isNotEmpty() && (
                normalizedListingCity == normalizedTarget ||
                    normalizedListingCity.contains(normalizedTarget) ||
                    normalizedTarget.contains(normalizedListingCity)
                )
        }
    }
    companion object {
        private fun defaultFetcher(): suspend (SearchFilter) -> List<Listing> {
            val provider = KleinanzeigenProvider()
            return { filter -> provider.fetchListingsForFilter(filter) }
        }
    }
}
