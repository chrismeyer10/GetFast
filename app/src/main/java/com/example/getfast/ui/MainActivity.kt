package com.example.getfast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.getfast.notifications.Notifier
import com.example.getfast.ui.components.ListingList
import com.example.getfast.viewmodel.ListingViewModel
import com.example.getfast.R

class MainActivity : ComponentActivity() {

    private val viewModel: ListingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notifier = Notifier(this)
        setContent {
            MaterialTheme {
                val listings by viewModel.listings.collectAsState()
                val lastFetch by viewModel.lastFetchTime.collectAsState()
                val favorites by viewModel.favorites.collectAsState()
                var showFavoritesOnly by remember { mutableStateOf(false) }
                val seenIds = remember { mutableSetOf<String>() }
                LaunchedEffect(listings) {
                    val newItems = listings.filter { it.id !in seenIds }
                    newItems.forEach { notifier.notifyNewListing(it.title) }
                    seenIds.addAll(newItems.map { it.id })
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.last_fetch,
                                lastFetch ?: "--"
                            ),
                            style = MaterialTheme.typography.bodySmall
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
                    ) {
                        FilterChip(
                            selected = !showFavoritesOnly,
                            onClick = { showFavoritesOnly = false },
                            label = { Text(text = stringResource(id = R.string.all_tab)) },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        FilterChip(
                            selected = showFavoritesOnly,
                            onClick = { showFavoritesOnly = true },
                            label = { Text(text = stringResource(id = R.string.favorites_tab)) },
                        )
                    }
                    ListingList(
                        listings = listings,
                        favorites = favorites,
                        favoritesOnly = showFavoritesOnly,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(id = R.string.copyright),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        viewModel.refreshListings()
    }
}

