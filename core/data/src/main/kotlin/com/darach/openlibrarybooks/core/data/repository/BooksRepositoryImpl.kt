package com.darach.openlibrarybooks.core.data.repository

import android.util.Log
import com.darach.openlibrarybooks.core.data.mapper.toBook
import com.darach.openlibrarybooks.core.data.mapper.toBookEntity
import com.darach.openlibrarybooks.core.data.mapper.toDomain
import com.darach.openlibrarybooks.core.database.dao.BookDao
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.EditionDetails
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.WorkDetails
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.core.network.api.OpenLibraryApi
import com.darach.openlibrarybooks.core.network.dto.ReadingListEntryDto
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.asFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BooksRepository with offline-first strategy.
 *
 * Uses RxJava for reactive data fetching from the API and Room's Flowable
 * converted to Kotlin Flow for observing cached data. Implements comprehensive
 * cover fallback logic and handles edge cases gracefully.
 *
 * @property api The Open Library API service
 * @property bookDao The local database DAO for books
 */
@Singleton
class BooksRepositoryImpl @Inject constructor(private val api: OpenLibraryApi, private val bookDao: BookDao) :
    BooksRepository {

    companion object {
        private const val TAG = "BooksRepositoryImpl"
        private const val BOOKS_PER_SHELF = 100
        private const val COVER_BASE_URL = "https://covers.openlibrary.org/b/id"
        private const val COVER_SIZE = "M" // Medium size covers
        private const val UNKNOWN_AUTHOR = "Unknown Author"

        // Shelf names for the Open Library API
        private const val SHELF_WANT_TO_READ = "want-to-read"
        private const val SHELF_CURRENTLY_READING = "currently-reading"
        private const val SHELF_ALREADY_READ = "already-read"
    }

    /**
     * Synchronise books from all three shelves for the specified user.
     *
     * Fetches Want to Read, Currently Reading, and Already Read shelves in parallel
     * using RxJava zip. Maps DTOs to domain models, generates composite keys,
     * and stores them in the local database.
     *
     * @param username Open Library username
     * @return Single emitting the complete list of fetched books
     */
    override fun syncBooks(username: String): Single<List<Book>> {
        Log.i(TAG, "Starting book synchronisation for user: $username")
        return Single.zip(
            fetchShelf(username, SHELF_WANT_TO_READ, ReadingStatus.WantToRead),
            fetchShelf(username, SHELF_CURRENTLY_READING, ReadingStatus.CurrentlyReading),
            fetchShelf(username, SHELF_ALREADY_READ, ReadingStatus.AlreadyRead),
        ) { wantToRead, currentlyReading, alreadyRead ->
            wantToRead + currentlyReading + alreadyRead
        }
            .subscribeOn(Schedulers.io())
            .flatMap { books ->
                // Store books in database
                val entities = books.map { it.toBookEntity() }
                bookDao.insertAllRx(entities)
                    .andThen(Single.just(books))
            }
            .doOnSuccess { books ->
                Log.i(TAG, "Successfully synchronised ${books.size} books for user: $username")
            }
            .doOnError { error ->
                Log.e(TAG, "Error syncing books for user: $username", error)
            }
    }

    /**
     * Fetch books from a specific shelf.
     *
     * Retrieves up to BOOKS_PER_SHELF books from the specified shelf,
     * maps them to domain models, and applies cover fallback logic.
     *
     * @param username Open Library username
     * @param shelf Shelf name (want-to-read, currently-reading, already-read)
     * @param status Reading status to assign to books
     * @return Single emitting list of books from the shelf
     */
    private fun fetchShelf(username: String, shelf: String, status: ReadingStatus): Single<List<Book>> {
        Log.d(TAG, "Fetching shelf '$shelf' for user: $username")
        return api.getReadingList(username, shelf, page = 1)
            .subscribeOn(Schedulers.io())
            .map { response ->
                val books = response.readingLogEntries
                    ?.take(BOOKS_PER_SHELF)
                    ?.mapNotNull { entry ->
                        try {
                            mapEntryToBook(entry, status)
                        } catch (e: IllegalArgumentException) {
                            Log.w(TAG, "Failed to parse book entry: ${e.message}", e)
                            null // Skip malformed entries
                        }
                    }
                    ?: emptyList()
                Log.i(TAG, "Fetched ${books.size} books from shelf '$shelf' for user: $username")
                books
            }
            .onErrorReturn { error ->
                Log.e(TAG, "Error fetching shelf $shelf: ${error.message}")
                emptyList()
            }
    }

    /**
     * Map a reading list entry DTO to a domain Book model.
     *
     * Generates a composite key from title and first author, applies cover
     * fallback logic, and handles missing data gracefully.
     *
     * @param entry The reading list entry DTO
     * @param status Reading status to assign
     * @return Book domain model
     */
    private fun mapEntryToBook(entry: ReadingListEntryDto, status: ReadingStatus): Book {
        val work = entry.work
        val title = work?.title?.takeIf { it.isNotBlank() } ?: "Untitled"
        val authors = work?.authorNames?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
            ?: listOf(UNKNOWN_AUTHOR)

        // Generate composite key: sanitise title and first author
        val compositeKey = generateCompositeKey(title, authors.first())

        // Apply cover fallback logic
        val coverUrl = resolveCoverUrl(work?.coverId)

        return Book(
            id = compositeKey,
            title = title,
            authors = authors,
            coverUrl = coverUrl,
            publishYear = work?.firstPublishYear,
            description = null, // Not available in reading list entries
            subjects = emptyList(), // Not available in reading list entries
            readingStatus = status,
            isFavorite = false,
            workKey = work?.key,
            editionKey = entry.loggedEdition,
            dateAdded = parseLoggedDate(),
        )
    }

    /**
     * Resolve the cover URL with fallback logic.
     *
     * Priority:
     * 1. Use cover_i from work if available
     * 2. Fallback to null (UI will generate placeholder)
     *
     * Note: Edition cover fallback would require an additional API call,
     * which is avoided for performance. The placeholder provides a good
     * user experience for books without covers.
     *
     * @param coverId Cover ID from work
     * @return Cover URL or null for placeholder
     */
    private fun resolveCoverUrl(coverId: Int?): String? = if (coverId != null && coverId > 0) {
        "$COVER_BASE_URL/$coverId-$COVER_SIZE.jpg"
    } else {
        // Return null to indicate placeholder should be used
        // The UI layer will generate the placeholder with title initials
        null
    }

    /**
     * Generate a composite key from title and first author.
     *
     * Sanitises strings by removing special characters and converting to lowercase.
     * Format: "title_author"
     *
     * @param title Book title
     * @param firstAuthor First author name
     * @return Composite key
     */
    private fun generateCompositeKey(title: String, firstAuthor: String): String {
        val sanitisedTitle = title.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(50)
        val sanitisedAuthor = firstAuthor.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(30)
        return "${sanitisedTitle}_$sanitisedAuthor"
    }

    /**
     * Parse logged date string to timestamp.
     *
     * Falls back to current time if parsing fails.
     *
     * @return Timestamp in milliseconds
     */
    @Suppress("UnusedPrivateMember") // Reserved for future date parsing implementation
    private fun parseLoggedDate(): Long {
        // For now, use current timestamp as the API date format may vary
        // In production, would parse ISO 8601 format from the logged_date field
        return System.currentTimeMillis()
    }

    /**
     * Observe all cached books from the local database.
     *
     * Implements offline-first strategy by always returning cached data first.
     * The UI can trigger sync separately to refresh data.
     *
     * @return Flow emitting the list of all cached books
     */
    override fun getBooks(): Flow<List<Book>> = bookDao.getAllBooks()
        .map { entities ->
            entities.map { it.toBook() }
        }
        .subscribeOn(Schedulers.io())
        .toObservable()
        .asFlow()

    /**
     * Observe cached books filtered by reading status.
     *
     * @param status The reading status to filter by
     * @return Flow emitting the filtered list of books
     */
    override fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>> = bookDao.getBooksByStatus(status)
        .map { entities ->
            entities.map { it.toBook() }
        }
        .subscribeOn(Schedulers.io())
        .toObservable()
        .asFlow()

    /**
     * Observe a single book by its unique identifier.
     *
     * @param bookId Unique identifier for the book (composite key)
     * @return Flow emitting the book, or null if not found
     */
    override fun getBookById(bookId: String): Flow<Book?> = bookDao.getBookByKey(bookId)
        .map { it.toBook() }
        .toObservable()
        .cast(Book::class.java)
        .subscribeOn(Schedulers.io())
        .asFlow()

    /**
     * Fetch detailed work information from the Open Library API.
     *
     * Retrieves comprehensive metadata about a work. Results should be
     * cached locally in a production implementation.
     *
     * @param workKey Open Library work key (e.g., "/works/OL27448W")
     * @return Single emitting the detailed work information
     */
    override fun getWorkDetails(workKey: String): Single<WorkDetails> {
        // Extract key without the "/works/" prefix
        val key = workKey.removePrefix("/works/")
        Log.i(TAG, "Fetching work details for key: $workKey")

        return api.getWork(key)
            .subscribeOn(Schedulers.io())
            .map { it.toDomain() }
            .doOnSuccess { workDetails ->
                Log.i(TAG, "Successfully fetched work details for key: $workKey (title: ${workDetails.title})")
            }
            .doOnError { error ->
                Log.e(TAG, "Error fetching work details for $workKey", error)
            }
    }

    /**
     * Fetch detailed edition information from the Open Library API.
     *
     * Retrieves specific edition details. Results should be cached locally
     * in a production implementation.
     *
     * @param editionKey Open Library edition key (e.g., "/books/OL7353617M")
     * @return Single emitting the detailed edition information
     */
    override fun getEditionDetails(editionKey: String): Single<EditionDetails> {
        // Extract key without the "/books/" prefix
        val key = editionKey.removePrefix("/books/")
        Log.i(TAG, "Fetching edition details for key: $editionKey")

        return api.getEdition(key)
            .subscribeOn(Schedulers.io())
            .map { it.toDomain() }
            .doOnSuccess { editionDetails ->
                Log.i(TAG, "Successfully fetched edition details for key: $editionKey (title: ${editionDetails.title})")
            }
            .doOnError { error ->
                Log.e(TAG, "Error fetching edition details for $editionKey", error)
            }
    }

    /**
     * Synchronise all reading list shelves from the API to the local cache.
     *
     * This is the primary sync operation that fetches fresh data and updates
     * the local database. Follows offline-first strategy by allowing the UI
     * to continue displaying cached data even if sync fails.
     *
     * @param username Open Library username to sync reading lists for
     * @return Completable that completes when sync is finished
     */
    override fun sync(username: String): Completable {
        Log.i(TAG, "Starting synchronisation for user: $username")
        return syncBooks(username)
            .doOnSuccess {
                Log.i(TAG, "Synchronisation completed successfully for user: $username")
            }
            .ignoreElement()
            .onErrorComplete { error ->
                Log.e(TAG, "Sync failed for user $username, keeping cached data", error)
                true // Complete anyway to prevent error propagation
            }
    }

    /**
     * Clear all cached books from the local database.
     *
     * This does not affect the user's reading lists on Open Library.
     *
     * @return Completable that completes when cache is cleared
     */
    override fun clearCache(): Completable {
        Log.i(TAG, "Clearing all cached books from local database")
        return bookDao.deleteAll()
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                Log.i(TAG, "Successfully cleared all cached books")
            }
            .doOnError { error ->
                Log.e(TAG, "Failed to clear cached books", error)
            }
    }
}
