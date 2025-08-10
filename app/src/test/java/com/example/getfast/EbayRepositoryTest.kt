package com.example.getfast

import com.example.getfast.repository.EbayRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class EbayRepositoryTest {
    @Test
    fun fetchLatestListings_returnsEmpty() = runBlocking {
        val repo = EbayRepository()
        val listings = repo.fetchLatestListings()
        assertTrue(listings.isEmpty())
    }
}
