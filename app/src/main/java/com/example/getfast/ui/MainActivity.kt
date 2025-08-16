package com.example.getfast.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.notifications.Notifier
import com.example.getfast.ui.components.ListingList
import com.example.getfast.ui.theme.GetFastTheme
import com.example.getfast.viewmodel.ListingViewModel
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ListingViewModel by viewModels()

    /**
     * Initialisiert die Oberfläche und startet das Beobachten der Daten.
     * Hier werden Berechtigungen geprüft und das UI gesetzt.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (
            Build.VERSION.SDK_INT >= 33 &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0,
            )
        }
        val notifier = Notifier(this)
        setContent {
            val darkMode by viewModel.darkMode.collectAsState()
            GetFastTheme(darkTheme = darkMode) {
                val listings by viewModel.listings.collectAsState()
                val lastFetch by viewModel.lastFetchTime.collectAsState()
                val favorites by viewModel.favorites.collectAsState()
                val filter by viewModel.filter.collectAsState()
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val archived by viewModel.archived.collectAsState()
                var showFavoritesOnly by remember { mutableStateOf(false) }
                var currentTab by remember { mutableStateOf(ListingTab.OFFERS) }
                var showSettings by remember { mutableStateOf(false) }
                val seenIds = remember { mutableSetOf<String>() }
                val blinkingIds = remember { mutableStateOf<Set<String>>(emptySet()) }
                LaunchedEffect(listings) {
                    val newItems = listings.filter { it.id !in seenIds }
                    newItems.forEach { listing ->
                        notifier.notifyNewListing(listing.title)
                        blinkingIds.value = blinkingIds.value + listing.id
                        launch {
                            delay(10_000)
                            blinkingIds.value = blinkingIds.value - listing.id
                        }
                    }
                    seenIds.addAll(newItems.map { it.id })
                }
                val tabFiltered = listings
                    .filter { it.id !in archived }
                    .filter { if (currentTab == ListingTab.SEARCH) it.isSearch else !it.isSearch }
                val highlightedIds = tabFiltered.take(2).map { it.id }.toSet()
                if (showSettings) {
                    SettingsScreen(
                        filter = filter,
                        darkMode = darkMode,
                        onApply = {
                            viewModel.updateFilter(it)
                            showSettings = false
                        },
                        onDarkModeChange = { viewModel.setDarkMode(it) },
                        onBack = { showSettings = false },
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.background,
                                    ),
                                ),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.refreshListings() }) {
                            Text(text = stringResource(id = R.string.refresh))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(id = R.string.open_settings))
                            }
                            Text(
                                text = stringResource(
                                    id = R.string.last_fetch,
                                    lastFetch ?: "--",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            OutlinedTextField(
                                value = selectedCity.displayName,
                                onValueChange = {},
                                label = { Text(text = stringResource(id = R.string.city_label)) },
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .height(56.dp),
                                singleLine = true,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.refreshListings() }) {
                                Text(text = stringResource(id = R.string.refresh))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(text = stringResource(id = R.string.max_price_label)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            onClick = {
                                viewModel.updateFilter(
                                    SearchFilter(
                                        city = selectedCity,
                                        maxPrice = priceText.toIntOrNull(),
                                    ),
                                )
                            },
                        ) {
                            Text(text = stringResource(id = R.string.apply_filters))
                        }
                    }

                    TabRow(selectedTabIndex = currentTab.ordinal) {
                        Tab(
                            selected = currentTab == ListingTab.OFFERS,
                            onClick = { currentTab = ListingTab.OFFERS },
                            text = { Text(text = stringResource(id = R.string.offers_tab)) },
                        )
                        Tab(
                            selected = currentTab == ListingTab.SEARCH,
                            onClick = { currentTab = ListingTab.SEARCH },
                            text = { Text(text = stringResource(id = R.string.search_tab)) },
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = !showFavoritesOnly,
                            onClick = { showFavoritesOnly = false },
                            label = { Text(text = stringResource(id = R.string.all_tab)) },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (showFavoritesOnly && favorites.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearFavorites() }) {
                                Text(text = stringResource(id = R.string.clear_favorites))
                            }
                        }
                        FilterChip(
                            selected = showFavoritesOnly,
                            onClick = { showFavoritesOnly = true },
                            label = { Text(text = stringResource(id = R.string.favorites_tab)) },
                        )
                    }
                    ListingList(
                        listings = tabFiltered,
                        favorites = favorites,
                        favoritesOnly = showFavoritesOnly,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        highlightedIds = highlightedIds,
                        blinkingIds = blinkingIds.value,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refreshListings() },
                        onArchive = { viewModel.archive(it) },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(id = R.string.copyright),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        viewModel.refreshListings()
    }
}
