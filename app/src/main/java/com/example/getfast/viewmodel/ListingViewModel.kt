package com.example.getfast.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    private val _lastFetchTime = MutableStateFlow<String?>(null)
    val lastFetchTime: StateFlow<String?> = _lastFetchTime

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _favorites.value = prefs[favoritesKey] ?: emptySet()
            _darkMode.value = prefs[darkModeKey] ?: false
        }
    }

    /**
     * Lädt Listings vom Repository und aktualisiert den Zeitstempel.
     */
    fun refreshListings() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _listings.value = repository.fetchLatestListings(_filter.value)
                .sortedByDescending { it.id.toLongOrNull() ?: Long.MIN_VALUE }
            _lastFetchTime.value = formatter.format(Date())
            _isRefreshing.value = false
        }
    }

    /**
     * Aktualisiert den Suchfilter und lädt sofort neue Daten.
     */
    fun updateFilter(newFilter: SearchFilter) {
        _filter.value = newFilter
        refreshListings()
    }

    /**
     * Merkt oder entfernt ein Listing aus den Favoriten.
     */
    fun toggleFavorite(listing: Listing) {
        val id = listing.id
        _favorites.value = if (_favorites.value.contains(id)) {
            _favorites.value - id
        } else {
            _favorites.value + id
        }
        viewModelScope.launch {
            dataStore.edit { it[favoritesKey] = _favorites.value }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        viewModelScope.launch {
            dataStore.edit { it[darkModeKey] = enabled }
        }
    }

    /**
     * Entfernt alle Favoriten und speichert den Zustand.
     */
    fun clearFavorites() {
        _favorites.value = emptySet()
        viewModelScope.launch {
            dataStore.edit { it[favoritesKey] = emptySet() }
        }
    }
}
