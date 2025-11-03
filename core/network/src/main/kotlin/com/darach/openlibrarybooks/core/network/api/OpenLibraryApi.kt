package com.darach.openlibrarybooks.core.network.api

import com.darach.openlibrarybooks.core.network.dto.EditionDto
import com.darach.openlibrarybooks.core.network.dto.ReadingListResponseDto
import com.darach.openlibrarybooks.core.network.dto.WorkDto
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for Open Library API.
 * Configured with RxJava3 for reactive streams.
 *
 * All methods return RxJava3 Single for asynchronous operations.
 * Use ApiErrorHandler.toApiResult() to wrap calls with error handling.
 */
interface OpenLibraryApi {

    /**
     * Get a user's reading list for a specific shelf.
     *
     * Retrieves books from one of the three reading list shelves:
     * - want-to-read: Books the user wants to read
     * - currently-reading: Books actively being read
     * - already-read: Books the user has finished
     *
     * @param username Open Library username
     * @param shelf Reading list shelf (want-to-read, currently-reading, already-read)
     * @param page Page number for pagination (starts at 1)
     * @return Single emitting the reading list response
     */
    @GET("/people/{username}/books/{shelf}.json")
    fun getReadingList(
        @Path("username") username: String,
        @Path("shelf") shelf: String,
        @Query("page") page: Int = 1,
    ): Single<ReadingListResponseDto>

    /**
     * Get detailed information about a work.
     *
     * A work represents the logical book entity (the creative content),
     * separate from specific editions. Contains title, description,
     * subjects, and author references.
     *
     * @param key Work identifier (e.g., "OL45804W" from "/works/OL45804W")
     * @return Single emitting the work details
     */
    @GET("/works/{key}.json")
    fun getWork(@Path("key") key: String): Single<WorkDto>

    /**
     * Get detailed information about an edition.
     *
     * An edition is a specific published version of a work,
     * containing edition-specific details like ISBN, publisher,
     * publication date, and physical properties.
     *
     * @param key Edition identifier (e.g., "OL7353617M" from "/books/OL7353617M")
     * @return Single emitting the edition details
     */
    @GET("/books/{key}.json")
    fun getEdition(@Path("key") key: String): Single<EditionDto>
}
