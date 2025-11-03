package com.darach.openlibrarybooks.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus

/**
 * Room entity representing a book in the local database.
 *
 * Uses a composite key generated from title and first author for uniqueness.
 * Includes indices on frequently queried fields (readingStatus, workKey) for performance.
 *
 * @property compositeKey Primary key generated from title + first author
 * @property title The book's title
 * @property authors List of author names (stored as JSON via TypeConverter)
 * @property coverImageId Open Library cover ID for fetching cover images
 * @property coverUrl Direct URL to the book's cover image
 * @property firstPublishYear Year the book was first published
 * @property description Book description or synopsis
 * @property subjects List of subjects/genres (stored as JSON via TypeConverter)
 * @property readingStatus User's reading status (Want to Read, Currently Reading, Already Read)
 * @property workKey Open Library work key (e.g., "/works/OL45804W")
 * @property editionKey Open Library edition key (e.g., "/books/OL7353617M")
 * @property dateAdded Timestamp (in milliseconds) when the book was added
 * @property lastUpdated Timestamp (in milliseconds) when the book was last updated
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["reading_status"]),
        Index(value = ["work_key"]),
        Index(value = ["date_added"]),
    ],
)
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "composite_key")
    val compositeKey: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "authors")
    val authors: List<String>,
    @ColumnInfo(name = "cover_image_id")
    val coverImageId: Int?,
    @ColumnInfo(name = "cover_url")
    val coverUrl: String?,
    @ColumnInfo(name = "first_publish_year")
    val firstPublishYear: Int?,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "subjects")
    val subjects: List<String>,
    @ColumnInfo(name = "reading_status")
    val readingStatus: ReadingStatus,
    @ColumnInfo(name = "work_key")
    val workKey: String?,
    @ColumnInfo(name = "edition_key")
    val editionKey: String?,
    @ColumnInfo(name = "date_added")
    val dateAdded: Long,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long,
)
