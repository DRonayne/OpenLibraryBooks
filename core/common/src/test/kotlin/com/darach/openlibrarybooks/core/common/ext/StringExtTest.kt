package com.darach.openlibrarybooks.core.common.ext

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtTest {

    // Composite Key Generation Tests

    @Test
    fun `generateCompositeKey creates key with title and first author`() {
        val key = generateCompositeKey("The Hobbit", listOf("J.R.R. Tolkien"))
        assertEquals("the_hobbit_j.r.r._tolkien", key)
    }

    @Test
    fun `generateCompositeKey uses only first author`() {
        val key = generateCompositeKey(
            "1984",
            listOf("George Orwell", "Other Author"),
        )
        assertEquals("1984_george_orwell", key)
    }

    @Test
    fun `generateCompositeKey handles empty authors list`() {
        val key = generateCompositeKey("Unknown Book", emptyList())
        assertEquals("unknown_book", key)
    }

    @Test
    fun `generateCompositeKey sanitizes special characters`() {
        val key = generateCompositeKey(
            "The Lord of the Rings: The Fellowship",
            listOf("J.R.R. Tolkien"),
        )
        assertEquals("the_lord_of_the_rings_the_fellowship_j.r.r._tolkien", key)
    }

    @Test
    fun `generateCompositeKey handles titles with numbers`() {
        val key = generateCompositeKey("1984", listOf("George Orwell"))
        assertEquals("1984_george_orwell", key)
    }

    @Test
    fun `generateCompositeKey removes multiple consecutive spaces`() {
        val key = generateCompositeKey(
            "Book    With    Spaces",
            listOf("Author  Name"),
        )
        assertEquals("book_with_spaces_author_name", key)
    }

    // String Sanitization Tests

    @Test
    fun `sanitizeForKey converts to lowercase`() {
        val result = "UPPERCASE".sanitizeForKey()
        assertEquals("uppercase", result)
    }

    @Test
    fun `sanitizeForKey replaces spaces with underscores`() {
        val result = "multiple word string".sanitizeForKey()
        assertEquals("multiple_word_string", result)
    }

    @Test
    fun `sanitizeForKey removes special characters`() {
        val result = "text!@#$%^&*()+=[]{}|\\:;\"'<>?/".sanitizeForKey()
        assertEquals("text", result)
    }

    @Test
    fun `sanitizeForKey preserves periods`() {
        val result = "J.R.R. Tolkien".sanitizeForKey()
        assertEquals("j.r.r._tolkien", result)
    }

    @Test
    fun `sanitizeForKey preserves alphanumeric characters`() {
        val result = "abc123XYZ".sanitizeForKey()
        assertEquals("abc123xyz", result)
    }

    @Test
    fun `sanitizeForKey removes leading and trailing underscores`() {
        val result = "___text___".sanitizeForKey()
        assertEquals("text", result)
    }

    @Test
    fun `sanitizeForKey replaces multiple underscores with single`() {
        val result = "text___with___underscores".sanitizeForKey()
        assertEquals("text_with_underscores", result)
    }

    @Test
    fun `sanitizeForKey trims whitespace`() {
        val result = "   trimmed   ".sanitizeForKey()
        assertEquals("trimmed", result)
    }

    // Truncate Tests

    @Test
    fun `truncate does not modify string shorter than maxLength`() {
        val result = "short".truncate(10)
        assertEquals("short", result)
    }

    @Test
    fun `truncate adds ellipsis to string longer than maxLength`() {
        val result = "this is a very long string".truncate(10)
        assertEquals("this is...", result)
    }

    @Test
    fun `truncate respects custom ellipsis`() {
        val result = "this is a very long string".truncate(10, "...")
        assertEquals("this is...", result)
    }

    @Test
    fun `truncate handles maxLength equal to string length`() {
        val result = "exact".truncate(5)
        assertEquals("exact", result)
    }

    @Test
    fun `truncate handles very short maxLength`() {
        val result = "text".truncate(3)
        assertEquals("...", result)
    }

    @Test
    fun `truncate with longer ellipsis`() {
        val result = "this is a very long string".truncate(15, " [more]")
        assertEquals("this is  [more]", result)
    }

    // Capitalize Words Tests

    @Test
    fun `capitalizeWords capitalizes first letter of each word`() {
        val result = "hello world".capitalizeWords()
        assertEquals("Hello World", result)
    }

    @Test
    fun `capitalizeWords handles already capitalized words`() {
        val result = "Hello World".capitalizeWords()
        assertEquals("Hello World", result)
    }

    @Test
    fun `capitalizeWords handles mixed case`() {
        val result = "hElLo WoRlD".capitalizeWords()
        assertEquals("HElLo WoRlD", result)
    }

    @Test
    fun `capitalizeWords handles single word`() {
        val result = "hello".capitalizeWords()
        assertEquals("Hello", result)
    }

    @Test
    fun `capitalizeWords handles empty string`() {
        val result = "".capitalizeWords()
        assertEquals("", result)
    }

    @Test
    fun `capitalizeWords handles multiple spaces`() {
        val result = "hello  world".capitalizeWords()
        assertEquals("Hello  World", result)
    }

    // isNotNullOrBlank Tests

    @Test
    fun `isNotNullOrBlank returns false for null`() {
        val text: String? = null
        assertFalse(text.isNotNullOrBlank())
    }

    @Test
    fun `isNotNullOrBlank returns false for blank string`() {
        assertFalse("   ".isNotNullOrBlank())
    }

    @Test
    fun `isNotNullOrBlank returns false for empty string`() {
        assertFalse("".isNotNullOrBlank())
    }

    @Test
    fun `isNotNullOrBlank returns true for non-blank string`() {
        assertTrue("text".isNotNullOrBlank())
    }

    @Test
    fun `isNotNullOrBlank returns true for string with content`() {
        assertTrue("  text  ".isNotNullOrBlank())
    }

    // blankToNull Tests

    @Test
    fun `blankToNull returns null for null input`() {
        val text: String? = null
        assertNull(text.blankToNull())
    }

    @Test
    fun `blankToNull returns null for blank string`() {
        assertNull("   ".blankToNull())
    }

    @Test
    fun `blankToNull returns null for empty string`() {
        assertNull("".blankToNull())
    }

    @Test
    fun `blankToNull returns string for non-blank input`() {
        assertEquals("text", "text".blankToNull())
    }

    @Test
    fun `blankToNull preserves whitespace in non-blank string`() {
        assertEquals("  text  ", "  text  ".blankToNull())
    }
}
