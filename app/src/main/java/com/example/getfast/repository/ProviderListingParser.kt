package com.example.getfast.repository

import com.example.getfast.model.Listing
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Allgemeine Schnittstelle für Parser, die aus einem Dokument Listings extrahieren.
 */
interface ProviderListingParser {
    /**
     * Parst ein HTML-Dokument in eine Liste von Listings des jeweiligen Anbieters.
     */
    fun parseListingsFromDocument(document: Document): List<Listing>
}

/**
 * Parser für eBay Kleinanzeigen, der die relevanten Informationen extrahiert.
 */
class KleinanzeigenListingParser : ProviderListingParser {
    /**
     * Liest alle Artikel-Elemente und wandelt sie in Listings um.
     */
    override fun parseListingsFromDocument(document: Document): List<Listing> {
        return document.select("article.aditem").mapNotNull { element ->
            parseSingleKleinanzeigenListing(element)
        }
    }

    /**
     * Erzeugt ein Listing aus einem einzelnen HTML-Element der Kleinanzeigen.
     */
    private fun parseSingleKleinanzeigenListing(element: Element): Listing? {
        val id = element.attr("data-adid")
        val linkElement = element.selectFirst("a[href].ellipsis")
        val href = linkElement?.attr("href")
        val title = linkElement?.text()
        if (id.isEmpty() || href == null || title == null) {
            return null
        }
        val date = element.selectFirst(".aditem-main--top--right")?.text()?.trim() ?: ""
        val locationText = element.selectFirst(".aditem-main--top--left")?.text()?.trim() ?: ""
        val (district, city) = extractDistrictAndCity(locationText)
        val price = element.selectFirst(".aditem-main--middle--price-shipping")?.text()?.trim() ?: ""
        val description = element.selectFirst(".aditem-main--middle--description")?.text()?.trim() ?: ""
        val summary = generateSummaryFromDescription(description)
        val images = extractImageUrls(element)
        val isSearch = listOfNotNull(title, description).any {
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
     * Zerlegt einen Standorttext in Bezirk und Stadt.
     */
    private fun extractDistrictAndCity(locationText: String): Pair<String, String> {
        val parts = locationText.split(",")
        val district = parts.getOrNull(0)?.trim() ?: ""
        val city = parts.getOrNull(1)?.trim() ?: ""
        return district to city
    }

    /**
     * Sammelt verfügbare Bild-URLs des Eintrags.
     */
    private fun extractImageUrls(element: Element): List<String> {
        return element.select("img").mapNotNull { image ->
            image.attr("data-src").takeIf { it.isNotBlank() }
                ?: image.attr("src").takeIf { it.isNotBlank() }
        }.distinct()
    }

    /**
     * Kürzt die Beschreibung auf die ersten beiden Sätze.
     */
    private fun generateSummaryFromDescription(text: String): String {
        val sentences = text.split(".").map { it.trim() }.filter { it.isNotEmpty() }
        val summary = sentences.take(2).joinToString(". ")
        return if (summary.isNotEmpty()) "$summary." else ""
    }
}
