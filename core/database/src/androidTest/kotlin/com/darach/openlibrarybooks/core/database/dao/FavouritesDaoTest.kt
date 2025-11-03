package com.darach.openlibrarybooks.core.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.darach.openlibrarybooks.core.database.AppDatabase
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.database.entity.FavouritesEntity
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouritesDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var bookDao: BookDao
    private lateinit var favouritesDao: FavouritesDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        bookDao = database.bookDao()
        favouritesDao = database.favouritesDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestBook(
        compositeKey: String = "the_hobbit_j.r.r._tolkien",
        title: String = "The Hobbit",
        authors: List<String> = listOf("J.R.R. Tolkien"),
        dateAdded: Long = System.currentTimeMillis(),
    ) = BookEntity(
        compositeKey = compositeKey,
        title = title,
        authors = authors,
        coverImageId = 12345,
        coverUrl = "https://covers.openlibrary.org/b/id/12345-L.jpg",
        firstPublishYear = 1937,
        description = "A fantasy adventure novel",
        subjects = listOf("Fantasy", "Adventure"),
        readingStatus = ReadingStatus.WantToRead,
        workKey = "/works/OL45804W",
        editionKey = "/books/OL7353617M",
        dateAdded = dateAdded,
        lastUpdated = dateAdded,
    )

    // Insert Tests

    @Test
    fun insertFavourite_insertsSuccessfully() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val favourite = FavouritesEntity(
            bookCompositeKey = book.compositeKey,
            dateAdded = System.currentTimeMillis(),
        )
        favouritesDao.insertFavourite(favourite)

        val isFav = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertTrue(isFav)
    }

    @Test
    fun insertFavourite_replacesExistingFavourite() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val now = System.currentTimeMillis()
        val favourite1 = FavouritesEntity(bookCompositeKey = book.compositeKey, dateAdded = now)
        val favourite2 = FavouritesEntity(bookCompositeKey = book.compositeKey, dateAdded = now + 1000)

        favouritesDao.insertFavourite(favourite1)
        favouritesDao.insertFavourite(favourite2)

        val count = favouritesDao.getFavouritesCount().blockingGet()
        assertEquals(1, count) // Should still be 1 due to unique constraint and replace strategy
    }

    // Get Tests

    @Test
    fun getAllFavouriteBooks_returnsEmptyListWhenNoFavourites() = runTest {
        val favourites = favouritesDao.getAllFavouriteBooks().blockingFirst()
        assertEquals(0, favourites.size)
    }

    @Test
    fun getAllFavouriteBooks_returnsAllFavouriteBooksWithJoin() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
        )
        bookDao.insertAll(books)

        // Favourite only book1 and book2
        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = "book1",
                dateAdded = System.currentTimeMillis(),
            ),
        )
        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = "book2",
                dateAdded = System.currentTimeMillis() + 1000,
            ),
        )

        val favouriteBooks = favouritesDao.getAllFavouriteBooks().blockingFirst()
        assertEquals(2, favouriteBooks.size)
        // Should be ordered by date added DESC (newest first)
        assertEquals("Book 2", favouriteBooks[0].title)
        assertEquals("Book 1", favouriteBooks[1].title)
    }

    @Test
    fun getRecentFavouriteBooks_returnsLimitedResults() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
            createTestBook(compositeKey = "book4", title = "Book 4"),
        )
        bookDao.insertAll(books)

        val now = System.currentTimeMillis()
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book1", dateAdded = now))
        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = "book2",
                dateAdded = now + 1000,
            ),
        )
        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = "book3",
                dateAdded = now + 2000,
            ),
        )
        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = "book4",
                dateAdded = now + 3000,
            ),
        )

        val recentFavourites = favouritesDao.getRecentFavouriteBooks(limit = 2).blockingFirst()
        assertEquals(2, recentFavourites.size)
        // Should return the 2 most recent
        assertEquals("Book 4", recentFavourites[0].title)
        assertEquals("Book 3", recentFavourites[1].title)
    }

    // Check Tests

    @Test
    fun isFavourite_returnsTrueWhenBookIsFavourited() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = book.compositeKey,
                dateAdded = System.currentTimeMillis(),
            ),
        )

        val isFav = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertTrue(isFav)
    }

    @Test
    fun isFavourite_returnsFalseWhenBookIsNotFavourited() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val isFav = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertFalse(isFav)
    }

    // Count Tests

    @Test
    fun getFavouritesCount_returnsCorrectCount() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
        )
        bookDao.insertAll(books)

        val now = System.currentTimeMillis()
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book1", dateAdded = now))
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book2", dateAdded = now))

        val count = favouritesDao.getFavouritesCount().blockingGet()
        assertEquals(2, count)
    }

    @Test
    fun getFavouritesCount_returnsZeroWhenNoFavourites() = runTest {
        val count = favouritesDao.getFavouritesCount().blockingGet()
        assertEquals(0, count)
    }

    // Delete Tests

    @Test
    fun deleteFavourite_removesCorrectFavourite() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
        )
        bookDao.insertAll(books)

        val now = System.currentTimeMillis()
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book1", dateAdded = now))
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book2", dateAdded = now))

        favouritesDao.deleteFavourite("book1")

        val isFav1 = favouritesDao.isFavourite("book1").blockingFirst()
        val isFav2 = favouritesDao.isFavourite("book2").blockingFirst()

        assertFalse(isFav1)
        assertTrue(isFav2)
    }

    @Test
    fun deleteAllFavourites_removesAllFavourites() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
        )
        bookDao.insertAll(books)

        val now = System.currentTimeMillis()
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book1", dateAdded = now))
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book2", dateAdded = now))
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book3", dateAdded = now))

        favouritesDao.deleteAllFavourites().blockingAwait()

        val count = favouritesDao.getFavouritesCount().blockingGet()
        assertEquals(0, count)
    }

    @Test
    fun deleteAllFavourites_doesNotDeleteBooks() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
        )
        bookDao.insertAll(books)

        val now = System.currentTimeMillis()
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book1", dateAdded = now))
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = "book2", dateAdded = now))

        favouritesDao.deleteAllFavourites().blockingAwait()

        val booksRemaining = bookDao.getAllBooks().blockingFirst()
        assertEquals(2, booksRemaining.size) // Books should still exist
    }

    // Foreign Key Cascade Tests

    @Test
    fun deletingBook_cascadesDeleteFavourite() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        favouritesDao.insertFavourite(
            FavouritesEntity(
                bookCompositeKey = book.compositeKey,
                dateAdded = System.currentTimeMillis(),
            ),
        )

        // Verify favourite exists
        val isFavBefore = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertTrue(isFavBefore)

        // Delete the book
        bookDao.deleteByKey(book.compositeKey)

        // Favourite should be automatically deleted due to cascade
        val favouritesCount = favouritesDao.getFavouritesCount().blockingGet()
        assertEquals(0, favouritesCount)
    }

    // Toggle Tests

    @Test
    fun toggleFavourite_addsWhenNotFavourited() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        favouritesDao.toggleFavourite(book.compositeKey, System.currentTimeMillis())

        val isFav = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertTrue(isFav)
    }

    @Test
    fun toggleFavourite_removesWhenAlreadyFavourited() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val now = System.currentTimeMillis()
        favouritesDao.insertFavourite(FavouritesEntity(bookCompositeKey = book.compositeKey, dateAdded = now))

        // Verify it's favourited
        val isFavBefore = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertTrue(isFavBefore)

        // Toggle should remove it
        favouritesDao.toggleFavourite(book.compositeKey, now)

        val isFavAfter = favouritesDao.isFavourite(book.compositeKey).blockingFirst()
        assertFalse(isFavAfter)
    }

    @Test
    fun toggleFavourite_worksMultipleTimes() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val now = System.currentTimeMillis()

        // Toggle on
        favouritesDao.toggleFavourite(book.compositeKey, now)
        assertTrue(favouritesDao.isFavourite(book.compositeKey).blockingFirst())

        // Toggle off
        favouritesDao.toggleFavourite(book.compositeKey, now)
        assertFalse(favouritesDao.isFavourite(book.compositeKey).blockingFirst())

        // Toggle on again
        favouritesDao.toggleFavourite(book.compositeKey, now)
        assertTrue(favouritesDao.isFavourite(book.compositeKey).blockingFirst())
    }
}
