package com.example.getfast.model

import java.text.Normalizer
import java.util.Locale

/**
 * Represents a selectable city. The [displayName] is shown to the user and used as
 * search term unless a provider specific override is supplied.
 */
data class City(
    val displayName: String,
    val isCustom: Boolean = false,
    private val providerOverrides: Map<ListingSource, String> = emptyMap(),
) {
    /**
     * Returns the provider specific path or query value for this city. Providers that accept
     * free text queries simply receive the trimmed [displayName], while providers that require
     * slugs get a generated representation.
     */
    fun pathFor(source: ListingSource): String {
        providerOverrides[source]?.let { return it }
        val normalized = displayName.trim()
        return when (source) {
            ListingSource.WOHNUNGSBOERSE -> normalized.toWohnungsboerseSlug()
            ListingSource.KLEINANZEIGEN -> normalized
            ListingSource.IMMOSCOUT,
            ListingSource.IMMONET,
            ListingSource.IMMOWELT -> normalized
        }
    }

    companion object {
        fun custom(name: String): City = City(name.trim(), isCustom = true)
    }
}

/**
 * Collection of popular German cities sorted by population. The order is preserved so that
 * autocomplete suggestions naturally appear by relevance.
 */
object CityCatalog {
    val germany: List<City> = listOf(
        City("Berlin"),
        City("Hamburg"),
        City("München"),
        City("Köln"),
    )

    val defaultCity: City = germany.first()

    fun findByName(name: String): City? {
        val normalized = name.trim()
        if (normalized.isEmpty()) return null
        return germany.firstOrNull { it.displayName.equals(normalized, ignoreCase = true) }
    }
}

private fun String.toWohnungsboerseSlug(): String {
    if (isBlank()) return ""
    return toNormalizedParts().joinToString("-") { part ->
        part.lowercase(Locale.GERMANY).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.GERMANY) else char.toString()
        }
    }
}

private fun String.toNormalizedParts(): List<String> {
    val base = Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace("ß", "ss")
        .replace("Ä", "Ae")
        .replace("Ö", "Oe")
        .replace("Ü", "Ue")
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
    return base
        .replace("'", " ")
        .replace("/", " ")
        .split(" ", "-", ",", ".")
        .map { part ->
            val stripped = Normalizer.normalize(part, Normalizer.Form.NFD)
                .replace("\p{Mn}".toRegex(), "")
            stripped.replace("[^A-Za-z0-9]".toRegex(), "")
        }
        .filter { it.isNotEmpty() }
}
