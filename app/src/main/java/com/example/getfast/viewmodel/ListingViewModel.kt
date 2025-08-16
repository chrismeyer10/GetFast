package com.example.getfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.repository.EbayRepository
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListingViewModel(
    private val repository: EbayRepository = EbayRepository(),
) : ViewModel() {

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    private val _lastFetchTime = MutableStateFlow<String?>(null)
    val lastFetchTime: StateFlow<String?> = _lastFetchTime

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun refreshListings() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _listings.value = repository.fetchLatestListings(_filter.value)
                .sortedByDescending { it.id.toLongOrNull() ?: Long.MIN_VALUE }
            _lastFetchTime.value = formatter.format(Date())
            _isRefreshing.value = false
        }
    }

    fun updateFilter(newFilter: SearchFilter) {
        _filter.value = newFilter
        refreshListings()
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

