package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * DTO representing a work reference in reading list entries.
 *
 * This lightweight version contains just the essential metadata
 * returned by the Reading List API endpoint.
 */
@Keep
data class WorkReferenceDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("key")
    val key: String? = null,

    @SerializedName("author_keys")
    val authorKeys: List<String>? = null,

    @SerializedName("author_names")
    val authorNames: List<String>? = null,

    @SerializedName("first_publish_year")
    val firstPublishYear: Int? = null,

    @SerializedName("cover_id")
    val coverId: Int? = null,
)
