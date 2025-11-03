package com.darach.openlibrarybooks.core.data.mapper

import com.darach.openlibrarybooks.core.domain.model.WorkDetails
import com.darach.openlibrarybooks.core.network.dto.WorkDto

/**
 * Convert a WorkDto from the API to a domain WorkDetails model.
 *
 * Handles nullable fields and extracts author keys from the nested structure.
 *
 * @return WorkDetails domain model
 */
fun WorkDto.toDomain(): WorkDetails = WorkDetails(
    workKey = key ?: "",
    title = title ?: "Untitled",
    description = description?.getDescription(),
    subjects = subjects ?: emptyList(),
    authors = emptyList(), // Author names not included in work details, would need separate API calls
    authorKeys = authors?.mapNotNull { it.author?.key } ?: emptyList(),
    coverIds = covers ?: emptyList(),
    firstPublishDate = firstPublishDate,
    excerpts = excerpts?.mapNotNull { it.excerpt } ?: emptyList(),
    links = links?.mapNotNull { it.url } ?: emptyList(),
)
