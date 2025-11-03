package com.darach.openlibrarybooks.core.database.converter

import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    // String List Conversion Tests

    @Test
    fun `fromStringList converts list to JSON string`() {
        val list = listOf("item1", "item2", "item3")
        val json = converters.fromStringList(list)

        assertNotNull(json)
        assertTrue(json!!.contains("item1"))
        assertTrue(json.contains("item2"))
        assertTrue(json.contains("item3"))
    }

    @Test
    fun `fromStringList handles empty list`() {
        val list = emptyList<String>()
        val json = converters.fromStringList(list)

        assertNotNull(json)
        assertEquals("[]", json)
    }

    @Test
    fun `fromStringList handles null input`() {
        val json = converters.fromStringList(null)
        assertNull(json)
    }

    @Test
    fun `toStringList converts JSON string to list`() {
        val json = "[\"item1\",\"item2\",\"item3\"]"
        val list = converters.toStringList(json)

        assertEquals(3, list.size)
        assertEquals("item1", list[0])
        assertEquals("item2", list[1])
        assertEquals("item3", list[2])
    }

    @Test
    fun `toStringList handles empty JSON array`() {
        val json = "[]"
        val list = converters.toStringList(json)

        assertTrue(list.isEmpty())
    }

    @Test
    fun `toStringList handles null input`() {
        val list = converters.toStringList(null)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `toStringList handles blank input`() {
        val list = converters.toStringList("   ")
        assertTrue(list.isEmpty())
    }

    @Test
    fun `toStringList handles invalid JSON gracefully`() {
        val list = converters.toStringList("not valid json")
        assertTrue(list.isEmpty())
    }

    @Test
    fun `toStringList handles JSON with special characters`() {
        val json = "[\"item with spaces\",\"item_with_underscores\",\"item-with-dashes\"]"
        val list = converters.toStringList(json)

        assertEquals(3, list.size)
        assertEquals("item with spaces", list[0])
        assertEquals("item_with_underscores", list[1])
        assertEquals("item-with-dashes", list[2])
    }

    @Test
    fun `String list roundtrip conversion preserves data`() {
        val originalList = listOf("J.R.R. Tolkien", "George Orwell", "Jane Austen")
        val json = converters.fromStringList(originalList)
        val restoredList = converters.toStringList(json)

        assertEquals(originalList, restoredList)
    }

    // ReadingStatus Conversion Tests

    @Test
    fun `fromReadingStatus converts WantToRead to string`() {
        val result = converters.fromReadingStatus(ReadingStatus.WantToRead)
        assertEquals("WantToRead", result)
    }

    @Test
    fun `fromReadingStatus converts CurrentlyReading to string`() {
        val result = converters.fromReadingStatus(ReadingStatus.CurrentlyReading)
        assertEquals("CurrentlyReading", result)
    }

    @Test
    fun `fromReadingStatus converts AlreadyRead to string`() {
        val result = converters.fromReadingStatus(ReadingStatus.AlreadyRead)
        assertEquals("AlreadyRead", result)
    }

    @Test
    fun `fromReadingStatus handles null input`() {
        val result = converters.fromReadingStatus(null)
        assertNull(result)
    }

    @Test
    fun `toReadingStatus converts WantToRead string to enum`() {
        val result = converters.toReadingStatus("WantToRead")
        assertEquals(ReadingStatus.WantToRead, result)
    }

    @Test
    fun `toReadingStatus converts CurrentlyReading string to enum`() {
        val result = converters.toReadingStatus("CurrentlyReading")
        assertEquals(ReadingStatus.CurrentlyReading, result)
    }

    @Test
    fun `toReadingStatus converts AlreadyRead string to enum`() {
        val result = converters.toReadingStatus("AlreadyRead")
        assertEquals(ReadingStatus.AlreadyRead, result)
    }

    @Test
    fun `toReadingStatus handles null input`() {
        val result = converters.toReadingStatus(null)
        assertNull(result)
    }

    @Test
    fun `toReadingStatus handles blank input`() {
        val result = converters.toReadingStatus("   ")
        assertNull(result)
    }

    @Test
    fun `toReadingStatus handles invalid string gracefully`() {
        val result = converters.toReadingStatus("InvalidStatus")
        assertNull(result)
    }

    @Test
    fun `ReadingStatus roundtrip conversion preserves data`() {
        val originalStatus = ReadingStatus.CurrentlyReading
        val string = converters.fromReadingStatus(originalStatus)
        val restoredStatus = converters.toReadingStatus(string)

        assertEquals(originalStatus, restoredStatus)
    }

    @Test
    fun `All ReadingStatus values can be converted roundtrip`() {
        ReadingStatus.entries.forEach { status ->
            val string = converters.fromReadingStatus(status)
            val restored = converters.toReadingStatus(string)
            assertEquals(status, restored)
        }
    }
}
