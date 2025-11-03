package com.darach.openlibrarybooks.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
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
 * Settings screen - placeholder for app preferences and configuration.
 * Will allow users to customise dark mode, username, and other app settings.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Settings Coming Soon",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Customise your app experience with\n" +
                        "dark mode, username, and sync preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "Settings Screen - Light")
@Composable
private fun SettingsScreenPreview() {
    OpenLibraryTheme(darkTheme = false) {
        SettingsScreen()
    }
}

@Preview(name = "Settings Screen - Dark")
@Composable
private fun SettingsScreenDarkPreview() {
    OpenLibraryTheme(darkTheme = true) {
        SettingsScreen()
    }
}
