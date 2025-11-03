package com.darach.openlibrarybooks.feature.widget

import com.darach.openlibrarybooks.core.database.dao.FavouritesDao
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WidgetRepository.
 * Verifies that the repository correctly fetches and maps the 3 most recent favourite books.
 */
class WidgetRepositoryTest {

    private lateinit var favouritesDao: FavouritesDao
    private lateinit var widgetRepository: WidgetRepository

    @Before
    fun setup() {
        favouritesDao = mockk()
        widgetRepository = WidgetRepository(favouritesDao)
    }

    @Test
    fun `getRecentFavourites should fetch 3 books from dao`() {
        // Given
        val bookEntities = listOf(
            createBookEntity("1", "The Hobbit"),
            createBookEntity("2", "1984"),
            createBookEntity("3", "Dune"),
        )
        every { favouritesDao.getRecentFavouriteBooks(3) } returns Flowable.just(bookEntities)

        // When
        val testObserver = widgetRepository.getRecentFavourites().test()

        // Then
        verify { favouritesDao.getRecentFavouriteBooks(3) }
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        result shouldHaveSize 3
        result[0].title shouldBe "The Hobbit"
        result[1].title shouldBe "1984"
        result[2].title shouldBe "Dune"
    }

    @Test
    fun `getRecentFavourites should mark all books as favourites`() {
        // Given
        val bookEntities = listOf(createBookEntity("1", "The Hobbit"))
        every { favouritesDao.getRecentFavouriteBooks(3) } returns Flowable.just(bookEntities)

        // When
        val testObserver = widgetRepository.getRecentFavourites().test()

        // Then
        val result = testObserver.values()[0]
        result.all { it.isFavorite } shouldBe true
    }

    @Test
    fun `getRecentFavourites should handle empty list`() {
        // Given
        every { favouritesDao.getRecentFavouriteBooks(3) } returns Flowable.just(emptyList())

        // When
        val testObserver = widgetRepository.getRecentFavourites().test()

        // Then
        testObserver.assertNoErrors()
        val result = testObserver.values()[0]
        result shouldHaveSize 0
    }

    @Test
    fun `getRecentFavourites should map BookEntity fields correctly`() {
        // Given
        val bookEntity = BookEntity(
            compositeKey = "test-key",
            title = "Test Book",
            authors = listOf("Author One", "Author Two"),
            coverImageId = 12345,
            coverUrl = "https://covers.openlibrary.org/b/id/12345-L.jpg",
            firstPublishYear = 2020,
            description = "Test description",
            subjects = listOf("Fiction", "Science"),
            readingStatus = ReadingStatus.WantToRead,
            workKey = "/works/OL123W",
            editionKey = "/books/OL456M",
            dateAdded = 1234567890L,
            lastUpdated = 1234567890L,
        )
        every { favouritesDao.getRecentFavouriteBooks(3) } returns Flowable.just(listOf(bookEntity))

        // When
        val testObserver = widgetRepository.getRecentFavourites().test()

        // Then
        val result = testObserver.values()[0][0]
        result.id shouldBe "test-key"
        result.title shouldBe "Test Book"
        result.authors shouldBe listOf("Author One", "Author Two")
        result.coverUrl shouldBe "https://covers.openlibrary.org/b/id/12345-L.jpg"
        result.publishYear shouldBe 2020
        result.description shouldBe "Test description"
        result.subjects shouldBe listOf("Fiction", "Science")
        result.readingStatus shouldBe ReadingStatus.WantToRead
        result.isFavorite shouldBe true
        result.workKey shouldBe "/works/OL123W"
        result.editionKey shouldBe "/books/OL456M"
        result.dateAdded shouldBe 1234567890L
    }

    @Test
    fun `getRecentFavourites should propagate dao errors`() {
        // Given
        val exception = RuntimeException("Database error")
        every { favouritesDao.getRecentFavouriteBooks(3) } returns Flowable.error(exception)

        // When
        val testObserver = widgetRepository.getRecentFavourites().test()

        // Then
        testObserver.assertError(exception)
    }

    private fun createBookEntity(id: String, title: String) = BookEntity(
        compositeKey = id,
        title = title,
        authors = listOf("Test Author"),
        coverImageId = null,
        coverUrl = null,
        firstPublishYear = 2020,
        description = "Test description",
        subjects = emptyList(),
        readingStatus = ReadingStatus.WantToRead,
        workKey = "/works/OL123W",
        editionKey = "/books/OL456M",
        dateAdded = System.currentTimeMillis(),
        lastUpdated = System.currentTimeMillis(),
    )
}
