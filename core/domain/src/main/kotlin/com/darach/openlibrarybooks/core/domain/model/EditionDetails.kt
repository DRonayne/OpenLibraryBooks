package com.darach.openlibrarybooks.core.domain.model

/**
 * Domain model representing detailed information about a specific edition from Open Library.
 *
 * An edition represents a specific published version of a work, with unique attributes like
 * publisher, ISBN, format, and physical characteristics. One work can have many editions.
 *
 * @property editionKey Open Library edition key (e.g., "/books/OL7353617M")
 * @property title The title of this edition
 * @property isbn10 ISBN-10 identifier(s)
 * @property isbn13 ISBN-13 identifier(s)
 * @property publishers List of publishers
 * @property publishDate Date this edition was published
 * @property numberOfPages Number of pages in this edition
 * @property physicalFormat Physical format (e.g., "Hardcover", "Paperback", "Mass Market Paperback")
 * @property languages List of language codes (e.g., "eng", "spa")
 * @property weight Weight of the physical book
 * @property dimensions Physical dimensions of the book
 * @property coverIds List of cover image IDs (use with Covers API)
 * @property workKey Reference to the parent work
 */
data class EditionDetails(
    val editionKey: String,
    val title: String,
    val isbn10: List<String> = emptyList(),
    val isbn13: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val publishDate: String? = null,
    val numberOfPages: Int? = null,
    val physicalFormat: String? = null,
    val languages: List<String> = emptyList(),
    val weight: String? = null,
    val dimensions: String? = null,
    val coverIds: List<Int> = emptyList(),
    val workKey: String? = null,
)
