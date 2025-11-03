package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * DTO representing the response from the Reading List API.
 *
 * Endpoint: /people/{username}/books/{shelf}.json
 * Shelves: want-to-read, currently-reading, already-read
 */
@Keep
data class ReadingListResponseDto(
    @SerializedName("page")
    val page: Int? = null,

    @SerializedName("reading_log_entries")
    val readingLogEntries: List<ReadingListEntryDto>? = null,
)
