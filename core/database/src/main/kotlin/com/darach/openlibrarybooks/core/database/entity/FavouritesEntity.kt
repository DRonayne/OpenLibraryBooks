package com.darach.openlibrarybooks.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's favourite books.
 *
 * Uses a foreign key relationship with BookEntity to maintain referential integrity.
 * When a book is deleted, its favourite entry is automatically deleted (cascade).
 *
 * @property id Auto-generated unique identifier for the favourite entry
 * @property bookCompositeKey Foreign key reference to BookEntity's composite key
 * @property dateAdded Timestamp (in milliseconds) when the book was marked as favourite
 */
@Entity(
    tableName = "favourites",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["composite_key"],
            childColumns = ["book_composite_key"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["book_composite_key"], unique = true),
        Index(value = ["date_added"]),
    ],
)
data class FavouritesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "book_composite_key")
    val bookCompositeKey: String,
    @ColumnInfo(name = "date_added")
    val dateAdded: Long,
)
