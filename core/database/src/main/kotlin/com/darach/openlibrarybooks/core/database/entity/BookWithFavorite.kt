package com.darach.openlibrarybooks.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Data class representing a book with its favorite status.
 *
 * Used by Room queries that join books with favourites table.
 * The book entity fields are embedded directly, and the favorite status
 * is added as an additional field.
 *
 * @property bookEntity The book entity with all book data
 * @property isFavorite Whether the book is marked as favorite (1) or not (0)
 */
data class BookWithFavorite(
    @Embedded
    val bookEntity: BookEntity,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Int,
) {
    /**
     * Convenience property to get favorite status as a Boolean.
     */
    val isFavoriteBoolean: Boolean
        get() = isFavorite == 1
}
