package com.darach.openlibrarybooks.core.domain.model

/**
 * Data class representing filter criteria for the book list.
 *
 * All filter properties are nullable to allow for flexible filtering.
 * If a property is null, that filter is not applied.
 *
 * @property readingStatuses Set of reading statuses to filter by (empty set = show all statuses)
 * @property isFavorite Filter by favorite status (null = show all, true = favorites only, false = non-favorites only)
 * @property searchQuery Text search filter for title and author names (null or empty = no search filter)
 * @property subjects List of subjects/genres to filter by (empty list = show all subjects)
 * @property authors List of author names to filter by (empty list = show all authors)
 * @property yearFrom Minimum publish year (inclusive, null = no minimum)
 * @property yearTo Maximum publish year (inclusive, null = no maximum)
 */
data class FilterOptions(
    val readingStatuses: Set<ReadingStatus> = setOf(ReadingStatus.WantToRead),
    val isFavorite: Boolean? = null,
    val searchQuery: String? = null,
    val subjects: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
) {
    /**
     * Returns true if no filters are applied (all properties are null or empty)
     */
    fun isEmpty(): Boolean = readingStatuses.isEmpty() &&
        isFavorite == null &&
        searchQuery.isNullOrBlank() &&
        subjects.isEmpty() &&
        authors.isEmpty() &&
        yearFrom == null &&
        yearTo == null

    /**
     * Returns true if any filters are applied
     */
    fun isNotEmpty(): Boolean = !isEmpty()
}
