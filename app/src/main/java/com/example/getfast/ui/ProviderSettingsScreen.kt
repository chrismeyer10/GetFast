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
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.model.City
import com.example.getfast.model.CityCatalog
import com.example.getfast.model.SearchFilter

/**
 * Einstellungen für einen einzelnen Anbieter.
 * Vorerst identisch für alle Provider, kann jedoch später
 * individuell erweitert werden.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    filter: SearchFilter,
    onApply: (SearchFilter) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val allCities = remember { CityCatalog.germany }
    var selectedCity by remember(filter.city) {
        mutableStateOf(CityCatalog.findByName(filter.city.displayName) ?: filter.city)
    }
    var cityQuery by remember { mutableStateOf(selectedCity.displayName) }
    var priceText by remember { mutableStateOf(filter.maxPrice?.toString() ?: "") }
    var daysText by remember { mutableStateOf(filter.maxAgeDays.toString()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
            }
            Text(
                text = stringResource(id = R.string.settings_title),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        var expanded by remember { mutableStateOf(false) }
        val filteredCities = remember(cityQuery) {
            val query = cityQuery.trim()
            if (query.isEmpty()) {
                allCities
            } else {
                allCities.filter { it.displayName.contains(query, ignoreCase = true) }
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = cityQuery,
                onValueChange = { input ->
                    cityQuery = input
                    expanded = true
                    val normalized = input.trim()
                    val exactMatch = allCities.firstOrNull { city ->
                        city.displayName.equals(normalized, ignoreCase = true)
                    }
                    selectedCity = when {
                        exactMatch != null -> exactMatch
                        normalized.isEmpty() -> selectedCity
                        else -> City.custom(input)
                    }
                },
                label = { Text(text = stringResource(id = R.string.city_label)) },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            val trimmedQuery = cityQuery.trim()
            val hasExactMatch = filteredCities.any { city ->
                city.displayName.equals(trimmedQuery, ignoreCase = true)
            }
            val shouldOfferCustom = trimmedQuery.isNotEmpty() && !hasExactMatch

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                filteredCities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city.displayName) },
                        onClick = {
                            selectedCity = city
                            cityQuery = city.displayName
                            expanded = false
                        }
                    )
                }

                if (shouldOfferCustom) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.use_custom_city, trimmedQuery)) },
                        onClick = {
                            selectedCity = City.custom(trimmedQuery)
                            cityQuery = trimmedQuery
                            expanded = false
                        }
                    )
                }

                if (filteredCities.isEmpty() && !shouldOfferCustom) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.no_city_results)) },
                        enabled = false,
                        onClick = {}
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
            label = { Text(text = stringResource(id = R.string.max_price_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = daysText,
            onValueChange = { daysText = it.filter { ch -> ch.isDigit() }.take(1) },
            label = { Text(text = stringResource(id = R.string.max_days_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onApply(
                    SearchFilter(
                        city = selectedCity,
                        maxPrice = priceText.toIntOrNull(),
                        maxAgeDays = daysText.toIntOrNull()?.coerceIn(0, 3) ?: 3,
                        sources = filter.sources
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.apply_filters))
        }
    }
}
