package com.example.getfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getfast.model.Listing
import com.example.getfast.repository.EbayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListingViewModel(
    private val repository: EbayRepository = EbayRepository()
) : ViewModel() {

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    fun refreshListings() {
        viewModelScope.launch {
            _listings.value = repository.fetchLatestListings()
        }
    }
}
