package com.example.getfast.repository

import com.example.getfast.model.Listing
import com.example.getfast.model.SearchFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class EbayRepository {
    suspend fun fetchLatestListings(filter: SearchFilter): List<Listing> = withContext(Dispatchers.IO) {
        val url = "https://www.kleinanzeigen.de/s-wohnung-mieten/${filter.city.urlPath}"
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get()
            val listings = doc.select("article.aditem").mapNotNull { element ->
                parseListing(element)
            }
            val maxPrice = filter.maxPrice
            if (maxPrice != null) {
                listings.filter { listing ->
                    val numeric = listing.price.replace("\\D".toRegex(), "")
                    val priceValue = numeric.toIntOrNull()
                    priceValue != null && priceValue <= maxPrice
                }
            } else {
                listings
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseListing(element: Element): Listing? {
        val id = element.attr("data-adid")
        val link = element.selectFirst("a[href].ellipsis")
        val href = link?.attr("href")
        val title = link?.text()
        if (id.isEmpty() || href == null || title == null) {
            return null
        }
        val date = element.selectFirst(".aditem-main--top--right")?.text()?.trim() ?: ""
        val locationText = element.selectFirst(".aditem-main--top--left")?.text()?.trim() ?: ""
        val parts = locationText.split(",")
        val district = parts.getOrNull(0)?.trim() ?: ""
        val city = parts.getOrNull(1)?.trim() ?: ""
        val price = element.selectFirst(".aditem-main--middle--price-shipping")?.text()?.trim() ?: ""
        val description = element.selectFirst(".aditem-main--middle--description")?.text()?.trim() ?: ""
        val summary = generateSummary(description)
        val isSearch = listOf(title, description).any {
            it.contains("suche", ignoreCase = true) || it.contains("gesuch", ignoreCase = true)
        }
        return Listing(
            id = id,
            title = title,
            url = "https://www.kleinanzeigen.de$href",
            date = date,
            district = district,
            city = city,
            price = price,
            summary = summary,
            isSearch = isSearch,
        )
    }

    private fun generateSummary(text: String): String {
        val sentences = text.split(".").map { it.trim() }.filter { it.isNotEmpty() }
        return sentences.take(2).joinToString(". ").let {
            if (it.isNotEmpty()) "$it." else ""
        }
    }
}
