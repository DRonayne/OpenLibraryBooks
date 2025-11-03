package com.darach.openlibrarybooks.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingStatusTest {

    @Test
    fun `ReadingStatus has three values`() {
        val values = ReadingStatus.entries
        assertEquals(3, values.size)
        assertTrue(values.contains(ReadingStatus.WantToRead))
        assertTrue(values.contains(ReadingStatus.CurrentlyReading))
        assertTrue(values.contains(ReadingStatus.AlreadyRead))
    }

    @Test
    fun `ReadingStatus valueOf returns correct enum`() {
        assertEquals(ReadingStatus.WantToRead, ReadingStatus.valueOf("WantToRead"))
        assertEquals(ReadingStatus.CurrentlyReading, ReadingStatus.valueOf("CurrentlyReading"))
        assertEquals(ReadingStatus.AlreadyRead, ReadingStatus.valueOf("AlreadyRead"))
    }
}

class BookTest {

    @Test
    fun `Book can be created with required fields`() {
        val book = Book(
            id = "the_hobbit_j.r.r._tolkien",
            title = "The Hobbit",
            readingStatus = ReadingStatus.WantToRead,
        )

        assertEquals("the_hobbit_j.r.r._tolkien", book.id)
        assertEquals("The Hobbit", book.title)
        assertEquals(ReadingStatus.WantToRead, book.readingStatus)
        assertTrue(book.authors.isEmpty())
        assertFalse(book.isFavorite)
    }

    @Test
    fun `Book can be created with all fields`() {
        val authors = listOf("J.R.R. Tolkien")
        val subjects = listOf("Fantasy", "Adventure")
        val dateAdded = System.currentTimeMillis()

        val book = Book(
            id = "the_hobbit_j.r.r._tolkien",
            title = "The Hobbit",
            authors = authors,
            coverUrl = "https://covers.openlibrary.org/b/id/123-L.jpg",
            publishYear = 1937,
            description = "A fantasy novel about a hobbit's adventure",
            subjects = subjects,
            readingStatus = ReadingStatus.CurrentlyReading,
            isFavorite = true,
            workKey = "/works/OL45804W",
            editionKey = "/books/OL7353617M",
            dateAdded = dateAdded,
        )

        assertEquals("the_hobbit_j.r.r._tolkien", book.id)
        assertEquals("The Hobbit", book.title)
        assertEquals(authors, book.authors)
        assertEquals("https://covers.openlibrary.org/b/id/123-L.jpg", book.coverUrl)
        assertEquals(1937, book.publishYear)
        assertEquals("A fantasy novel about a hobbit's adventure", book.description)
        assertEquals(subjects, book.subjects)
        assertEquals(ReadingStatus.CurrentlyReading, book.readingStatus)
        assertTrue(book.isFavorite)
        assertEquals("/works/OL45804W", book.workKey)
        assertEquals("/books/OL7353617M", book.editionKey)
        assertEquals(dateAdded, book.dateAdded)
    }

    @Test
    fun `Book copy creates new instance with modified fields`() {
        val original = Book(
            id = "original_id",
            title = "Original Title",
            readingStatus = ReadingStatus.WantToRead,
        )

        val modified = original.copy(
            title = "Modified Title",
            isFavorite = true,
        )

        assertEquals("original_id", modified.id)
        assertEquals("Modified Title", modified.title)
        assertTrue(modified.isFavorite)
        assertEquals(ReadingStatus.WantToRead, modified.readingStatus)
    }
}

class FilterOptionsTest {

    @Test
    fun `FilterOptions isEmpty returns true when all filters are null or empty`() {
        val options = FilterOptions()
        assertTrue(options.isEmpty())
    }

    @Test
    fun `FilterOptions isEmpty returns false when readingStatus is set`() {
        val options = FilterOptions(readingStatus = ReadingStatus.WantToRead)
        assertFalse(options.isEmpty())
    }

    @Test
    fun `FilterOptions isEmpty returns false when isFavorite is set`() {
        val options = FilterOptions(isFavorite = true)
        assertFalse(options.isEmpty())
    }

    @Test
    fun `FilterOptions isEmpty returns false when searchQuery is set`() {
        val options = FilterOptions(searchQuery = "hobbit")
        assertFalse(options.isEmpty())
    }

    @Test
    fun `FilterOptions isEmpty returns false when subjects is not empty`() {
        val options = FilterOptions(subjects = listOf("Fantasy"))
        assertFalse(options.isEmpty())
    }

    @Test
    fun `FilterOptions isEmpty returns true when searchQuery is blank`() {
        val options = FilterOptions(searchQuery = "   ")
        assertTrue(options.isEmpty())
    }

    @Test
    fun `FilterOptions isNotEmpty is opposite of isEmpty`() {
        val emptyOptions = FilterOptions()
        val nonEmptyOptions = FilterOptions(readingStatus = ReadingStatus.WantToRead)

        assertTrue(emptyOptions.isEmpty())
        assertFalse(emptyOptions.isNotEmpty())

        assertFalse(nonEmptyOptions.isEmpty())
        assertTrue(nonEmptyOptions.isNotEmpty())
    }

    @Test
    fun `FilterOptions can combine multiple filters`() {
        val options = FilterOptions(
            readingStatus = ReadingStatus.CurrentlyReading,
            isFavorite = true,
            searchQuery = "tolkien",
            subjects = listOf("Fantasy", "Adventure"),
        )

        assertEquals(ReadingStatus.CurrentlyReading, options.readingStatus)
        assertEquals(true, options.isFavorite)
        assertEquals("tolkien", options.searchQuery)
        assertEquals(2, options.subjects.size)
        assertTrue(options.isNotEmpty())
    }
}

class SortOptionTest {

    @Test
    fun `SortOption displayName returns correct strings`() {
        assertEquals("Title (A-Z)", SortOption.TitleAscending.displayName())
        assertEquals("Title (Z-A)", SortOption.TitleDescending.displayName())
        assertEquals("Author Name", SortOption.AuthorName.displayName())
        assertEquals("Newest First", SortOption.PublishYearNewest.displayName())
        assertEquals("Oldest First", SortOption.PublishYearOldest.displayName())
        assertEquals("Recently Added", SortOption.DateAddedNewest.displayName())
        assertEquals("Least Recently Added", SortOption.DateAddedOldest.displayName())
    }

    @Test
    fun `SortOption sealed class has seven options`() {
        val options = listOf(
            SortOption.TitleAscending,
            SortOption.TitleDescending,
            SortOption.AuthorName,
            SortOption.PublishYearNewest,
            SortOption.PublishYearOldest,
            SortOption.DateAddedNewest,
            SortOption.DateAddedOldest,
        )

        assertEquals(7, options.size)
    }

    @Test
    fun `SortOption instances are singletons`() {
        val option1 = SortOption.TitleAscending
        val option2 = SortOption.TitleAscending

        assertTrue(option1 === option2)
    }
}
