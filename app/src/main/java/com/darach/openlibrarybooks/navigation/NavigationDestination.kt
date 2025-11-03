package com.darach.openlibrarybooks.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the top-level navigation destinations in the app.
 * Each destination has both filled and outlined icons for selected/unselected states.
 */
enum class NavigationDestination(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
) {
    BOOKS(
        route = "books",
        label = "Books",
        iconSelected = Icons.Filled.Book,
        iconUnselected = Icons.Outlined.Book,
    ),
    FAVOURITES(
        route = "favourites",
        label = "Favourites",
        iconSelected = Icons.Filled.Favorite,
        iconUnselected = Icons.Outlined.FavoriteBorder,
    ),
    SETTINGS(
        route = "settings",
        label = "Settings",
        iconSelected = Icons.Filled.Settings,
        iconUnselected = Icons.Outlined.Settings,
    ),
}
