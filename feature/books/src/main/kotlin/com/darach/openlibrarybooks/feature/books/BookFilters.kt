package com.darach.openlibrarybooks.feature.books

import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.FilterOptions

/**
 * Helper object for filtering books based on various criteria.
 *
 * Provides extension functions to filter book lists according to FilterOptions.
 * All filters are combined with AND logic.
 */
internal object BookFilters {

    /**
     * Applies filter criteria to a list of books.
     *
     * All filters are combined with AND logic - a book must match all
     * non-null filter criteria to be included in the results.
     *
     * @param filters Filter options to apply
     * @return Filtered list of books
     */
    fun List<Book>.applyFilters(filters: FilterOptions): List<Book> = filter { book ->
        book.matchesReadingStatus(filters) &&
            book.matchesFavoriteStatus(filters) &&
            book.matchesSearchQuery(filters) &&
            book.matchesAuthorName(filters) &&
            book.matchesYearRange(filters) &&
            book.matchesSubjects(filters)
    }

    /**
     * Checks if a book matches the reading status filter.
     */
    private fun Book.matchesReadingStatus(filters: FilterOptions): Boolean =
        filters.readingStatus?.let { status -> readingStatus == status } ?: true

    /**
     * Checks if a book matches the favorite status filter.
     */
    private fun Book.matchesFavoriteStatus(filters: FilterOptions): Boolean =
        filters.isFavorite?.let { isFav -> isFavorite == isFav } ?: true

    /**
     * Checks if a book matches the search query filter (title and authors).
     */
    private fun Book.matchesSearchQuery(filters: FilterOptions): Boolean = filters.searchQuery?.let { query ->
        if (query.isBlank()) {
            true
        } else {
            title.contains(query, ignoreCase = true) ||
                authors.any { author -> author.contains(query, ignoreCase = true) }
        }
    } ?: true

    /**
     * Checks if a book matches the author name filter.
     */
    private fun Book.matchesAuthorName(filters: FilterOptions): Boolean = filters.authorName?.let { authorName ->
        if (authorName.isBlank()) {
            true
        } else {
            authors.any { author -> author.contains(authorName, ignoreCase = true) }
        }
    } ?: true

    /**
     * Checks if a book matches the year range filter.
     */
    private fun Book.matchesYearRange(filters: FilterOptions): Boolean {
        val matchesYearFrom = filters.yearFrom?.let { yearFrom ->
            publishYear?.let { it >= yearFrom } ?: false
        } ?: true

        val matchesYearTo = filters.yearTo?.let { yearTo ->
            publishYear?.let { it <= yearTo } ?: false
        } ?: true

        return matchesYearFrom && matchesYearTo
    }

    /**
     * Checks if a book matches the subjects filter.
     */
    private fun Book.matchesSubjects(filters: FilterOptions): Boolean = if (filters.subjects.isNotEmpty()) {
        subjects.any { bookSubject ->
            filters.subjects.any { filterSubject ->
                bookSubject.contains(filterSubject, ignoreCase = true)
            }
        }
    } else {
        true
    }
}
