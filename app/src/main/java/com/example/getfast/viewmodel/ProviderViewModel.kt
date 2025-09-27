package com.example.getfast.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getfast.model.Listing
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter
import com.example.getfast.repository.ListingRepository
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel zum Laden von Listings für einen einzelnen Anbieter.
 * Jeder Provider erhält eine eigene Instanz dieser ViewModels.
 */
class ProviderViewModel(
    application: Application,
    private val source: ListingSource,
) : AndroidViewModel(application) {

    private val repository = ListingRepository()

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _filter = MutableStateFlow(SearchFilter(sources = setOf(source)))
    val filter: StateFlow<SearchFilter> = _filter

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    private val _lastFetchTime = MutableStateFlow<String?>(null)
    val lastFetchTime: StateFlow<String?> = _lastFetchTime

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    /**
     * Lädt die neuesten Listings für den konfigurierten Anbieter.
     */
    fun refreshListingsFromRepository() {
        viewModelScope.launch {
            startRefreshingState()
            _listings.value = repository.fetchLatestListingsMatchingFilter(_filter.value)
            updateTimestamp()
            stopRefreshingState()
        }
    }

    /**
     * Aktualisiert den Filter und lädt sofort neue Daten.
     */
    fun updateFilterAndReloadListings(newFilter: SearchFilter) {
        _filter.value = newFilter
        refreshListingsFromRepository()
    }

    /**
     * Startet den Ladezustand.
     */
    private fun startRefreshingState() {
        _isRefreshing.value = true
    }

    /**
     * Stoppt den Ladezustand.
     */
    private fun stopRefreshingState() {
        _isRefreshing.value = false
    }

    /**
     * Aktualisiert den Zeitstempel der letzten Aktualisierung.
     */
    private fun updateTimestamp() {
        _lastFetchTime.value = formatter.format(Date())
    }

    companion object {
        /** Factory zum Erzeugen eines ProviderViewModel mit Source Parameter. */
        fun factory(application: Application, source: ListingSource): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProviderViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ProviderViewModel(application, source) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

