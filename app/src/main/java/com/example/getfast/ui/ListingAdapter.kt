package com.example.getfast.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.getfast.databinding.ItemListingBinding
import com.example.getfast.model.Listing

class ListingAdapter : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {
    private val items = mutableListOf<Listing>()

    fun submitList(listings: List<Listing>) {
        items.clear()
        items.addAll(listings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemListingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listing: Listing) {
            binding.title.text = listing.title
        }
    }
}
