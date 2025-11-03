package com.darach.openlibrarybooks.core.data.mapper

import com.darach.openlibrarybooks.core.domain.model.EditionDetails
import com.darach.openlibrarybooks.core.network.dto.EditionDto

/**
 * Convert an EditionDto from the API to a domain EditionDetails model.
 *
 * Handles nullable fields and extracts language codes and work keys
 * from nested structures.
 *
 * @return EditionDetails domain model
 */
fun EditionDto.toDomain(): EditionDetails = EditionDetails(
    editionKey = key ?: "",
    title = title ?: "Untitled",
    isbn10 = isbn10 ?: emptyList(),
    isbn13 = isbn13 ?: emptyList(),
    publishers = publishers ?: emptyList(),
    publishDate = publishDate,
    numberOfPages = numberOfPages,
    physicalFormat = physicalFormat,
    languages = languages?.mapNotNull { it.key } ?: emptyList(),
    weight = weight,
    dimensions = dimensions,
    coverIds = covers ?: emptyList(),
    workKey = works?.firstOrNull()?.key,
)
