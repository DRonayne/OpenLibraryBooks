package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * DTO representing a specific edition of a book.
 *
 * An edition is a particular published version of a work,
 * containing edition-specific details like ISBN, publisher,
 * publication date, and physical properties.
 *
 * Endpoint: /books/{key}.json
 */
@Keep
data class EditionDto(
    @SerializedName("key")
    val key: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("covers")
    val covers: List<Int>? = null,

    @SerializedName("publishers")
    val publishers: List<String>? = null,

    @SerializedName("publish_date")
    val publishDate: String? = null,

    @SerializedName("number_of_pages")
    val numberOfPages: Int? = null,

    @SerializedName("isbn_10")
    val isbn10: List<String>? = null,

    @SerializedName("isbn_13")
    val isbn13: List<String>? = null,

    @SerializedName("physical_format")
    val physicalFormat: String? = null,

    @SerializedName("languages")
    val languages: List<LanguageDto>? = null,

    @SerializedName("weight")
    val weight: String? = null,

    @SerializedName("dimensions")
    val dimensions: String? = null,

    @SerializedName("works")
    val works: List<WorkKeyDto>? = null,
)

/**
 * DTO for language references in editions.
 */
@Keep
data class LanguageDto(
    @SerializedName("key")
    val key: String? = null,
)

/**
 * DTO for work key references in editions.
 */
@Keep
data class WorkKeyDto(
    @SerializedName("key")
    val key: String? = null,
)
