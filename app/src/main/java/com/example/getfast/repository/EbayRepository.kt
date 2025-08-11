package com.example.getfast.repository

import com.example.getfast.model.Listing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class EbayRepository(
    private val searchUrl: String = "https://www.kleinanzeigen.de/s-wohnung-mieten/berlin/c203l3331"
) {
    suspend fun fetchLatestListings(): List<Listing> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .get()
            doc.select("article.aditem").mapNotNull { element ->
                parseListing(element)
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
        return Listing(
            id = id,
            title = title,
            url = "https://www.kleinanzeigen.de$href",
            date = date,
            district = district,
            city = city,
            price = price,
            summary = summary
        )
    }

    private fun generateSummary(text: String): String {
        val sentences = text.split(".").map { it.trim() }.filter { it.isNotEmpty() }
        return sentences.take(2).joinToString(". ").let {
            if (it.isNotEmpty()) "$it." else ""
        }
    }
}
