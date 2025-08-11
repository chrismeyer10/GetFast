package com.example.getfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getfast.model.Listing
import com.example.getfast.repository.EbayRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListingViewModel(
    private val repository: EbayRepository = EbayRepository(),
) : ViewModel() {

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val formatter = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
    private val _lastFetchTime = MutableStateFlow<String?>(null)
    val lastFetchTime: StateFlow<String?> = _lastFetchTime

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    fun refreshListings() {
        viewModelScope.launch {
            _listings.value = repository.fetchLatestListings()
            _lastFetchTime.value = formatter.format(Date())
        }
    }

    fun toggleFavorite(listing: Listing) {
        val id = listing.id
        _favorites.value = if (_favorites.value.contains(id)) {
            _favorites.value - id
        } else {
            _favorites.value + id
        }
    }
}

