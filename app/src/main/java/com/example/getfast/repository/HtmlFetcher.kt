package com.example.getfast.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Schnittstelle zum Laden von HTML-Dokumenten.
 */
interface HtmlFetcher {
    /**
     * Lädt ein HTML-Dokument von der angegebenen URL.
     */
    suspend fun fetch(url: String): Document
}

/**
 * Standardimplementierung, die Jsoup zum Abrufen des HTML verwendet.
 */
class JsoupHtmlFetcher : HtmlFetcher {
    override suspend fun fetch(url: String): Document = withContext(Dispatchers.IO) {
        Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .get()
    }
}
