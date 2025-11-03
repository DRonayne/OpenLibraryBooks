package com.darach.openlibrarybooks.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.darach.openlibrarybooks.BuildConfig
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
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
@Composable
fun OpenLibraryNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BooksRoute,
        modifier = modifier,
    ) {
        // Books screen - main reading list
        composable<BooksRoute> {
            BooksScreen(username = BuildConfig.DEFAULT_USERNAME)
        }

        // Favourites screen - favourite books
        composable<FavouritesRoute> {
            FavouritesScreen()
        }

        // Settings screen - app configuration
        composable<SettingsRoute> {
            SettingsScreen()
        }

        // Book details screen - configured for bottom sheet presentation
        // TODO: When implementing BookDetails feature, convert this to use a bottom sheet:
        //  - Use ModalBottomSheet or BottomSheetScaffold
        //  - Extract route arguments: backStackEntry.toRoute<BookDetailsRoute>()
        //  - Pass workId and editionId to BookDetailsScreen
        // For now, shows a placeholder to establish the navigation structure
        composable<BookDetailsRoute> { backStackEntry ->
            // This will be replaced with the actual BookDetailsScreen when implemented
            // Navigation is ready - just needs the UI implementation
            BookDetailsPlaceholder()
        }
    }
}

/**
 * Placeholder for the book details screen.
 * This will be replaced when the BookDetails feature is implemented.
 */
@Composable
private fun BookDetailsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Book Details - Coming Soon")
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailsPlaceholderPreview() {
    OpenLibraryTheme {
        BookDetailsPlaceholder()
    }
}
