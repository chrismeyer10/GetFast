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
 *
 * Viele Immobilienportale blockieren direkte Zugriffe von Skripten und
 * liefern HTTP-Fehler oder eine leere Antwort zurück. Um dennoch Inhalte
 * laden zu können wird beim Auftreten eines Fehlers ein zweiter Versuch
 * über den von "r.jina.ai" bereitgestellten Proxy unternommen. Dieser Proxy
 * spiegelt öffentlich zugängliche Seiten und stellt eine einfache Möglichkeit
 * dar, an das HTML zu gelangen ohne die ursprüngliche Domain direkt
 * ansprechen zu müssen.
 */
class JsoupHtmlFetcher : HtmlFetcher {
    override suspend fun fetch(url: String): Document = withContext(Dispatchers.IO) {
        runCatching { connect(url).get() }
            .recoverCatching {
                val proxied = "https://r.jina.ai/$url"
                connect(proxied).get()
            }
            .getOrThrow()
    }

    private fun connect(url: String) =
        Jsoup.connect(url)
            .userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, wie Gecko) " +
                    "Chrome/120.0.0.0 Safari/537.36"
            )
            .referrer("https://www.google.com")
            .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
            .timeout(10000)
}

