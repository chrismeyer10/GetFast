package com.example.getfast.model

/**
 * Represents user-defined search filtering options.
 *
 * @property city City to search within. Controls the remote search URL.
 */
data class SearchFilter(
    val city: City = CityCatalog.defaultCity,
)
