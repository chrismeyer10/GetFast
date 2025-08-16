package com.example.getfast.repository

import com.example.getfast.model.Listing
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Parser zur Umwandlung eines HTML-Dokuments in eine Liste von Anzeigen.
 */
class ListingParser {
    /**
     * Extrahiert alle Anzeigen aus dem Dokument.
     */
    fun parse(document: Document): List<Listing> {
        return document.select("article.aditem").mapNotNull { element ->
            parseListing(element)
        }
    }

    /**
     * Wandelt ein einzelnes HTML-Element in ein Listing-Objekt um.
     */
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
        val images = element.select("img").mapNotNull {
            it.attr("data-src").takeIf { src -> src.isNotBlank() }
                ?: it.attr("src").takeIf { src -> src.isNotBlank() }
        }.distinct()
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
            imageUrls = images,
            isSearch = isSearch,
        )
    }

    /**
     * Erzeugt eine kurze Zusammenfassung aus dem Beschreibungstext.
     */
    private fun generateSummary(text: String): String {
        val sentences = text.split(".").map { it.trim() }.filter { it.isNotEmpty() }
        return sentences.take(2).joinToString(". ").let {
            if (it.isNotEmpty()) "$it." else ""
        }
    }
}
