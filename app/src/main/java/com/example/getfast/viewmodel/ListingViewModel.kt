package com.example.getfast.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import com.example.getfast.repository.ListingRepository
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * ViewModel hält Zustand und Geschäftslogik der Listing-Ansicht.
 */
class ListingViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = ListingRepository()

    private val dataStore = application.dataStore
    private val favoritesKey = stringSetPreferencesKey("favorites")
    private val archivedKey = stringSetPreferencesKey("archived")

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    private val _lastFetchTime = MutableStateFlow<String?>(null)
    val lastFetchTime: StateFlow<String?> = _lastFetchTime

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _archived = MutableStateFlow<Set<String>>(emptySet())
    val archived: StateFlow<Set<String>> = _archived


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _favorites.value = prefs[favoritesKey] ?: emptySet()
            _archived.value = prefs[archivedKey] ?: emptySet()
        }
    }

    /**
     * Lädt Listings vom Repository und aktualisiert den Zeitstempel.
     */
    fun refreshListingsFromRepository() {
        viewModelScope.launch {
            startRefreshingState()
            val sortedListings = loadAndSortListings()
            updateListingsAndTimestamp(sortedListings)
            stopRefreshingState()
        }
    }

    /**
     * Aktualisiert den Suchfilter und lädt sofort neue Daten.
     */
    fun updateFilterAndReloadListings(newFilter: SearchFilter) {
        val previousFilter = _filter.value
        _filter.value = newFilter
        if (previousFilter.city != newFilter.city) {
            _listings.value = emptyList()
        }
        refreshListingsFromRepository()
    }

    /**
     * Merkt oder entfernt ein Listing aus den Favoriten.
     */
    fun toggleFavoriteSelectionForListing(listing: Listing) {
        val id = listing.id
        _favorites.value = computeUpdatedFavorites(id)
        persistFavoritesAsync()
    }


    /**
     * Markiert ein Listing als archiviert und speichert den Zustand.
     */
    fun archiveListing(listing: Listing) {
        val id = listing.id
        _archived.value = _archived.value + id
        persistArchivedAsync()
    }

    /**
     * Entfernt alle Favoriten und speichert den Zustand.
     */
    fun clearAllFavoriteListings() {
        _favorites.value = emptySet()
        persistFavoritesAsync(emptySet())
    }

    /**
     * Setzt Favoriten, Archiv und Filter zurück.
     */
    fun resetApplicationState() {
        _favorites.value = emptySet()
        _archived.value = emptySet()
        _filter.value = SearchFilter()
        persistFavoritesAndArchivedAsync()
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
     * Lädt Listings aus dem Repository und sortiert sie nach ID.
     */
    private suspend fun loadAndSortListings(): List<Listing> {
        val listings = repository.fetchLatestListingsMatchingFilter(_filter.value)
        return listings.sortedByDescending { it.id.toLongOrNull() ?: Long.MIN_VALUE }
    }

    /**
     * Speichert Listings und aktualisiert den Zeitstempel.
     */
    private fun updateListingsAndTimestamp(listings: List<Listing>) {
        _listings.value = listings
        _lastFetchTime.value = formatter.format(Date())
    }

    /**
     * Ermittelt die neue Menge an Favoriten nach einem Toggle.
     */
    private fun computeUpdatedFavorites(id: String): Set<String> {
        return if (_favorites.value.contains(id)) {
            _favorites.value - id
        } else {
            _favorites.value + id
        }
    }

    /**
     * Speichert Favoriten asynchron.
     */
    private fun persistFavoritesAsync(customFavorites: Set<String>? = null) {
        val favoritesToStore = customFavorites ?: _favorites.value
        viewModelScope.launch {
            dataStore.edit { it[favoritesKey] = favoritesToStore }
        }
    }

    /**
     * Speichert archivierte Listings asynchron.
     */
    private fun persistArchivedAsync() {
        viewModelScope.launch {
            dataStore.edit { it[archivedKey] = _archived.value }
        }
    }

    /**
     * Speichert Favoriten und Archiv gleichzeitig.
     */
    private fun persistFavoritesAndArchivedAsync() {
        viewModelScope.launch {
            dataStore.edit {
                it[favoritesKey] = _favorites.value
                it[archivedKey] = _archived.value
            }
        }
    }
}
