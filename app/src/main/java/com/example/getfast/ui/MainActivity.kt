package com.example.getfast.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.getfast.model.Listing
import com.example.getfast.notifications.Notifier
import com.example.getfast.viewmodel.ListingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ListingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notifier = Notifier(this)
        setContent {
            MaterialTheme {
                val listings by viewModel.listings.collectAsState()
                val seenIds = remember { mutableSetOf<String>() }
                LaunchedEffect(listings) {
                    val newItems = listings.filter { it.id !in seenIds }
                    newItems.forEach { notifier.notifyNewListing(it.title) }
                    seenIds.addAll(newItems.map { it.id })
                }
                ListingList(listings)
            }
        }
        viewModel.refreshListings()
    }
}

@Composable
fun ListingList(listings: List<Listing>) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(listings) { listing ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.url))
                        context.startActivity(intent)
                    }
            ) {
                Text(
                    text = listing.title,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

