package com.darach.openlibrarybooks.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.darach.openlibrarybooks.core.database.converter.Converters
import com.darach.openlibrarybooks.core.database.dao.BookDao
import com.darach.openlibrarybooks.core.database.dao.FavouritesDao
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.database.entity.FavouritesEntity

/**
 * Room database for Open Library Books app.
 * Configured with KSP for code generation and RxJava3 for reactive queries.
 *
 * Database includes:
 * - BookEntity: Stores book information with composite key (title + first author)
 * - FavouritesEntity: Tracks user's favourite books with foreign key to BookEntity
 *
 * TypeConverters handle:
 * - List<String> (authors, subjects) serialization via JSON
 * - ReadingStatus enum conversion
 *
 * Migration strategy:
 * - Version 1: Initial database with books and favourites tables
 * - Future versions: Add migrations as needed
 */
@Database(
    entities = [
        BookEntity::class,
        FavouritesEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Data Access Object for BookEntity.
     * Provides queries for managing books and reading lists.
     */
    abstract fun bookDao(): BookDao

    /**
     * Data Access Object for FavouritesEntity.
     * Provides queries for managing favourite books with JOIN operations.
     */
    abstract fun favouritesDao(): FavouritesDao

    companion object {
        const val DATABASE_NAME = "open_library_books.db"
    }
}
