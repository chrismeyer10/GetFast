package com.example.getfast.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.model.Listing

data class ListingDisplayOptions(
    val showDetails: Boolean,
    val showSummary: Boolean
)

@Composable
fun ListingOptionsControls(
    showDetails: Boolean,
    onShowDetailsChange: (Boolean) -> Unit,
    showSummary: Boolean,
    onShowSummaryChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = showDetails, onCheckedChange = onShowDetailsChange)
            Text(
                text = stringResource(id = R.string.show_details),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = showSummary, onCheckedChange = onShowSummaryChange)
            Text(
                text = stringResource(id = R.string.show_summary),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun ListingList(
    listings: List<Listing>,
    options: ListingDisplayOptions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        items(listings) { listing ->
            ListingCard(listing, options) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.url))
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    options: ListingDisplayOptions,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = listing.title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (options.showDetails) {
                Text(
                    text = "${listing.date} • ${listing.district}, ${listing.city}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = listing.price,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (options.showSummary) {
                Text(
                    text = listing.summary,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

