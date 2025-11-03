package com.darach.openlibrarybooks.feature.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme

/**
 * Favourites screen - placeholder for the favourites collection feature.
 * Will display books marked as favourites from the reading list.
 */
@Composable
fun FavouritesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Favourites Coming Soon",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mark your favourite books and\n" +
                        "they'll be showcased here and on your home widget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "Favourites Screen - Light")
@Composable
private fun FavouritesScreenPreview() {
    OpenLibraryTheme(darkTheme = false) {
        FavouritesScreen()
    }
}

@Preview(name = "Favourites Screen - Dark")
@Composable
private fun FavouritesScreenDarkPreview() {
    OpenLibraryTheme(darkTheme = true) {
        FavouritesScreen()
    }
}
