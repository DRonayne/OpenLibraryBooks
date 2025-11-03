package com.darach.openlibrarybooks.core.data.mapper

import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.domain.model.Book

/**
 * Convert a BookEntity from the database to a domain Book model.
 *
 * @return Book domain model
 */
fun BookEntity.toBook(): Book = Book(
    id = compositeKey,
    title = title,
    authors = authors,
    coverUrl = coverUrl,
    publishYear = firstPublishYear,
    description = description,
    subjects = subjects,
    readingStatus = readingStatus,
    isFavorite = false, // Favourites are managed separately via FavouritesDao
    workKey = workKey,
    editionKey = editionKey,
    dateAdded = dateAdded,
)

/**
 * Convert a domain Book model to a BookEntity for database storage.
 *
 * @return BookEntity for Room database
 */
fun Book.toBookEntity(): BookEntity = BookEntity(
    compositeKey = id,
    title = title,
    authors = authors,
    coverImageId = coverUrl?.let { extractCoverIdFromUrl(it) },
    coverUrl = coverUrl,
    firstPublishYear = publishYear,
    description = description,
    subjects = subjects,
    readingStatus = readingStatus,
    workKey = workKey,
    editionKey = editionKey,
    dateAdded = dateAdded,
    lastUpdated = System.currentTimeMillis(),
)

/**
 * Extract cover ID from a cover URL.
 *
 * Example: "https://covers.openlibrary.org/b/id/12345-M.jpg" → 12345
 *
 * @param url Cover URL
 * @return Cover ID or null if extraction fails
 */
private fun extractCoverIdFromUrl(url: String): Int? = try {
    // Pattern: https://covers.openlibrary.org/b/id/{id}-{size}.jpg
    val regex = Regex("""/id/(\d+)-[A-Z]\.jpg""")
    regex.find(url)?.groupValues?.get(1)?.toInt()
} catch (e: NumberFormatException) {
    // Invalid number format in cover ID - return null for placeholder
    android.util.Log.w("BookMappers", "Invalid cover ID format in URL: $url", e)
    null
} catch (e: IllegalArgumentException) {
    // Invalid regex pattern or URL format - return null for placeholder
    android.util.Log.w("BookMappers", "Invalid URL format: $url", e)
    null
}
