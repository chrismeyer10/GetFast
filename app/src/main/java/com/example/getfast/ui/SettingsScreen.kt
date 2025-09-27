package com.example.getfast.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.model.City
import com.example.getfast.model.CityCatalog
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter

/**
 * SettingsScreenState beschreibt den kompletten UI-Zustand der Einstellungsseite.
 * Durch die gebündelte Klasse lässt sich gut nachvollziehen, welche Werte aktuell angezeigt werden.
 */
data class SettingsScreenState(
    val selectedCity: City,
    val cityQuery: String,
    val priceText: String,
    val daysText: String,
    val selectedSources: Set<ListingSource>,
    val allCities: List<City>,
) {
    /**
     * Liefert die Städte, die zum aktuellen Suchtext passen, damit das Dropdown nicht unnötig filtert.
     */
    val filteredCities: List<City>
        get() {
            val trimmedQuery = cityQuery.trim()
            return if (trimmedQuery.isEmpty()) {
                allCities
            } else {
                allCities.filter { city ->
                    city.displayName.contains(trimmedQuery, ignoreCase = true)
                }
            }
        }

    /**
     * Hilfsfunktion zum Aktualisieren der ausgewählten Stadt, wodurch zugleich die Suchanzeige angepasst wird.
     */
    fun updateSelectedCityAndQuery(newCity: City): SettingsScreenState = copy(
        selectedCity = newCity,
        cityQuery = newCity.displayName,
    )

    /**
     * Hilfsfunktion zur Aktualisierung der Suchanfrage und der intern ausgewählten Stadt.
     */
    fun updateCityQuery(newQuery: String): SettingsScreenState {
        val normalized = newQuery.trim()
        val exactMatch = allCities.firstOrNull { city ->
            city.displayName.equals(normalized, ignoreCase = true)
        }
        val updatedCity = exactMatch ?: selectedCity
        return copy(cityQuery = newQuery, selectedCity = updatedCity)
    }

    /**
     * Hilfsfunktion, mit der wir die Texteingabe für den Preis säubern und speichern.
     */
    fun updatePriceText(newText: String): SettingsScreenState = copy(
        priceText = newText.filter { character -> character.isDigit() },
    )

    /**
     * Hilfsfunktion, mit der wir die Tageingabe begrenzen und speichern.
     */
    fun updateDaysText(newText: String): SettingsScreenState = copy(
        daysText = newText.filter { character -> character.isDigit() }.take(1),
    )

    /**
     * Hilfsfunktion, um eine Quelle entweder hinzuzufügen oder zu entfernen.
     */
    fun toggleSource(source: ListingSource): SettingsScreenState = copy(
        selectedSources = if (source in selectedSources) {
            selectedSources - source
        } else {
            selectedSources + source
        },
    )

    /**
     * Hilfsfunktion, die aus dem aktuellen Zustand das SearchFilter-Modell erzeugt.
     */
    fun toSearchFilter(): SearchFilter = SearchFilter(
        city = selectedCity,
        maxPrice = priceText.toIntOrNull(),
        maxAgeDays = daysText.toIntOrNull()?.coerceIn(0, 3) ?: 3,
        sources = selectedSources,
    )
}

/**
 * Erzeugt den Startzustand für den Screen basierend auf dem übergebenen Filter.
 */
