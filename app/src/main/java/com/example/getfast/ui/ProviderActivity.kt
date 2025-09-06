package com.example.getfast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.model.ListingSource
import com.example.getfast.ui.components.ListingList
import com.example.getfast.ui.theme.GetFastTheme
import com.example.getfast.viewmodel.ProviderViewModel

/**
 * Basisklasse für Aktivitäten, die Listings eines einzelnen Anbieters anzeigen.
 */
abstract class ProviderActivity : ComponentActivity() {

    /** Der Anbieter, für den diese Activity zuständig ist. */
    protected abstract val source: ListingSource

    private val viewModel: ProviderViewModel by viewModels {
        ProviderViewModel.factory(application, source)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            GetFastTheme(darkTheme = systemDark) {
                val listings by viewModel.listings.collectAsState()
                val lastFetch by viewModel.lastFetchTime.collectAsState()
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val filter by viewModel.filter.collectAsState()
                var showSettings by remember { mutableStateOf(false) }
                if (showSettings) {
                    ProviderSettingsScreen(
                        filter = filter,
                        onApply = {
                            viewModel.updateFilter(it)
                            showSettings = false
                        },
                        onBack = { showSettings = false },
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.refreshListings() }) {
                                Text(text = stringResource(id = R.string.refresh))
                            }
                        }
                        ListingList(
                            listings = listings,
                            favorites = emptySet(),
                            favoritesOnly = false,
                            onToggleFavorite = {},
                            highlightedIds = emptySet(),
                            blinkingIds = emptySet(),
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshListings() },
                            onArchive = {},
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        viewModel.refreshListings()
    }
}

/** Aktivität für ImmoScout24. */
class ImmoscoutActivity : ProviderActivity() {
    override val source: ListingSource = ListingSource.IMMOSCOUT
}

/** Aktivität für Kleinanzeigen. */
class KleinanzeigenActivity : ProviderActivity() {
    override val source: ListingSource = ListingSource.KLEINANZEIGEN
}

/** Aktivität für Immonet. */
class ImmonetActivity : ProviderActivity() {
    override val source: ListingSource = ListingSource.IMMONET
}

/** Aktivität für Immowelt. */
class ImmoweltActivity : ProviderActivity() {
    override val source: ListingSource = ListingSource.IMMOWELT
}

/** Aktivität für Wohnungsboerse. */
class WohnungsboerseActivity : ProviderActivity() {
    override val source: ListingSource = ListingSource.WOHNUNGSBOERSE
}

