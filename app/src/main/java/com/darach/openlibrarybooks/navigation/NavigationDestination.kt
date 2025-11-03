package com.darach.openlibrarybooks.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Defines type-safe navigation destinations using Kotlin Serialization.
 * Top-level destinations use data objects, whilst destinations with arguments use data classes.
 */

/**
 * Books screen - displays the user's reading list
 */
@Serializable
data object BooksRoute

/**
 * Favourites screen - shows favourite books
 */
@Serializable
data object FavouritesRoute

/**
 * Settings screen - app configuration
 */
@Serializable
data object SettingsRoute

/**
 * Book details screen - shows detailed info about a specific book.
 * Configured to display as a bottom sheet.
 *
 * @param workId The Open Library work identifier (e.g., "OL45804W")
 * @param editionId Optional edition identifier for specific book editions
 */
@Serializable
data class BookDetailsRoute(val workId: String, val editionId: String? = null)

/**
 * Enum defining the main navigation destinations with their display properties.
 * Used for bottom navigation and navigation rail UI.
 */
enum class TopLevelDestination(val label: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    BOOKS(
        label = "Books",
        iconSelected = Icons.Filled.Book,
        iconUnselected = Icons.Outlined.Book,
    ),
    FAVOURITES(
        label = "Favourites",
        iconSelected = Icons.Filled.Favorite,
        iconUnselected = Icons.Outlined.FavoriteBorder,
    ),
    SETTINGS(
        label = "Settings",
        iconSelected = Icons.Filled.Settings,
        iconUnselected = Icons.Outlined.Settings,
    ),
}
