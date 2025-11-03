package com.darach.openlibrarybooks.core.common.ext

import java.util.Locale

/**
 * Generates a composite key from a book title and list of authors.
 *
 * Format: lowercase_title_lowercase_first_author
 * - Converts title and first author to lowercase
 * - Removes extra whitespace and special characters
 * - Joins with underscore separator
 * - Falls back to sanitized title if no authors are provided
 *
 * @param title The book title
 * @param authors List of author names (will use first author only)
 * @return A sanitized composite key suitable for use as a unique identifier
 *
 * Examples:
 * - generateCompositeKey("The Hobbit", ["J.R.R. Tolkien"]) -> "the_hobbit_j.r.r._tolkien"
 * - generateCompositeKey("1984", ["George Orwell"]) -> "1984_george_orwell"
 * - generateCompositeKey("Unknown Book", []) -> "unknown_book"
 */
fun generateCompositeKey(title: String, authors: List<String>): String {
    val sanitizedTitle = title.sanitizeForKey()
    val sanitizedAuthor = authors.firstOrNull()?.sanitizeForKey()

    return if (sanitizedAuthor != null) {
        "${sanitizedTitle}_$sanitizedAuthor"
    } else {
        sanitizedTitle
    }
}

/**
 * Sanitizes a string for use in a composite key.
 *
 * - Converts to lowercase
 * - Replaces multiple whitespace with single space
 * - Replaces spaces with underscores
 * - Removes or replaces special characters (keeps alphanumeric, periods, and underscores)
 *
 * @return Sanitized string suitable for use in a composite key
 */
fun String.sanitizeForKey(): String {
    return this
        .lowercase(Locale.getDefault())
        .trim()
        .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
        .replace(" ", "_") // Replace spaces with underscores
        .replace(Regex("[^a-z0-9._]"), "") // Keep only alphanumeric, periods, and underscores
        .replace(Regex("_+"), "_") // Replace multiple underscores with single underscore
        .removeSuffix("_") // Remove trailing underscore
        .removePrefix("_") // Remove leading underscore
}

/**
 * Truncates a string to a maximum length, adding an ellipsis if truncated.
 *
 * @param maxLength Maximum length of the resulting string (including ellipsis)
 * @param ellipsis The string to append when truncating (default: "...")
 * @return Truncated string with ellipsis, or original string if shorter than maxLength
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    if (this.length <= maxLength) return this
    val truncateAt = (maxLength - ellipsis.length).coerceAtLeast(0)
    return this.take(truncateAt) + ellipsis
}

/**
 * Capitalizes the first letter of each word in the string.
 *
 * @return String with each word capitalized
 */
fun String.capitalizeWords(): String = this.split(" ").joinToString(" ") { word ->
    word.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

/**
 * Returns true if the string is not null and not blank.
 */
fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

/**
 * Returns the string if it's not blank, otherwise returns null.
 */
fun String?.blankToNull(): String? = if (this.isNullOrBlank()) null else this
