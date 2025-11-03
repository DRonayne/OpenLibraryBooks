package com.darach.openlibrarybooks.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.darach.openlibrarybooks.core.database.converter.Converters
import com.darach.openlibrarybooks.core.database.entity.PlaceholderEntity

/**
 * Room database for Open Library Books app.
 * Configured with KSP for code generation and RxJava3 for reactive queries.
 *
 * Entities and DAOs will be added in future implementation tickets.
 * Note: exportSchema is set to false during initial setup; will be configured properly
 * when entities are added.
 * Note: PlaceholderEntity is temporary and will be removed when actual entities are added.
 */
@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAOs will be added here in future implementation tickets

    companion object {
        const val DATABASE_NAME = "open_library_books.db"
    }
}
