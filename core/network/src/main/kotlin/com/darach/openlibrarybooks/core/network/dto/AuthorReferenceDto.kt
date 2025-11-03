package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * DTO representing an author reference in work metadata.
 *
 * Author references in works typically contain just the author key,
 * requiring a separate API call to fetch full author details.
 */
@Keep
data class AuthorReferenceDto(
    @SerializedName("author")
    val author: AuthorKeyDto? = null,

    @SerializedName("type")
    val type: AuthorTypeDto? = null,
)

/**
 * Nested DTO containing the author's key.
 */
@Keep
data class AuthorKeyDto(
    @SerializedName("key")
    val key: String? = null,
)

/**
 * Nested DTO for author type metadata.
 */
@Keep
data class AuthorTypeDto(
    @SerializedName("key")
    val key: String? = null,
)
