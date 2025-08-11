package com.example.getfast.repository

import com.example.getfast.model.Listing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class EbayRepository(
    private val searchUrl: String = "https://www.kleinanzeigen.de/s-wohnung-mieten/berlin/c203l3331"
) {
    suspend fun fetchLatestListings(): List<Listing> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .get()
            doc.select("article.aditem").mapNotNull { element ->
                val id = element.attr("data-adid")
                val link = element.selectFirst("a[href].ellipsis")
                val href = link?.attr("href")
                val title = link?.text()
                if (id.isNotEmpty() && href != null && title != null) {
                    Listing(id, title, "https://www.kleinanzeigen.de$href")
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
