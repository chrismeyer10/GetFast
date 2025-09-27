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

/**
 * Parser für ImmoScout24-Dokumente.
 */
class ImmoscoutListingParser : ProviderListingParser {
    /**
     * Liest alle Ergebnis-Elemente und erzeugt Listings.
     */
    override fun parseListingsFromDocument(document: Document): List<Listing> {
        return document.select("li.result-list__listing[data-obid]").mapNotNull { element ->
            createListingFromResultElement(element)
        }
    }

    /**
     * Wandelt ein Ergebnis-Element in ein Listing-Objekt um.
     */
    private fun createListingFromResultElement(element: Element): Listing? {
        val id = element.attr("data-obid")
        val linkElement = element.selectFirst("a.result-list-entry__brand-title-container")
        val href = linkElement?.attr("href")
        val title = linkElement?.text()?.trim()
        if (id.isBlank() || href == null || title.isNullOrBlank()) {
            return null
        }
        val price = element.selectFirst(".result-list-entry__primary-criterion dd")?.text()?.trim() ?: ""
        val address = element.selectFirst(".result-list-entry__address")?.text()?.trim() ?: ""
        val (district, city) = extractDistrictAndCity(address)
        val summary = element.selectFirst(".result-list-entry__description")?.text()?.trim() ?: ""
        val images = extractImageUrls(element)
        return Listing(
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

    /**
     * Extrahiert Bezirk und Stadt aus einer Adresszeile.
     */
    private fun extractDistrictAndCity(address: String): Pair<String, String> {
        val parts = address.split(",")
        val district = parts.getOrNull(0)?.trim() ?: ""
        val city = parts.getOrNull(1)?.trim() ?: ""
        return district to city
    }

    /**
     * Ermittelt die erste gefundene Bild-URL.
     */
    private fun extractImageUrls(element: Element): List<String> {
        val image = element.selectFirst("img[data-src]")?.attr("data-src")
            ?: element.selectFirst("img[src]")?.attr("src")
        return if (image.isNullOrBlank()) emptyList() else listOf(image)
    }
}

/**
 * Parser für Immonet-Dokumente.
 */
class ImmonetListingParser : ProviderListingParser {
    /**
     * Liest alle Ergebnis-Elemente und erzeugt Listings.
     */
    override fun parseListingsFromDocument(document: Document): List<Listing> {
        return document.select("article.search-list-entry[data-id]").mapNotNull { element ->
            createListingFromSearchEntry(element)
        }
    }

    /**
     * Wandelt ein Immonet-Ergebnis in ein Listing-Objekt um.
     */
    private fun createListingFromSearchEntry(element: Element): Listing? {
        val id = element.attr("data-id")
        val linkElement = element.selectFirst("a[href*='/angebot/']")
        val href = linkElement?.attr("href")
        val title = linkElement?.text()?.trim()
        if (id.isBlank() || href == null || title.isNullOrBlank()) {
            return null
        }
        val price = element.selectFirst(".result-item-price, .search-list-entry__primary-criterion")?.text()?.trim() ?: ""
        val address = element.selectFirst(".result-item-address, .search-list-entry__address")?.text()?.trim() ?: ""
        val (district, city) = extractDistrictAndCity(address)
        val summary = element.selectFirst(".result-item-description, .search-list-entry__description")?.text()?.trim() ?: ""
        val images = extractImageUrls(element)
        return Listing(
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

    /**
     * Extrahiert Bezirk und Stadt aus einer Adresszeile.
     */
    private fun extractDistrictAndCity(address: String): Pair<String, String> {
        val parts = address.split(",")
        val district = parts.getOrNull(0)?.trim() ?: ""
        val city = parts.getOrNull(1)?.trim() ?: ""
        return district to city
    }

    /**
     * Liest die relevanteste Bild-URL aus dem Element.
     */
    private fun extractImageUrls(element: Element): List<String> {
        val image = element.selectFirst("img[data-src]")?.attr("data-src")
            ?: element.selectFirst("img[src]")?.attr("src")
        return if (image.isNullOrBlank()) emptyList() else listOf(image)
    }
}

/**
 * Parser für Immowelt-Dokumente.
 */
class ImmoweltListingParser : ProviderListingParser {
    /**
     * Liest alle Ergebnis-Elemente und erzeugt Listings.
     */
    override fun parseListingsFromDocument(document: Document): List<Listing> {
        return document.select("div.EstateItem[data-id]").mapNotNull { element ->
            createListingFromEstateItem(element)
        }
    }

    /**
     * Wandelt ein Immowelt-Element in ein Listing um.
     */
    private fun createListingFromEstateItem(element: Element): Listing? {
        val id = element.attr("data-id")
        val linkElement = element.selectFirst("a[href*='/expose/']")
        val href = linkElement?.attr("href")
        val title = linkElement?.text()?.trim()
        if (id.isBlank() || href == null || title.isNullOrBlank()) {
            return null
        }
        val price = element.selectFirst(".EstateItem-price, .price")?.text()?.trim() ?: ""
        val address = element.selectFirst(".EstateItem-address, .address")?.text()?.trim() ?: ""
        val (district, city) = extractDistrictAndCity(address)
        val summary = element.selectFirst(".EstateItem-description, .description")?.text()?.trim() ?: ""
        val images = extractImageUrls(element)
        return Listing(
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

    /**
     * Extrahiert Bezirk und Stadt aus einer Adresszeile.
     */
    private fun extractDistrictAndCity(address: String): Pair<String, String> {
        val parts = address.split(",")
        val district = parts.getOrNull(0)?.trim() ?: ""
        val city = parts.getOrNull(1)?.trim() ?: ""
        return district to city
    }

    /**
     * Liest die relevanteste Bild-URL aus dem Element.
     */
    private fun extractImageUrls(element: Element): List<String> {
        val image = element.selectFirst("img[data-src]")?.attr("data-src")
            ?: element.selectFirst("img[src]")?.attr("src")
        return if (image.isNullOrBlank()) emptyList() else listOf(image)
    }
}

/**
 * Parser für Wohnungsbörse-Dokumente.
 */
class WohnungsboerseListingParser : ProviderListingParser {
    /**
     * Liest alle Ergebnis-Elemente und erzeugt Listings.
     */
    override fun parseListingsFromDocument(document: Document): List<Listing> {
        return document.select("div.inserate-result[data-id]").mapNotNull { element ->
            createListingFromInserateResult(element)
        }
    }

    /**
     * Wandelt ein Inserats-Element in ein Listing um.
     */
    private fun createListingFromInserateResult(element: Element): Listing? {
        val id = element.attr("data-id")
        val linkElement = element.selectFirst("a.ad-list-item")
        val href = linkElement?.attr("href")
        val title = linkElement?.selectFirst("h2")?.text()?.trim()
        if (id.isBlank() || href == null || title.isNullOrBlank()) {
            return null
        }
        val price = linkElement.selectFirst(".mietpreis")?.text()?.trim() ?: ""
        val address = linkElement.selectFirst(".adresse")?.text()?.trim() ?: ""
        val (district, city) = extractDistrictAndCity(address)
        val summary = linkElement.selectFirst(".beschreibung")?.text()?.trim() ?: ""
        val images = extractImageUrls(linkElement)
        return Listing(
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

    /**
     * Extrahiert Bezirk und Stadt aus einer Adresszeile.
     */
    private fun extractDistrictAndCity(address: String): Pair<String, String> {
        val parts = address.split(",")
        val district = parts.getOrNull(0)?.trim() ?: ""
        val city = parts.getOrNull(1)?.trim() ?: ""
        return district to city
    }

    /**
     * Liest alle Bild-URLs aus dem Element.
     */
    private fun extractImageUrls(element: Element): List<String> {
        val image = element.selectFirst("img[data-src]")?.attr("data-src")
            ?: element.selectFirst("img[src]")?.attr("src")
        return if (image.isNullOrBlank()) emptyList() else listOf(image)
    }
}
