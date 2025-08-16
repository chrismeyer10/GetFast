package com.example.getfast.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.getfast.R
import com.example.getfast.model.Listing
import com.example.getfast.utils.ListingDateUtils
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay

/**
 * Zeigt eine Liste von Listings mit Favoritenfunktion an.
 */
@Composable
fun ListingList(
    listings: List<Listing>,
    favorites: Set<String>,
    favoritesOnly: Boolean,
    onToggleFavorite: (Listing) -> Unit,
    highlightedIds: Set<String> = emptySet(),
    blinkingIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedListing by remember { mutableStateOf<Listing?>(null) }

    val shownListings = if (favoritesOnly) {
        listings.filter { favorites.contains(it.id) }
    } else {
        listings
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
    ) {
        items(shownListings) { listing ->
            ListingCard(
                listing = listing,
                isFavorite = favorites.contains(listing.id),
                onToggleFavorite = onToggleFavorite,
                highlighted = highlightedIds.contains(listing.id),
                blink = blinkingIds.contains(listing.id),
            ) { selectedListing = listing }
        }
    }

    selectedListing?.let { listing ->
        AlertDialog(
            onDismissRequest = { selectedListing = null },
            title = { Text(text = stringResource(id = R.string.open_listing)) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.url))
                        .setPackage("com.ebay.kleinanzeigen")
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(listing.url)))
                    }
                    selectedListing = null
                }) {
                    Text(text = stringResource(id = R.string.open_in_app))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(listing.url)))
                    selectedListing = null
                }) {
                    Text(text = stringResource(id = R.string.open_in_browser))
                }
            }
        )
    }
}

/**
 * Einzelkarte eines Listings mit optionaler Bildvorschau.
 */
@Composable
fun ListingCard(
    listing: Listing,
    isFavorite: Boolean,
    onToggleFavorite: (Listing) -> Unit,
    highlighted: Boolean,
    blink: Boolean,
    onClick: () -> Unit,
) {
    val baseColor = if (highlighted) MaterialTheme.colorScheme.error else Color.Transparent
    val borderColor by if (blink) {
        val infinite = rememberInfiniteTransition(label = "blink")
        infinite.animateColor(
            initialValue = baseColor,
            targetValue = Color.Transparent,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "color",
        )
    } else {
        rememberUpdatedState(baseColor)
    }
    var expanded by remember { mutableStateOf(false) }
    var currentImageIndex by remember { mutableStateOf(0) }
    LaunchedEffect(expanded) {
        while (expanded && listing.imageUrls.isNotEmpty()) {
            delay(3000)
            currentImageIndex = (currentImageIndex + 1) % listing.imageUrls.size
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (delta > 20) expanded = true
                    if (delta < -20) expanded = false
                },
            )
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(2.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconToggleButton(checked = isFavorite, onCheckedChange = { onToggleFavorite(listing) }) {
                    if (isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(id = R.string.favorite),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(id = R.string.not_favorite)
                        )
                    }
                }
            }
            if (expanded && listing.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = listing.imageUrls[currentImageIndex],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    contentScale = ContentScale.Crop
                )
            }
            val isNew = ListingDateUtils.isRecent(listing.date)
            Text(
                text = buildAnnotatedString {
                    append(listing.date)
                    if (isNew) {
                        append(" ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                            append("NEU")
                        }
                    }
                    append(" • ${listing.district}, ${listing.city} • ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(listing.price)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (expanded) {
                Text(
                    text = listing.summary,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
