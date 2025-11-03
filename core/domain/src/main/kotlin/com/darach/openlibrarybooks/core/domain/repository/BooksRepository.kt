package com.darach.openlibrarybooks.core.domain.repository

import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.EditionDetails
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.WorkDetails
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing books and reading lists.
 *
 * Handles fetching books from the Open Library API, caching them locally,
 * and providing access to detailed work and edition information. Uses RxJava
 * for reactive data fetching and Flow for observing cached data.
 *
 * Implementations should follow an offline-first approach:
 * 1. Fetch data from the API
 * 2. Cache it locally in the database
 * 3. Expose cached data via Flow for UI consumption
 */
interface BooksRepository {

    /**
     * Fetch books from all reading list shelves for the specified user.
     *
     * Retrieves books from Want to Read, Currently Reading, and Already Read
     * shelves in a single operation. The results are cached locally for offline access.
     *
     * @param username Open Library username to fetch reading lists for
     * @return Single emitting the complete list of books from all shelves
     */
    fun syncBooks(username: String): Single<List<Book>>

    /**
     * Observe all cached books from the local database.
     *
     * Returns a Flow that emits the complete list of books whenever the
     * underlying data changes. This allows the UI to reactively update.
     *
     * @return Flow emitting the list of all cached books
     */
    fun getBooks(): Flow<List<Book>>

    /**
     * Observe cached books filtered by reading status.
     *
     * Returns a Flow that emits books matching the specified reading status
     * (Want to Read, Currently Reading, or Already Read).
     *
     * @param status The reading status to filter by
     * @return Flow emitting the filtered list of books
     */
    fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>>

    /**
     * Observe a single book by its unique identifier.
     *
     * @param bookId Unique identifier for the book
     * @return Flow emitting the book, or null if not found
     */
    fun getBookById(bookId: String): Flow<Book?>

    /**
     * Fetch detailed work information from the Open Library API.
     *
     * Retrieves comprehensive metadata about a work, including description,
     * subjects, authors, and related information. Results are cached locally.
     *
     * @param workKey Open Library work key (e.g., "/works/OL27448W")
     * @return Single emitting the detailed work information
     */
    fun getWorkDetails(workKey: String): Single<WorkDetails>

    /**
     * Fetch detailed edition information from the Open Library API.
     *
     * Retrieves specific edition details like ISBN, publisher, physical format,
     * and publication date. Results are cached locally.
     *
     * @param editionKey Open Library edition key (e.g., "/books/OL7353617M")
     * @return Single emitting the detailed edition information
     */
    fun getEditionDetails(editionKey: String): Single<EditionDetails>

    /**
     * Synchronise all reading list shelves from the API to the local cache.
     *
     * Fetches the latest data from Want to Read, Currently Reading, and Already Read
     * shelves for the specified user, and updates the local database. This is typically
     * called during background sync operations.
     *
     * @param username Open Library username to sync reading lists for
     * @return Completable that completes when sync is finished
     */
    fun sync(username: String): Completable

    /**
     * Clear all cached books from the local database.
     *
     * Removes all book data from the local cache. This does not affect
     * the user's reading lists on Open Library.
     *
     * @return Completable that completes when cache is cleared
     */
    fun clearCache(): Completable
}
