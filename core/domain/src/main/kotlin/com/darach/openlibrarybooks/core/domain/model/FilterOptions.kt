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
 * @property authorName Filter by author name (contains, case-insensitive)
 * @property yearFrom Minimum publish year (inclusive, null = no minimum)
 * @property yearTo Maximum publish year (inclusive, null = no maximum)
 */
data class FilterOptions(
    val readingStatus: ReadingStatus? = null,
    val isFavorite: Boolean? = null,
    val searchQuery: String? = null,
    val subjects: List<String> = emptyList(),
    val authorName: String? = null,
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
) {
    /**
     * Returns true if no filters are applied (all properties are null or empty)
     */
    fun isEmpty(): Boolean = readingStatus == null &&
        isFavorite == null &&
        searchQuery.isNullOrBlank() &&
        subjects.isEmpty() &&
        authorName.isNullOrBlank() &&
        yearFrom == null &&
        yearTo == null

    /**
     * Returns true if any filters are applied
     */
    fun isNotEmpty(): Boolean = !isEmpty()
}