fun createInitialSettingsScreenState(filter: SearchFilter): SettingsScreenState {
    val knownCity = CityCatalog.findByName(filter.city.displayName)
    val selectedCity = knownCity ?: filter.city
    return SettingsScreenState(
        selectedCity = selectedCity,
        cityQuery = selectedCity.displayName,
        priceText = filter.maxPrice?.toString() ?: "",
        daysText = filter.maxAgeDays.toString(),
        selectedSources = filter.sources,
        allCities = CityCatalog.germany,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    filter: SearchFilter,
    onApply: (SearchFilter) -> Unit,
    onBack: () -> Unit,
    onOpenArchive: () -> Unit,
    onReset: () -> Unit,
) {
    // Wir intercepten den Back-Button, damit die Navigation funktioniert.
    BackHandler(onBack = onBack)

    // Wir halten den kompletten Zustand in einem State-Objekt, damit alle Unterkomponenten ihn nutzen können.
    var uiState by remember(filter) { mutableStateOf(createInitialSettingsScreenState(filter)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Kopfbereich mit Back-Button und Titel.
        SettingsHeaderSection(onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // Auswahlbereich für die Stadt inklusive Dropdown.
        CitySelectorSection(
            state = uiState,
            onQueryChange = { newQuery ->
                uiState = uiState.updateCityQuery(newQuery)
            },
            onExpandedCityChosen = { chosenCity ->
                uiState = uiState.updateSelectedCityAndQuery(chosenCity)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Eingabefeld für den Preis.
        PriceInputSection(
            priceText = uiState.priceText,
            onPriceChange = { newPrice -> uiState = uiState.updatePriceText(newPrice) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Eingabefeld für die maximale Anzahl Tage.
        DaysInputSection(
            daysText = uiState.daysText,
            onDaysChange = { newDays -> uiState = uiState.updateDaysText(newDays) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quellenbereich mit Chips für jeden Anbieter.
        SourceSelectionSection(
            selectedSources = uiState.selectedSources,
            onSourceToggle = { source -> uiState = uiState.toggleSource(source) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Aktionsbereich mit Buttons.
        SettingsActionButtons(
            onApply = { onApply(uiState.toSearchFilter()) },
            onOpenArchive = onOpenArchive,
            onReset = onReset
        )
    }
}

/**
 * Stellt den Kopfbereich mit Back-Button und Seitentitel dar.
 */
@Composable
private fun SettingsHeaderSection(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Back-Button für die Navigation.
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
        }
        // Titeltext der Seite.
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/**
 * Zeigt das Dropdown zur Stadtauswahl inklusive Suchfeld.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySelectorSection(
    state: SettingsScreenState,
    onQueryChange: (String) -> Unit,
    onExpandedCityChosen: (City) -> Unit,
) {
    // Lokaler State, ob das Dropdown geöffnet ist.
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        // Texteingabe für den Suchbegriff.
        OutlinedTextField(
            value = state.cityQuery,
            onValueChange = { input ->
                onQueryChange(input)
                expanded = true
            },
            label = { Text(text = stringResource(id = R.string.city_label)) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (state.filteredCities.isNotEmpty()) {
                state.filteredCities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city.displayName) },
                        onClick = {
                            onExpandedCityChosen(city)
                            expanded = false
                        }
                    )
                }
            }

            if (state.filteredCities.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.no_city_results)) },
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}

/**
 * Zeigt das Feld für die maximale Miete in Euro.
 */
@Composable
private fun PriceInputSection(
    priceText: String,
    onPriceChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = priceText,
        onValueChange = { newValue -> onPriceChange(newValue) },
        label = { Text(text = stringResource(id = R.string.max_price_label)) },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Zeigt das Feld für die maximale Anzahl an Tagen.
 */
@Composable
private fun DaysInputSection(
    daysText: String,
    onDaysChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = daysText,
        onValueChange = { newValue -> onDaysChange(newValue) },
        label = { Text(text = stringResource(id = R.string.max_days_label)) },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Zeigt alle Anbieter in Form von FilterChips.
 */
@Composable
private fun SourceSelectionSection(
    selectedSources: Set<ListingSource>,
    onSourceToggle: (ListingSource) -> Unit,
) {
    val chunkedSources = ListingSource.values().toList().chunked(3)
    chunkedSources.forEachIndexed { rowIndex, rowSources ->
        Row(modifier = Modifier.fillMaxWidth()) {
            rowSources.forEachIndexed { chipIndex, source ->
                // Jeder Chip repräsentiert eine Quelle.
                FilterChip(
                    selected = source in selectedSources,
                    onClick = { onSourceToggle(source) },
                    label = { Text(text = stringResource(id = source.toLabelRes())) }
                )
                if (chipIndex < rowSources.lastIndex) {
                    // Horizontaler Abstand zwischen den Chips.
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        if (rowIndex < chunkedSources.lastIndex) {
            // Vertikaler Abstand zwischen den Reihen.
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Zeigt die drei Buttons zum Anwenden, Öffnen des Archivs und Zurücksetzen.
 */
@Composable
private fun SettingsActionButtons(
    onApply: () -> Unit,
    onOpenArchive: () -> Unit,
    onReset: () -> Unit,
) {
    Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
        // Beschriftung für den Anwenden-Button.
        Text(text = stringResource(id = R.string.apply_filters))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onOpenArchive, modifier = Modifier.fillMaxWidth()) {
        // Beschriftung für das Archiv.
        Text(text = stringResource(id = R.string.open_archive))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
        // Beschriftung für den Reset.
        Text(text = stringResource(id = R.string.reset_app))
    }
}

/**
 * Wandelt eine Quelle in die passende Ressourcen-ID um.
 */
private fun ListingSource.toLabelRes(): Int = when (this) {
    ListingSource.KLEINANZEIGEN -> R.string.source_kleinanzeigen
    ListingSource.IMMOSCOUT -> R.string.source_immoscout
    ListingSource.IMMONET -> R.string.source_immonet
    ListingSource.IMMOWELT -> R.string.source_immowelt
    ListingSource.WOHNUNGSBOERSE -> R.string.source_wohnungsboerse
}

// region Previews

/**
 * Vorschau für den kompletten Screen, um das Zusammenspiel zu testen.
 */
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        filter = SearchFilter(),
        onApply = {},
        onBack = {},
        onOpenArchive = {},
        onReset = {},
    )
}

/**
 * Vorschau für den Header.
 */
@Preview(showBackground = true)
@Composable
private fun SettingsHeaderSectionPreview() {
    SettingsHeaderSection(onBack = {})
}

/**
 * Vorschau für die Stadtauswahl.
 */
@Preview(showBackground = true)
@Composable
private fun CitySelectorSectionPreview() {
    CitySelectorSection(
        state = createInitialSettingsScreenState(SearchFilter()),
        onQueryChange = {},
        onExpandedCityChosen = {}
    )
}

/**
 * Vorschau für die Preiseingabe.
 */
@Preview(showBackground = true)
@Composable
private fun PriceInputSectionPreview() {
    PriceInputSection(priceText = "1500", onPriceChange = {})
}

/**
 * Vorschau für die Tageeingabe.
 */
@Preview(showBackground = true)
@Composable
private fun DaysInputSectionPreview() {
    DaysInputSection(daysText = "3", onDaysChange = {})
}

/**
 * Vorschau für die Quellen.
 */
@Preview(showBackground = true)
@Composable
private fun SourceSelectionSectionPreview() {
    SourceSelectionSection(selectedSources = setOf(ListingSource.IMMOSCOUT, ListingSource.IMMONET)) {}
}

/**
 * Vorschau für die Aktions-Buttons.
 */
@Preview(showBackground = true)
@Composable
private fun SettingsActionButtonsPreview() {
    SettingsActionButtons(onApply = {}, onOpenArchive = {}, onReset = {})
}

// endregion
