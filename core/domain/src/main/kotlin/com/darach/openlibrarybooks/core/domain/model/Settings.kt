package com.darach.openlibrarybooks.core.domain.model

/**
 * Domain model representing user app settings and preferences.
 *
 * @property username Open Library username for accessing reading lists
 * @property darkModeEnabled Whether dark mode is enabled
 * @property dynamicThemeEnabled Whether dynamic theming (Material You) is enabled
 * @property lastSyncTimestamp Timestamp (in milliseconds) of the last successful sync with the API
 * @property sortOption The current sort option for book lists
 * @property filterOptions The current filter options for book lists
 */
data class Settings(
    val username: String = "",
    val darkModeEnabled: Boolean = false,
    val dynamicThemeEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val sortOption: SortOption = SortOption.DateAddedNewest,
    val filterOptions: FilterOptions = FilterOptions(),
)
