package com.darach.openlibrarybooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.navigation.BooksRoute
import com.darach.openlibrarybooks.navigation.FavouritesRoute
import com.darach.openlibrarybooks.navigation.OpenLibraryNavHost
import com.darach.openlibrarybooks.navigation.SettingsRoute
import com.darach.openlibrarybooks.navigation.TopLevelDestination
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Open Library Books app.
 * Implements adaptive navigation that switches between bottom nav (mobile) and nav rail (tablet).
 * Uses type-safe navigation with Kotlin Serialization.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for deeplink from widget or external source
        val deeplink = intent?.data?.toString()

        setContent {
            OpenLibraryApp(deeplinkUri = deeplink)
        }
    }
}

/**
 * Main app composable that sets up navigation and adaptive UI.
 * Observes user settings to apply dark mode and dynamic theming.
 *
 * @param deeplinkUri Optional deeplink URI from widget or external source
 */
@Composable
private fun OpenLibraryApp(deeplinkUri: String? = null, viewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Observe settings for theme configuration
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Handle deeplink navigation
    androidx.compose.runtime.LaunchedEffect(deeplinkUri) {
        if (deeplinkUri == "openlibrarybooks://favourites") {
            navController.navigate(FavouritesRoute) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
            }
        }
    }

    OpenLibraryTheme(
        darkTheme = settings.darkModeEnabled,
        dynamicColor = settings.dynamicThemeEnabled,
    ) {
        AppNavigationScaffold(
            navController = navController,
            currentDestination = currentDestination,
        )
    }
}

/**
 * Navigation scaffold with adaptive bottom navigation or nav rail.
 */
@Composable
private fun AppNavigationScaffold(
    navController: androidx.navigation.NavHostController,
    currentDestination: androidx.navigation.NavDestination?,
) {
    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentDestination?.hierarchy?.any { navDest ->
                    when (destination) {
                        TopLevelDestination.BOOKS -> navDest.hasRoute(BooksRoute::class)
                        TopLevelDestination.FAVOURITES -> navDest.hasRoute(FavouritesRoute::class)
                        TopLevelDestination.SETTINGS -> navDest.hasRoute(SettingsRoute::class)
                    }
                } == true

                item(
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = if (isSelected) {
                                destination.iconSelected
                            } else {
                                destination.iconUnselected
                            },
                            contentDescription = destination.label,
                        )
                    },
                    label = { androidx.compose.material3.Text(destination.label) },
                    selected = isSelected,
                    onClick = {
                        // Navigate to the selected destination, clearing back stack to start
                        val targetRoute = when (destination) {
                            TopLevelDestination.BOOKS -> BooksRoute
                            TopLevelDestination.FAVOURITES -> FavouritesRoute
                            TopLevelDestination.SETTINGS -> SettingsRoute
                        }

                        navController.navigate(targetRoute) {
                            // Pop up to the start destination and save state
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when navigating back to a destination
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) {
        OpenLibraryNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
