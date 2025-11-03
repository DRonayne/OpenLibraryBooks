package com.darach.openlibrarybooks.core.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.darach.openlibrarybooks.core.database.AppDatabase
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestBook(
        compositeKey: String = "the_hobbit_j.r.r._tolkien",
        title: String = "The Hobbit",
        authors: List<String> = listOf("J.R.R. Tolkien"),
        readingStatus: ReadingStatus = ReadingStatus.WantToRead,
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
        readingStatus = readingStatus,
        workKey = "/works/OL45804W",
        editionKey = "/books/OL7353617M",
        dateAdded = dateAdded,
        lastUpdated = dateAdded,
    )

    // Insert Tests

    @Test
    fun insertBook_insertsSuccessfully() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val retrieved = bookDao.getBookByKey(book.compositeKey).blockingFirst()
        assertEquals(book.title, retrieved.title)
        assertEquals(book.authors, retrieved.authors)
    }

    @Test
    fun insertBook_replacesExistingBookWithSameKey() = runTest {
        val book1 = createTestBook(title = "Original Title")
        val book2 = createTestBook(title = "Updated Title")

        bookDao.insert(book1)
        bookDao.insert(book2)

        val allBooks = bookDao.getAllBooks().blockingFirst()
        assertEquals(1, allBooks.size)
        assertEquals("Updated Title", allBooks[0].title)
    }

    @Test
    fun insertAllBooks_insertsMultipleBooks() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
        )

        bookDao.insertAll(books)

        val retrieved = bookDao.getAllBooks().blockingFirst()
        assertEquals(3, retrieved.size)
    }

    // Get Tests

    @Test
    fun getAllBooks_returnsEmptyListWhenNoBooks() = runTest {
        val books = bookDao.getAllBooks().blockingFirst()
        assertEquals(0, books.size)
    }

    @Test
    fun getAllBooks_returnsAllBooksOrderedByDateAdded() = runTest {
        val now = System.currentTimeMillis()
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1", dateAdded = now),
            createTestBook(compositeKey = "book2", title = "Book 2", dateAdded = now + 1000),
            createTestBook(compositeKey = "book3", title = "Book 3", dateAdded = now + 2000),
        )

        bookDao.insertAll(books)

        val retrieved = bookDao.getAllBooks().blockingFirst()
        assertEquals(3, retrieved.size)
        // Should be ordered by date added DESC (newest first)
        assertEquals("Book 3", retrieved[0].title)
        assertEquals("Book 2", retrieved[1].title)
        assertEquals("Book 1", retrieved[2].title)
    }

    @Test
    fun getBooksByStatus_returnsOnlyBooksWithMatchingStatus() = runTest {
        val books = listOf(
            createTestBook(
                compositeKey = "book1",
                title = "Want to Read Book",
                readingStatus = ReadingStatus.WantToRead,
            ),
            createTestBook(
                compositeKey = "book2",
                title = "Currently Reading Book",
                readingStatus = ReadingStatus.CurrentlyReading,
            ),
            createTestBook(
                compositeKey = "book3",
                title = "Already Read Book",
                readingStatus = ReadingStatus.AlreadyRead,
            ),
        )

        bookDao.insertAll(books)

        val wantToReadBooks = bookDao.getBooksByStatus(ReadingStatus.WantToRead).blockingFirst()
        assertEquals(1, wantToReadBooks.size)
        assertEquals("Want to Read Book", wantToReadBooks[0].title)

        val currentlyReadingBooks =
            bookDao.getBooksByStatus(ReadingStatus.CurrentlyReading).blockingFirst()
        assertEquals(1, currentlyReadingBooks.size)
        assertEquals("Currently Reading Book", currentlyReadingBooks[0].title)
    }

    @Test
    fun getBookByKey_returnsCorrectBook() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val retrieved = bookDao.getBookByKey(book.compositeKey).blockingFirst()
        assertEquals(book.compositeKey, retrieved.compositeKey)
    }

    @Test
    fun getBookByWorkKey_returnsCorrectBook() = runTest {
        val book = createTestBook(workKey = "/works/OL12345W")
        bookDao.insert(book)

        val retrieved = bookDao.getBookByWorkKey("/works/OL12345W").blockingFirst()
        assertEquals(book.workKey, retrieved.workKey)
    }

    // Search Tests

    @Test
    fun searchBooks_findsByTitle() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "The Hobbit"),
            createTestBook(compositeKey = "book2", title = "The Lord of the Rings"),
            createTestBook(compositeKey = "book3", title = "1984"),
        )

        bookDao.insertAll(books)

        val results = bookDao.searchBooks("Hobbit").blockingFirst()
        assertEquals(1, results.size)
        assertEquals("The Hobbit", results[0].title)
    }

    @Test
    fun searchBooks_findsByAuthor() = runTest {
        val books = listOf(
            createTestBook(
                compositeKey = "book1",
                title = "The Hobbit",
                authors = listOf("J.R.R. Tolkien"),
            ),
            createTestBook(compositeKey = "book2", title = "1984", authors = listOf("George Orwell")),
        )

        bookDao.insertAll(books)

        val results = bookDao.searchBooks("Tolkien").blockingFirst()
        assertEquals(1, results.size)
        assertEquals("The Hobbit", results[0].title)
    }

    @Test
    fun searchBooks_returnsEmptyListWhenNoMatches() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val results = bookDao.searchBooks("NonexistentBook").blockingFirst()
        assertEquals(0, results.size)
    }

    // Count Tests

    @Test
    fun getBooksCount_returnsCorrectCount() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
        )

        bookDao.insertAll(books)

        val count = bookDao.getBooksCount().blockingGet()
        assertEquals(3, count)
    }

    @Test
    fun getBooksCountByStatus_returnsCorrectCount() = runTest {
        val books = listOf(
            createTestBook(
                compositeKey = "book1",
                title = "Book 1",
                readingStatus = ReadingStatus.WantToRead,
            ),
            createTestBook(
                compositeKey = "book2",
                title = "Book 2",
                readingStatus = ReadingStatus.WantToRead,
            ),
            createTestBook(
                compositeKey = "book3",
                title = "Book 3",
                readingStatus = ReadingStatus.CurrentlyReading,
            ),
        )

        bookDao.insertAll(books)

        val wantToReadCount = bookDao.getBooksCountByStatus(ReadingStatus.WantToRead).blockingGet()
        assertEquals(2, wantToReadCount)

        val currentlyReadingCount =
            bookDao.getBooksCountByStatus(ReadingStatus.CurrentlyReading).blockingGet()
        assertEquals(1, currentlyReadingCount)
    }

    // Update Tests

    @Test
    fun updateBook_updatesSuccessfully() = runTest {
        val book = createTestBook()
        bookDao.insert(book)

        val updatedBook = book.copy(title = "Updated Title")
        bookDao.update(updatedBook)

        val retrieved = bookDao.getBookByKey(book.compositeKey).blockingFirst()
        assertEquals("Updated Title", retrieved.title)
    }

    @Test
    fun updateReadingStatus_updatesSuccessfully() = runTest {
        val book = createTestBook(readingStatus = ReadingStatus.WantToRead)
        bookDao.insert(book)

        val updateTime = System.currentTimeMillis()
        bookDao.updateReadingStatus(book.compositeKey, ReadingStatus.CurrentlyReading, updateTime)
            .blockingAwait()

        val retrieved = bookDao.getBookByKey(book.compositeKey).blockingFirst()
        assertEquals(ReadingStatus.CurrentlyReading, retrieved.readingStatus)
        assertEquals(updateTime, retrieved.lastUpdated)
    }

    // Delete Tests

    @Test
    fun deleteByKey_deletesCorrectBook() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
        )

        bookDao.insertAll(books)
        bookDao.deleteByKey("book1")

        val remaining = bookDao.getAllBooks().blockingFirst()
        assertEquals(1, remaining.size)
        assertEquals("Book 2", remaining[0].title)
    }

    @Test
    fun deleteAll_deletesAllBooks() = runTest {
        val books = listOf(
            createTestBook(compositeKey = "book1", title = "Book 1"),
            createTestBook(compositeKey = "book2", title = "Book 2"),
            createTestBook(compositeKey = "book3", title = "Book 3"),
        )

        bookDao.insertAll(books)
        bookDao.deleteAll().blockingAwait()

        val remaining = bookDao.getAllBooks().blockingFirst()
        assertEquals(0, remaining.size)
    }
}
