package com.darach.openlibrarybooks.core.data.repository

import com.darach.openlibrarybooks.core.database.dao.BookDao
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.network.api.OpenLibraryApi
import com.darach.openlibrarybooks.core.network.dto.EditionDto
import com.darach.openlibrarybooks.core.network.dto.ReadingListEntryDto
import com.darach.openlibrarybooks.core.network.dto.ReadingListResponseDto
import com.darach.openlibrarybooks.core.network.dto.WorkDto
import com.darach.openlibrarybooks.core.network.dto.WorkReferenceDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BooksRepositoryImpl.
 *
 * Tests cover:
 * - Parallel shelf fetching with RxJava zip
 * - Offline-first strategy
 * - Cover fallback logic
 * - Edge case handling (no authors, malformed data, duplicates)
 * - Helper methods (getBooks, sync, clearCache, getWorkDetails, getEditionDetails)
 */
class BooksRepositoryImplTest {

    private lateinit var repository: BooksRepositoryImpl
    private lateinit var mockApi: OpenLibraryApi
    private lateinit var mockBookDao: BookDao

    @Before
    fun setup() {
        // Override RxJava schedulers to use trampoline for synchronous testing
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }

        mockApi = mockk()
        mockBookDao = mockk()
        repository = BooksRepositoryImpl(mockApi, mockBookDao)
    }

    @After
    fun tearDown() {
        // Reset RxJava plugins after tests
        RxJavaPlugins.reset()
    }

    @Test
    fun `syncBooks fetches all three shelves in parallel and stores in database`() {
        // Given
        val username = "testuser"
        val wantToReadResponse = createMockReadingListResponse(
            listOf(createMockEntry("Book 1", listOf("Author 1"), 123)),
        )
        val currentlyReadingResponse = createMockReadingListResponse(
            listOf(createMockEntry("Book 2", listOf("Author 2"), 456)),
        )
        val alreadyReadResponse = createMockReadingListResponse(
            listOf(createMockEntry("Book 3", listOf("Author 3"), 789)),
        )

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(wantToReadResponse)
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns Single.just(currentlyReadingResponse)
        every { mockApi.getReadingList(username, "already-read", 1) } returns Single.just(alreadyReadResponse)
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val books = testObserver.values().first()
        assert(books.size == 3)
        assert(books[0].title == "Book 1")
        assert(books[0].readingStatus == ReadingStatus.WantToRead)
        assert(books[1].title == "Book 2")
        assert(books[1].readingStatus == ReadingStatus.CurrentlyReading)
        assert(books[2].title == "Book 3")
        assert(books[2].readingStatus == ReadingStatus.AlreadyRead)
        verify { mockBookDao.insertAllRx(any()) }
    }

    @Test
    fun `syncBooks handles empty shelves gracefully`() {
        // Given
        val username = "testuser"
        val emptyResponse = ReadingListResponseDto(page = 1, readingLogEntries = emptyList())

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(emptyResponse)
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns Single.just(emptyResponse)
        every { mockApi.getReadingList(username, "already-read", 1) } returns Single.just(emptyResponse)
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val books = testObserver.values().first()
        assert(books.isEmpty())
    }

    @Test
    fun `syncBooks handles null reading log entries`() {
        // Given
        val username = "testuser"
        val responseWithNullEntries = ReadingListResponseDto(page = 1, readingLogEntries = null)

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(responseWithNullEntries)
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns Single.just(responseWithNullEntries)
        every { mockApi.getReadingList(username, "already-read", 1) } returns Single.just(responseWithNullEntries)
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val books = testObserver.values().first()
        assert(books.isEmpty())
    }

    @Test
    fun `syncBooks uses Unknown Author when author names are missing`() {
        // Given
        val username = "testuser"
        val entryWithoutAuthors = createMockEntry("Book Without Authors", emptyList(), 123)
        val response = createMockReadingListResponse(listOf(entryWithoutAuthors))

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(response)
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns
            Single.just(createMockReadingListResponse(emptyList()))
        every { mockApi.getReadingList(username, "already-read", 1) } returns
            Single.just(createMockReadingListResponse(emptyList()))
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        val books = testObserver.values().first()
        assert(books.size == 1)
        assert(books[0].authors == listOf("Unknown Author"))
    }

    @Test
    fun `syncBooks applies cover URL when cover ID is present`() {
        // Given
        val username = "testuser"
        val entry = createMockEntry("Book With Cover", listOf("Author"), 12345)
        val response = createMockReadingListResponse(listOf(entry))

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(response)
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns
            Single.just(createMockReadingListResponse(emptyList()))
        every { mockApi.getReadingList(username, "already-read", 1) } returns
            Single.just(createMockReadingListResponse(emptyList()))
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        val books = testObserver.values().first()
        assert(books.size == 1)
        assert(books[0].coverUrl == "https://covers.openlibrary.org/b/id/12345-M.jpg")
    }

    @Test
    fun `syncBooks returns null cover URL when cover ID is missing for placeholder`() {
        // Given
        val username = "testuser"
        val entry = createMockEntry("Book Without Cover", listOf("Author"), null)
        val response = createMockReadingListResponse(listOf(entry))

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(response)
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns
            Single.just(createMockReadingListResponse(emptyList()))
        every { mockApi.getReadingList(username, "already-read", 1) } returns
            Single.just(createMockReadingListResponse(emptyList()))
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        val books = testObserver.values().first()
        assert(books.size == 1)
        assert(books[0].coverUrl == null) // UI will generate placeholder with initials
    }

    @Test
    fun `syncBooks handles API error for individual shelf and continues`() {
        // Given
        val username = "testuser"
        val validResponse = createMockReadingListResponse(
            listOf(createMockEntry("Book 1", listOf("Author 1"), 123)),
        )

        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.error(Exception("Network error"))
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns Single.just(validResponse)
        every { mockApi.getReadingList(username, "already-read", 1) } returns Single.just(validResponse)
        every { mockBookDao.insertAllRx(any()) } returns Completable.complete()

        // When
        val testObserver = repository.syncBooks(username).test()

        // Then
        testObserver.assertComplete()
        val books = testObserver.values().first()
        assert(books.size == 2) // Only two shelves succeeded
    }

    @Test
    fun `getBooks returns flow of cached books from database`() {
        // Given
        val mockBookWithFavorites = listOf(
            createMockBookWithFavorite("Book 1", ReadingStatus.WantToRead, isFavorite = false),
            createMockBookWithFavorite("Book 2", ReadingStatus.CurrentlyReading, isFavorite = true),
        )
        every { mockBookDao.getAllBooksWithFavorites() } returns Flowable.just(mockBookWithFavorites)

        // When
        repository.getBooks()

        // Then
        // Flow is tested by collecting values (would use turbine in production)
        verify { mockBookDao.getAllBooksWithFavorites() }
    }

    @Test
    fun `getBooksByStatus returns filtered books from database`() {
        // Given
        val status = ReadingStatus.WantToRead
        val mockEntities = listOf(
            createMockBookEntity("Book 1", status),
        )
        every { mockBookDao.getBooksByStatus(status) } returns Flowable.just(mockEntities)

        // When
        repository.getBooksByStatus(status)

        // Then
        verify { mockBookDao.getBooksByStatus(status) }
    }

    @Test
    fun `getWorkDetails fetches work from API and maps to domain model`() {
        // Given
        val workKey = "/works/OL27448W"
        val mockWorkDto = WorkDto(
            key = workKey,
            title = "Test Work",
            description = null,
            covers = listOf(12345),
            subjects = listOf("Fiction", "Adventure"),
            authors = null,
            firstPublishDate = "1990",
            excerpts = null,
            links = null,
        )
        every { mockApi.getWork("OL27448W") } returns Single.just(mockWorkDto)

        // When
        val testObserver = repository.getWorkDetails(workKey).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val workDetails = testObserver.values().first()
        assert(workDetails.title == "Test Work")
        assert(workDetails.coverIds == listOf(12345))
        assert(workDetails.subjects == listOf("Fiction", "Adventure"))
    }

    @Test
    fun `getEditionDetails fetches edition from API and maps to domain model`() {
        // Given
        val editionKey = "/books/OL7353617M"
        val mockEditionDto = EditionDto(
            key = editionKey,
            title = "Test Edition",
            covers = listOf(67890),
            publishers = listOf("Test Publisher"),
            publishDate = "2020",
            numberOfPages = 350,
            isbn10 = listOf("1234567890"),
            isbn13 = listOf("1234567890123"),
            physicalFormat = "Hardcover",
            languages = null,
            weight = null,
            dimensions = null,
            works = null,
        )
        every { mockApi.getEdition("OL7353617M") } returns Single.just(mockEditionDto)

        // When
        val testObserver = repository.getEditionDetails(editionKey).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val editionDetails = testObserver.values().first()
        assert(editionDetails.title == "Test Edition")
        assert(editionDetails.numberOfPages == 350)
        assert(editionDetails.publishers == listOf("Test Publisher"))
    }

    @Test
    fun `clearCache deletes all books from database`() {
        // Given
        every { mockBookDao.deleteAll() } returns Completable.complete()

        // When
        val testObserver = repository.clearCache().test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify { mockBookDao.deleteAll() }
    }

    @Test
    fun `sync completes even when syncBooks fails to preserve offline-first strategy`() {
        // Given
        val username = "testuser"
        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.error(Exception("Network error"))
        every { mockApi.getReadingList(username, "currently-reading", 1) } returns
            Single.error(Exception("Network error"))
        every { mockApi.getReadingList(username, "already-read", 1) } returns Single.error(Exception("Network error"))

        // When
        val testObserver = repository.sync(username).test()

        // Then
        testObserver.assertComplete() // Should complete without error
        testObserver.assertNoErrors()
    }

    // Helper functions to create mock objects

    private fun createMockReadingListResponse(entries: List<ReadingListEntryDto>): ReadingListResponseDto =
        ReadingListResponseDto(
            page = 1,
            readingLogEntries = entries,
        )

    private fun createMockEntry(title: String, authorNames: List<String>, coverId: Int?): ReadingListEntryDto =
        ReadingListEntryDto(
            work = WorkReferenceDto(
                title = title,
                key = "/works/OL${title.hashCode()}W",
                authorKeys = null,
                authorNames = authorNames.takeIf { it.isNotEmpty() },
                firstPublishYear = 2020,
                coverId = coverId,
            ),
            loggedEdition = "/books/OL${title.hashCode()}M",
            loggedDate = "2024-01-01T00:00:00",
        )

    private fun createMockBookEntity(title: String, status: ReadingStatus): BookEntity = BookEntity(
        compositeKey = "${title.lowercase().replace(" ", "")}_author",
        title = title,
        authors = listOf("Test Author"),
        coverImageId = 12345,
        coverUrl = "https://covers.openlibrary.org/b/id/12345-M.jpg",
        firstPublishYear = 2020,
        description = "Test description",
        subjects = listOf("Fiction"),
        readingStatus = status,
        workKey = "/works/OL12345W",
        editionKey = "/books/OL67890M",
        dateAdded = System.currentTimeMillis(),
        lastUpdated = System.currentTimeMillis(),
    )

    private fun createMockBookWithFavorite(
        title: String,
        status: ReadingStatus,
        isFavorite: Boolean,
    ): com.darach.openlibrarybooks.core.database.entity.BookWithFavorite {
        val bookEntity = createMockBookEntity(title, status)
        return com.darach.openlibrarybooks.core.database.entity.BookWithFavorite(
            bookEntity = bookEntity,
            isFavorite = if (isFavorite) 1 else 0,
        )
    }
}
