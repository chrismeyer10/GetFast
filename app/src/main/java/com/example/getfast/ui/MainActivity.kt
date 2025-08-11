package com.example.getfast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.getfast.databinding.ActivityMainBinding
import com.example.getfast.notifications.Notifier
import com.example.getfast.viewmodel.ListingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ListingViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var notifier: Notifier
    private lateinit var adapter: ListingAdapter
    private val seenIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notifier = Notifier(this)
        adapter = ListingAdapter()
        binding.listings.layoutManager = LinearLayoutManager(this)
        binding.listings.adapter = adapter

        lifecycleScope.launch {
            viewModel.listings.collectLatest { listings ->
                adapter.submitList(listings)
                val newItems = listings.filter { it.id !in seenIds }
                newItems.forEach { notifier.notifyNewListing(it.title) }
                seenIds.addAll(newItems.map { it.id })
            }
        }

        viewModel.refreshListings()
    }
}
