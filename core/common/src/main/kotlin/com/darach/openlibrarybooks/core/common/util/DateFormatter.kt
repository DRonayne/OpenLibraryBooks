package com.darach.openlibrarybooks.core.common.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility object for formatting dates and timestamps.
 *
 * Provides consistent date formatting across the application for:
 * - Display in UI
 * - Relative time formatting (e.g., "2 hours ago")
 * - API date parsing
 */
object DateFormatter {

    private const val DISPLAY_FORMAT = "MMM dd, yyyy"
    private const val DISPLAY_FORMAT_WITH_TIME = "MMM dd, yyyy HH:mm"
    private const val API_FORMAT = "yyyy-MM-dd"

    /**
     * Formats a timestamp (in milliseconds) to a human-readable date string.
     *
     * @param timestamp Timestamp in milliseconds since epoch
     * @param includeTime Whether to include time in the format (default: false)
     * @return Formatted date string (e.g., "Jan 15, 2024" or "Jan 15, 2024 14:30")
     */
    fun formatDate(timestamp: Long, includeTime: Boolean = false): String {
        val format = if (includeTime) DISPLAY_FORMAT_WITH_TIME else DISPLAY_FORMAT
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Formats a Date object to a human-readable date string.
     *
     * @param date The Date object to format
     * @param includeTime Whether to include time in the format (default: false)
     * @return Formatted date string
     */
    fun formatDate(date: Date, includeTime: Boolean = false): String {
        val format = if (includeTime) DISPLAY_FORMAT_WITH_TIME else DISPLAY_FORMAT
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date)
    }

    /**
     * Formats a timestamp as a relative time string (e.g., "2 hours ago", "3 days ago").
     *
     * @param timestamp Timestamp in milliseconds since epoch
     * @return Relative time string, or formatted date if older than 7 days
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 0 -> "Just now" // Future timestamp
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            else -> formatDate(timestamp)
        }
    }

    /**
     * Formats a timestamp for API requests (ISO 8601 format: yyyy-MM-dd).
     *
     * @param timestamp Timestamp in milliseconds since epoch
     * @return Date string in API format
     */
    fun formatForApi(timestamp: Long): String {
        val sdf = SimpleDateFormat(API_FORMAT, Locale.US)
        return sdf.format(Date(timestamp))
    }

    /**
     * Parses a date string from API format (yyyy-MM-dd) to a timestamp.
     *
     * @param dateString Date string in API format
     * @return Timestamp in milliseconds, or null if parsing fails
     */
    fun parseFromApi(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat(API_FORMAT, Locale.US)
            sdf.parse(dateString)?.time
        } catch (e: java.text.ParseException) {
            // Return null for invalid date formats
            null
        }
    }

    /**
     * Returns the current timestamp in milliseconds.
     *
     * @return Current timestamp
     */
    fun now(): Long = System.currentTimeMillis()
}
