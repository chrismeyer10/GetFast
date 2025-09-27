package com.example.getfast.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.getfast.model.SearchFilter

/**
 * SettingsScreenState beschreibt den UI-Zustand der Einstellungsseite.
 */
data class SettingsScreenState(
    val selectedCity: City,
    val allCities: List<City>,
) {
    /**
     * Hilfsfunktion zum Aktualisieren der ausgewählten Stadt.
     */
    fun updateSelectedCity(newCity: City): SettingsScreenState = copy(selectedCity = newCity)

    /**
     * Hilfsfunktion, die aus dem aktuellen Zustand das SearchFilter-Modell erzeugt.
     */
    fun toSearchFilter(): SearchFilter = SearchFilter(
        city = selectedCity,
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
    BackHandler(onBack = onBack)

    var uiState by remember(filter) { mutableStateOf(createInitialSettingsScreenState(filter)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SettingsHeaderSection(onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        CitySelectorSection(
            state = uiState,
            onExpandedCityChosen = { chosenCity ->
                uiState = uiState.updateSelectedCity(chosenCity)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

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
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
        }
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/**
 * Zeigt das Dropdown zur Stadtauswahl ohne manuelle Eingabe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySelectorSection(
    state: SettingsScreenState,
    onExpandedCityChosen: (City) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = state.selectedCity.displayName,
            onValueChange = {},
            label = { Text(text = stringResource(id = R.string.city_label)) },
            singleLine = true,
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.allCities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.displayName) },
                    onClick = {
                        onExpandedCityChosen(city)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Zeigt die Buttons zum Anwenden, Öffnen des Archivs und Zurücksetzen.
 */
@Composable
private fun SettingsActionButtons(
    onApply: () -> Unit,
    onOpenArchive: () -> Unit,
    onReset: () -> Unit,
) {
    Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(id = R.string.apply_filters))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onOpenArchive, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(id = R.string.open_archive))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(id = R.string.reset_app))
    }
}

// region Previews

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

@Preview(showBackground = true)
@Composable
private fun SettingsHeaderSectionPreview() {
    SettingsHeaderSection(onBack = {})
}

@Preview(showBackground = true)
@Composable
private fun CitySelectorSectionPreview() {
    CitySelectorSection(
        state = createInitialSettingsScreenState(SearchFilter()),
        onExpandedCityChosen = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsActionButtonsPreview() {
    SettingsActionButtons(onApply = {}, onOpenArchive = {}, onReset = {})
}

// endregion
