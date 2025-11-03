package com.darach.openlibrarybooks.core.database.converter

import android.util.Log
import androidx.room.TypeConverter
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters for converting complex data types to/from database-compatible formats.
 *
 * These converters handle:
 * - List<String> conversions using JSON serialization
 * - ReadingStatus enum conversions
 * - Null safety for all conversions
 */
class Converters {

    private val gson = Gson()

    companion object {
        private const val TAG = "Converters"
    }

    /**
     * Converts a List<String> to a JSON string for storage in the database.
     * Handles null input gracefully by returning null.
     *
     * @param list The list to convert, or null
     * @return JSON string representation of the list, or null if input is null
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { gson.toJson(it) }

    /**
     * Converts a JSON string to a List<String> from the database.
     * Handles null and empty strings gracefully by returning an empty list.
     *
     * @param value The JSON string from the database, or null
     * @return The deserialized list, or an empty list if value is null or empty
     */
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) {
            return emptyList()
        }
        return try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.w(TAG, "Failed to parse JSON to List<String>: $value", e)
            emptyList()
        }
    }

    /**
     * Converts a ReadingStatus enum to its string name for storage in the database.
     * Handles null input gracefully by returning null.
     *
     * @param status The ReadingStatus to convert, or null
     * @return The string name of the enum, or null if input is null
     */
    @TypeConverter
    fun fromReadingStatus(status: ReadingStatus?): String? = status?.name

    /**
     * Converts a string name to a ReadingStatus enum from the database.
     * Handles null and invalid strings gracefully by returning null.
     *
     * @param value The string name from the database, or null
     * @return The corresponding ReadingStatus enum, or null if value is null or invalid
     */
    @TypeConverter
    fun toReadingStatus(value: String?): ReadingStatus? {
        if (value.isNullOrBlank()) return null
        return try {
            ReadingStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid ReadingStatus value: $value", e)
            null
        }
    }
}
