package com.example.getfast.model

import java.text.Normalizer
import java.util.Locale

/**
 * Represents a selectable city. The [displayName] is shown to the user and used as
 * search term.
 */
data class City(
    val displayName: String,
    val isCustom: Boolean = false,
) {
    private val normalizedName: String = normalize(displayName)

    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return false
        return normalizedName.contains(normalizedQuery)
    }

    fun matchesName(name: String): Boolean {
        val normalized = normalize(name)
        if (normalized.isEmpty()) return false
        return normalizedName == normalized
    }

    /**
     * Returns the trimmed city name suitable for use in search queries.
     */
    fun asQuery(): String = displayName.trim()

    companion object {
        private val DIACRITICS_REGEX = "\\p{InCombiningDiacriticalMarks}+".toRegex()

        internal fun normalize(value: String): String {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return ""
            val normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
            val withoutDiacritics = DIACRITICS_REGEX.replace(normalized, "")
            return withoutDiacritics
                .replace("ß", "ss")
                .lowercase(Locale.GERMAN)
        }

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
        if (name.isBlank()) return null
        return germany.firstOrNull { it.matchesName(name) }
    }
}
