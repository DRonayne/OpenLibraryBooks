package com.darach.openlibrarybooks.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.darach.openlibrarybooks.BuildConfig
import com.darach.openlibrarybooks.feature.books.BooksScreen
import com.darach.openlibrarybooks.feature.favourites.FavouritesScreen
import com.darach.openlibrarybooks.feature.settings.SettingsScreen

/**
 * Defines the navigation graph for the Open Library Books app.
 * Uses type-safe navigation with Kotlin Serialization.
 *
 * @param navController The navigation controller managing the back stack
 * @param modifier Optional modifier for the NavHost
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenLibraryNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BooksRoute,
        modifier = modifier,
    ) {
        // Books screen - main reading list
        // Book details are handled internally in BooksScreen with ModalBottomSheet
        composable<BooksRoute> {
            BooksScreen(
                username = BuildConfig.DEFAULT_USERNAME,
            )
        }

        // Favourites screen - favourite books
        composable<FavouritesRoute> {
            FavouritesScreen()
        }

        // Settings screen - app configuration
        composable<SettingsRoute> {
            SettingsScreen()
        }
    }
}
