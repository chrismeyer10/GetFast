package com.example.getfast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.getfast.databinding.ActivityMainBinding
import com.example.getfast.notifications.Notifier
import com.example.getfast.viewmodel.ListingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ListingViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var notifier: Notifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notifier = Notifier(this)

        lifecycleScope.launch {
            viewModel.listings.collectLatest { listings ->
                if (listings.isNotEmpty()) {
                    val first = listings.first()
                    binding.message.text = first.title
                    notifier.notifyNewListing(first.title)
                }
            }
        }

        viewModel.refreshListings()
    }
}
