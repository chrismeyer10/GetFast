package com.example.getfast.repository

import com.example.getfast.model.Listing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EbayRepository {
    suspend fun fetchLatestListings(): List<Listing> = withContext(Dispatchers.IO) {
        // TODO: Implement real fetch from eBay Kleinanzeigen
        emptyList()
    }
}
