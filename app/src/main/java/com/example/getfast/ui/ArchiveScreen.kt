package com.example.getfast.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.model.Listing
import com.example.getfast.ui.components.ListingList

@Composable
fun ArchiveScreen(
    listings: List<Listing>,
    favorites: Set<String>,
    onToggleFavorite: (Listing) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
            }
            Text(
                text = stringResource(id = R.string.archive_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        ListingList(
            listings = listings,
            favorites = favorites,
            favoritesOnly = false,
            onToggleFavorite = onToggleFavorite,
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.weight(1f)
        )
    }
}

