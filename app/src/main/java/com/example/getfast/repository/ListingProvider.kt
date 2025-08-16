package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.model.ListingSource

/**
 * Allgemeine Schnittstelle für einen Anbieter von Wohnungsanzeigen.
 */
interface ListingProvider {
    /** Quelle, die dieser Provider bedient. */
    val source: ListingSource

    /**
     * Lädt und parst Listings entsprechend des Filters.
     * Fehler sollten zu einer leeren Liste führen.
     */
    suspend fun fetchListings(filter: SearchFilter): List<Listing>
}

