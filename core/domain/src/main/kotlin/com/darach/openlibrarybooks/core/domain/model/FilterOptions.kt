package com.darach.openlibrarybooks.core.domain.model

/**
 * Data class representing filter criteria for the book list.
 *
 * All filter properties are nullable to allow for flexible filtering.
 * If a property is null, that filter is not applied.
 *
 * @property readingStatus Filter by specific reading status (null = show all statuses)
 * @property isFavorite Filter by favorite status (null = show all, true = favorites only, false = non-favorites only)
 * @property searchQuery Text search filter for title and author names (null or empty = no search filter)
 * @property subjects Filter by specific subjects/genres (empty list = show all subjects)
 */
data class FilterOptions(
    val readingStatus: ReadingStatus? = null,
    val isFavorite: Boolean? = null,
    val searchQuery: String? = null,
    val subjects: List<String> = emptyList(),
) {
    /**
     * Returns true if no filters are applied (all properties are null or empty)
     */
    fun isEmpty(): Boolean = readingStatus == null &&
        isFavorite == null &&
        searchQuery.isNullOrBlank() &&
        subjects.isEmpty()

    /**
     * Returns true if any filters are applied
     */
    fun isNotEmpty(): Boolean = !isEmpty()
}
