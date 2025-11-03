package com.darach.openlibrarybooks.core.domain.model

/**
 * Sealed class representing available sorting options for the book list.
 *
 * Each option defines how books should be ordered in the UI.
 */
sealed class SortOption {
    /**
     * Sort by title in ascending order (A-Z)
     */
    data object TitleAscending : SortOption()

    /**
     * Sort by title in descending order (Z-A)
     */
    data object TitleDescending : SortOption()

    /**
     * Sort by first author's name in ascending order (A-Z)
     */
    data object AuthorAscending : SortOption()

    /**
     * Sort by first author's name in descending order (Z-A)
     */
    data object AuthorDescending : SortOption()

    /**
     * Sort by publication year, newest first
     */
    data object PublishYearNewest : SortOption()

    /**
     * Sort by publication year, oldest first
     */
    data object PublishYearOldest : SortOption()

    /**
     * Sort by date added to library, newest first
     */
    data object DateAddedNewest : SortOption()

    /**
     * Sort by date added to library, oldest first
     */
    data object DateAddedOldest : SortOption()

    /**
     * Returns a human-readable display name for the sort option
     */
    fun displayName(): String = when (this) {
        is TitleAscending -> "Title (A-Z)"
        is TitleDescending -> "Title (Z-A)"
        is AuthorAscending -> "Author (A-Z)"
        is AuthorDescending -> "Author (Z-A)"
        is PublishYearNewest -> "Newest First"
        is PublishYearOldest -> "Oldest First"
        is DateAddedNewest -> "Recently Added"
        is DateAddedOldest -> "Least Recently Added"
    }
}
