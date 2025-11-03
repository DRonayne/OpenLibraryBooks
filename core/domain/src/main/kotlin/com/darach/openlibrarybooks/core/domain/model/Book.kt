package com.darach.openlibrarybooks.core.domain.model

/**
 * Domain model representing a book in the user's reading library.
 *
 * This is a pure Kotlin model with no Android dependencies, suitable for use
 * across all layers of the application.
 *
 * @property id Unique identifier for the book (composite key or Open Library ID)
 * @property title The title of the book
 * @property authors List of author names
 * @property coverUrl URL to the book's cover image (if available)
 * @property publishYear Year the book was published
 * @property description Brief description or synopsis of the book
 * @property subjects List of subjects/genres associated with the book
 * @property readingStatus Current reading status (Want to Read, Currently Reading, Already Read)
 * @property isFavorite Whether the user has marked this book as a favorite
 * @property workKey Open Library work key (e.g., "/works/OL45804W")
 * @property editionKey Open Library edition key (e.g., "/books/OL7353617M")
 * @property dateAdded Timestamp (in milliseconds) when the book was added to the user's library
 */
data class Book(
    val id: String,
    val title: String,
    val authors: List<String> = emptyList(),
    val coverUrl: String? = null,
    val publishYear: Int? = null,
    val description: String? = null,
    val subjects: List<String> = emptyList(),
    val readingStatus: ReadingStatus,
    val isFavorite: Boolean = false,
    val workKey: String? = null,
    val editionKey: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
)
