package com.example.getfast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.getfast.notifications.Notifier
import com.example.getfast.ui.components.ListingDisplayOptions
import com.example.getfast.ui.components.ListingList
import com.example.getfast.ui.components.ListingOptionsControls
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
                var showDetails by remember { mutableStateOf(true) }
                var showSummary by remember { mutableStateOf(true) }
                val options = ListingDisplayOptions(showDetails, showSummary)
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
                    ListingOptionsControls(
                        showDetails = showDetails,
                        onShowDetailsChange = { showDetails = it },
                        showSummary = showSummary,
                        onShowSummaryChange = { showSummary = it }
                    )
                    ListingList(
                        listings = listings,
                        options = options,
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

