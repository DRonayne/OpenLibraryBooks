package com.darach.openlibrarybooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.feature.books.BooksScreen
import com.darach.openlibrarybooks.feature.favourites.FavouritesScreen
import com.darach.openlibrarybooks.feature.settings.SettingsScreen
import com.darach.openlibrarybooks.navigation.NavigationDestination
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Open Library Books app.
 * Implements adaptive navigation that switches between bottom nav (mobile) and nav rail (tablet).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenLibraryTheme {
                var selectedDestination by rememberSaveable {
                    mutableStateOf(NavigationDestination.BOOKS)
                }

                NavigationSuiteScaffold(
                    modifier = Modifier.fillMaxSize(),
                    navigationSuiteItems = {
                        NavigationDestination.entries.forEach { destination ->
                            item(
                                icon = {
                                    androidx.compose.material3.Icon(
                                        imageVector =
                                        if (selectedDestination == destination) {
                                            destination.iconSelected
                                        } else {
                                            destination.iconUnselected
                                        },
                                        contentDescription = destination.label,
                                    )
                                },
                                label = { androidx.compose.material3.Text(destination.label) },
                                selected = selectedDestination == destination,
                                onClick = { selectedDestination = destination },
                            )
                        }
                    },
                ) {
                    // Display the appropriate screen based on selected destination
                    when (selectedDestination) {
                        NavigationDestination.BOOKS -> BooksScreen()
                        NavigationDestination.FAVOURITES -> FavouritesScreen()
                        NavigationDestination.SETTINGS -> SettingsScreen()
                    }
                }
            }
        }
    }
}
