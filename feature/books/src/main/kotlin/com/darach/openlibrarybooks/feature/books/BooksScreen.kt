package com.darach.openlibrarybooks.feature.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
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
 * Books screen - placeholder for the main reading list feature.
 * Will display the user's personal reading collection with filtering and sorting.
 */
@Composable
fun BooksScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Books Coming Soon",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your personal reading collection will appear here,\n" +
                        "with adaptive grid layouts and offline support",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "Books Screen - Light")
@Composable
private fun BooksScreenPreview() {
    OpenLibraryTheme(darkTheme = false) {
        BooksScreen()
    }
}

@Preview(name = "Books Screen - Dark")
@Composable
private fun BooksScreenDarkPreview() {
    OpenLibraryTheme(darkTheme = true) {
        BooksScreen()
    }
}
