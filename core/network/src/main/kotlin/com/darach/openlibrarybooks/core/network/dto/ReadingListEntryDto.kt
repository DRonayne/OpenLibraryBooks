package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * DTO representing a single entry in a user's reading list.
 *
 * Each entry contains the work reference, the edition that was logged,
 * and the date when it was added to the list.
 */
@Keep
data class ReadingListEntryDto(
    @SerializedName("work")
    val work: WorkReferenceDto? = null,

    @SerializedName("logged_edition")
    val loggedEdition: String? = null,

    @SerializedName("logged_date")
    val loggedDate: String? = null,
)
