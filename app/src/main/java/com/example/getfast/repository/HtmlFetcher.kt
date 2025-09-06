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
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .referrer("https://www.google.com")
            .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
            .timeout(10000)
            .get()
    }
}
