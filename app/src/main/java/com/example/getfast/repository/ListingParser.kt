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
     * Parst ein Immoscout-Dokument in Listings.
     */
    fun parseImmoscout(document: Document): List<Listing> {
        return document.select("li.result-list__listing[data-obid]").mapNotNull { element ->
            val id = element.attr("data-obid")
            val link = element.selectFirst("a.result-list-entry__brand-title-container")
            val href = link?.attr("href")
            val title = link?.text()?.trim()
            if (id.isBlank() || href == null || title.isNullOrBlank()) {
                return@mapNotNull null
            }
            val price = element.selectFirst(".result-list-entry__primary-criterion dd")?.text()?.trim() ?: ""
            val address = element.selectFirst(".result-list-entry__address")?.text()?.trim() ?: ""
            val parts = address.split(",")
            val district = parts.getOrNull(0)?.trim() ?: ""
            val city = parts.getOrNull(1)?.trim() ?: ""
            val summary = element.selectFirst(".result-list-entry__description")?.text()?.trim() ?: ""
            val image = element.selectFirst("img[data-src]")?.attr("data-src")
                ?: element.selectFirst("img[src]")?.attr("src")
            val images = if (image.isNullOrBlank()) emptyList() else listOf(image)
            Listing(
                id = id,
                title = title,
                url = "https://www.immobilienscout24.de$href",
                date = "",
                district = district,
                city = city,
                price = price,
                summary = summary,
                imageUrls = images,
                isSearch = false,
            )
        }
    }

    /**
     * Parst ein Immonet-Dokument in Listings.
     */
    fun parseImmonet(document: Document): List<Listing> {
        return document.select("article.search-list-entry[data-id]").mapNotNull { element ->
            val id = element.attr("data-id")
            val link = element.selectFirst("a[href*='/angebot/']")
            val href = link?.attr("href")
            val title = link?.text()?.trim()
            if (id.isBlank() || href == null || title.isNullOrBlank()) {
                return@mapNotNull null
            }
            val price = element.selectFirst(".result-item-price, .search-list-entry__primary-criterion")?.text()?.trim() ?: ""
            val address = element.selectFirst(".result-item-address, .search-list-entry__address")?.text()?.trim() ?: ""
            val parts = address.split(",")
            val district = parts.getOrNull(0)?.trim() ?: ""
            val city = parts.getOrNull(1)?.trim() ?: ""
            val summary = element.selectFirst(".result-item-description, .search-list-entry__description")?.text()?.trim() ?: ""
            val image = element.selectFirst("img[data-src]")?.attr("data-src")
                ?: element.selectFirst("img[src]")?.attr("src")
            val images = if (image.isNullOrBlank()) emptyList() else listOf(image)
            Listing(
                id = id,
                title = title,
                url = if (href.startsWith("http")) href else "https://www.immonet.de$href",
                date = "",
                district = district,
                city = city,
                price = price,
                summary = summary,
                imageUrls = images,
                isSearch = false,
            )
        }
    }

    /**
     * Parst ein Immowelt-Dokument in Listings.
     */
    fun parseImmowelt(document: Document): List<Listing> {
        return document.select("div.EstateItem[data-id]").mapNotNull { element ->
            val id = element.attr("data-id")
            val link = element.selectFirst("a[href*='/expose/']")
            val href = link?.attr("href")
            val title = link?.text()?.trim()
            if (id.isBlank() || href == null || title.isNullOrBlank()) {
                return@mapNotNull null
            }
            val price = element.selectFirst(".EstateItem-price, .price")?.text()?.trim() ?: ""
            val address = element.selectFirst(".EstateItem-address, .address")?.text()?.trim() ?: ""
            val parts = address.split(",")
            val district = parts.getOrNull(0)?.trim() ?: ""
            val city = parts.getOrNull(1)?.trim() ?: ""
            val summary = element.selectFirst(".EstateItem-description, .description")?.text()?.trim() ?: ""
            val image = element.selectFirst("img[data-src]")?.attr("data-src")
                ?: element.selectFirst("img[src]")?.attr("src")
            val images = if (image.isNullOrBlank()) emptyList() else listOf(image)
            Listing(
                id = id,
                title = title,
                url = if (href.startsWith("http")) href else "https://www.immowelt.de$href",
                date = "",
                district = district,
                city = city,
                price = price,
                summary = summary,
                imageUrls = images,
                isSearch = false,
            )
        }
    }

    /**
     * Parst ein Wohnungsboerse-Dokument in Listings.
     */
    fun parseWohnungsboerse(document: Document): List<Listing> {
        return document.select("div.inserate-result[data-id]").mapNotNull { element ->
            val id = element.attr("data-id")
            val link = element.selectFirst("a.ad-list-item")
            val href = link?.attr("href")
            val title = link?.selectFirst("h2")?.text()?.trim()
            if (id.isBlank() || href == null || title.isNullOrBlank()) {
                return@mapNotNull null
            }
            val price = link.selectFirst(".mietpreis")?.text()?.trim() ?: ""
            val address = link.selectFirst(".adresse")?.text()?.trim() ?: ""
            val parts = address.split(",")
            val district = parts.getOrNull(0)?.trim() ?: ""
            val city = parts.getOrNull(1)?.trim() ?: ""
            val summary = link.selectFirst(".beschreibung")?.text()?.trim() ?: ""
            val image = link.selectFirst("img[data-src]")?.attr("data-src")
                ?: link.selectFirst("img[src]")?.attr("src")
            val images = if (image.isNullOrBlank()) emptyList() else listOf(image)
            Listing(
                id = id,
                title = title,
                url = "https://www.wohnungsboerse.net$href",
                date = "",
                district = district,
                city = city,
                price = price,
                summary = summary,
                imageUrls = images,
                isSearch = false,
            )
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
