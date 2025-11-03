package com.darach.openlibrarybooks.core.domain.model

/**
 * Domain model representing detailed information about a work from Open Library.
 *
 * A work represents the logical creative entity of a book, independent of any specific edition.
 * For example, "The Lord of the Rings" is a work, with many editions (different publishers,
 * languages, formats, etc.).
 *
 * @property workKey Open Library work key (e.g., "/works/OL27448W")
 * @property title The title of the work
 * @property description Full description or synopsis of the work
 * @property subjects List of subjects/genres associated with the work
 * @property authors List of author names
 * @property authorKeys List of Open Library author keys
 * @property coverIds List of cover image IDs (use with Covers API)
 * @property firstPublishDate First publication date of this work
 * @property excerpts Text excerpts from the work
 * @property links External links related to the work
 */
data class WorkDetails(
    val workKey: String,
    val title: String,
    val description: String? = null,
    val subjects: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val authorKeys: List<String> = emptyList(),
    val coverIds: List<Int> = emptyList(),
    val firstPublishDate: String? = null,
    val excerpts: List<String> = emptyList(),
    val links: List<String> = emptyList(),
)
