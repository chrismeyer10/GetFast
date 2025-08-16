package com.example.getfast.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.getfast.R
import com.example.getfast.model.City
import com.example.getfast.model.ListingSource
import com.example.getfast.model.SearchFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    filter: SearchFilter,
    onApply: (SearchFilter) -> Unit,
    onBack: () -> Unit,
) {
    var selectedCity by remember { mutableStateOf(filter.city) }
    var priceText by remember { mutableStateOf(filter.maxPrice?.toString() ?: "") }
    val sources = remember { mutableStateListOf<ListingSource>().apply { addAll(filter.sources) } }

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
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCity.displayName,
                onValueChange = {},
                label = { Text(text = stringResource(id = R.string.city_label)) },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                City.values().forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city.displayName) },
                        onClick = {
                            selectedCity = city
                            expanded = false
                        }
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
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = ListingSource.KLEINANZEIGEN in sources,
                onClick = {
                    if (ListingSource.KLEINANZEIGEN in sources) sources.remove(ListingSource.KLEINANZEIGEN)
                    else sources.add(ListingSource.KLEINANZEIGEN)
                },
                label = { Text(text = stringResource(id = R.string.source_kleinanzeigen)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = ListingSource.IMMOSCOUT in sources,
                onClick = {
                    if (ListingSource.IMMOSCOUT in sources) sources.remove(ListingSource.IMMOSCOUT)
                    else sources.add(ListingSource.IMMOSCOUT)
                },
                label = { Text(text = stringResource(id = R.string.source_immoscout)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = ListingSource.IMMONET in sources,
                onClick = {
                    if (ListingSource.IMMONET in sources) sources.remove(ListingSource.IMMONET)
                    else sources.add(ListingSource.IMMONET)
                },
                label = { Text(text = stringResource(id = R.string.source_immonet)) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = ListingSource.IMMOWELT in sources,
                onClick = {
                    if (ListingSource.IMMOWELT in sources) sources.remove(ListingSource.IMMOWELT)
                    else sources.add(ListingSource.IMMOWELT)
                },
                label = { Text(text = stringResource(id = R.string.source_immowelt)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = ListingSource.WOHNUNGSBOERSE in sources,
                onClick = {
                    if (ListingSource.WOHNUNGSBOERSE in sources) sources.remove(ListingSource.WOHNUNGSBOERSE)
                    else sources.add(ListingSource.WOHNUNGSBOERSE)
                },
                label = { Text(text = stringResource(id = R.string.source_wohnungsboerse)) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onApply(
                    SearchFilter(
                        city = selectedCity,
                        maxPrice = priceText.toIntOrNull(),
                        sources = sources.toSet()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.apply_filters))
        }
    }
}
