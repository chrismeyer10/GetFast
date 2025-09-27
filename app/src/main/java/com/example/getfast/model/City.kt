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
        City("Frankfurt am Main"),
        City("Stuttgart"),
        City("Düsseldorf"),
        City("Leipzig"),
        City("Dortmund"),
        City("Essen"),
        City("Bremen"),
        City("Dresden"),
        City("Hannover"),
        City("Nürnberg"),
        City("Duisburg"),
        City("Bochum"),
        City("Wuppertal"),
        City("Bielefeld"),
        City("Bonn"),
        City("Münster"),
        City("Karlsruhe"),
        City("Mannheim"),
        City("Augsburg"),
        City("Wiesbaden"),
        City("Gelsenkirchen"),
        City("Mönchengladbach"),
        City("Braunschweig"),
        City("Chemnitz"),
        City("Kiel"),
        City("Aachen"),
        City("Halle (Saale)"),
        City("Magdeburg"),
        City("Freiburg im Breisgau"),
        City("Krefeld"),
        City("Lübeck"),
        City("Oberhausen"),
        City("Erfurt"),
        City("Mainz"),
        City("Rostock"),
        City("Kassel"),
        City("Hagen"),
        City("Saarbrücken"),
        City("Hamm"),
        City("Potsdam"),
        City("Ludwigshafen am Rhein"),
        City("Oldenburg"),
        City("Leverkusen"),
        City("Osnabrück"),
        City("Solingen"),
        City("Heidelberg"),
        City("Herne"),
        City("Neuss"),
        City("Darmstadt"),
        City("Paderborn"),
        City("Regensburg"),
        City("Ingolstadt"),
        City("Würzburg"),
        City("Fürth"),
        City("Ulm"),
        City("Heilbronn"),
        City("Pforzheim"),
        City("Wolfsburg"),
        City("Göttingen"),
        City("Offenbach am Main"),
        City("Bottrop"),
        City("Reutlingen"),
        City("Trier"),
        City("Recklinghausen"),
        City("Bremerhaven"),
        City("Koblenz"),
        City("Bergisch Gladbach"),
        City("Jena"),
        City("Remscheid"),
        City("Erlangen"),
        City("Moers"),
        City("Siegen"),
        City("Hildesheim"),
        City("Salzgitter"),
        City("Cottbus"),
        City("Kaiserslautern"),
        City("Gütersloh"),
        City("Witten"),
        City("Hanau"),
        City("Schwerin"),
        City("Gera"),
        City("Esslingen am Neckar"),
        City("Iserlohn"),
        City("Ludwigsburg"),
        City("Marl"),
        City("Düren"),
        City("Tübingen"),
        City("Villingen-Schwenningen"),
        City("Flensburg"),
        City("Gießen"),
        City("Dessau-Roßlau"),
        City("Ratingen"),
        City("Lünen"),
        City("Wilhelmshaven"),
        City("Minden"),
        City("Neumünster"),
        City("Dormagen"),
        City("Bayreuth"),
        City("Landshut"),
        City("Aschaffenburg"),
        City("Kempten (Allgäu)"),
        City("Lüneburg"),
        City("Bamberg"),
        City("Aalen"),
        City("Langenfeld (Rheinland)"),
        City("Fulda"),
        City("Neubrandenburg"),
        City("Konstanz"),
        City("Rheine"),
        City("Grevenbroich"),
        City("Weimar"),
        City("Herten"),
        City("Frankfurt (Oder)"),
        City("Bergkamen"),
        City("Speyer"),
        City("Coburg"),
        City("Ravensburg"),
        City("Bocholt"),
        City("Delmenhorst"),
        City("Rosenheim"),
        City("Erftstadt"),
        City("Wesel"),
        City("Garbsen"),
        City("Hürth"),
        City("Hilden"),
        City("Gladbeck"),
        City("Sindelfingen"),
        City("Norderstedt"),
        City("Troisdorf"),
        City("Friedrichshafen"),
        City("Passau"),
        City("Görlitz"),
        City("Gummersbach"),
        City("Cloppenburg"),
        City("Bad Homburg vor der Höhe"),
        City("Celle"),
        City("Velbert"),
        City("Herford"),
        City("Lippstadt"),
        City("Baden-Baden"),
        City("Wolfenbüttel"),
        City("Nordhorn"),
        City("Kleve"),
        City("Bünde"),
        City("Neustadt an der Weinstraße"),
        City("Dinslaken"),
        City("Heidenheim an der Brenz"),
        City("Langenhagen"),
        City("Bad Salzuflen"),
        City("Euskirchen"),
        City("Herzogenrath"),
        City("Sankt Augustin"),
        City("Rheda-Wiedenbrück"),
        City("Unna"),
        City("Schweinfurt"),
        City("Stralsund"),
        City("Hameln"),
        City("Straubing"),
        City("Neunkirchen"),
        City("Goslar"),
        City("Waiblingen"),
        City("Amberg"),
        City("Freising"),
        City("Hof"),
        City("Böblingen"),
        City("Lörrach"),
        City("Offenburg"),
        City("Schwabach"),
        City("Aurich"),
        City("Ansbach"),
        City("Plauen"),
        City("Zwickau"),
        City("Singen (Hohentwiel)"),
        City("Emden"),
        City("Greifswald"),
        City("Cuxhaven"),
        City("Marburg"),
        City("Tönisvorst"),
        City("Vechta"),
        City("Lemgo"),
        City("Rastatt"),
        City("Papenburg"),
        City("Völklingen"),
        City("Lindau (Bodensee)"),
        City("Stade"),
        City("Homburg"),
        City("Bitterfeld-Wolfen"),
        City("Freital"),
        City("Gotha"),
        City("Halberstadt"),
        City("Rottweil"),
        City("Weiden in der Oberpfalz"),
        City("Deggendorf"),
        City("Lingen (Ems)"),
        City("Ahaus"),
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
