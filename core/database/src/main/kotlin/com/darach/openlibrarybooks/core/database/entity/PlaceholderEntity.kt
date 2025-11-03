package com.darach.openlibrarybooks.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Placeholder entity for initial database setup.
 * This entity exists to satisfy Room's requirement for at least one entity.
 * It will be removed when actual entities are implemented.
 */
@Entity(tableName = "placeholder")
internal data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0,
)
