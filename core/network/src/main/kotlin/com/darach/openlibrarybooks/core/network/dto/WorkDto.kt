package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * DTO representing a work from the Open Library API.
 *
 * A work represents the logical book entity (the creative content),
 * separate from specific editions. It contains title, description,
 * subjects, and author references.
 *
 * Endpoint: /works/{key}.json
 */
@Keep
data class WorkDto(
    @SerializedName("key")
    val key: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("description")
    val description: DescriptionDto? = null,

    @SerializedName("covers")
    val covers: List<Int>? = null,

    @SerializedName("subjects")
    val subjects: List<String>? = null,

    @SerializedName("authors")
    val authors: List<AuthorReferenceDto>? = null,

    @SerializedName("first_publish_date")
    val firstPublishDate: String? = null,

    @SerializedName("excerpts")
    val excerpts: List<ExcerptDto>? = null,

    @SerializedName("links")
    val links: List<LinkDto>? = null,
)

/**
 * DTO for book excerpts.
 */
@Keep
data class ExcerptDto(
    @SerializedName("excerpt")
    val excerpt: String? = null,

    @SerializedName("author")
    val author: AuthorKeyDto? = null,
)

/**
 * DTO for external links related to a work.
 */
@Keep
data class LinkDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("url")
    val url: String? = null,
)
