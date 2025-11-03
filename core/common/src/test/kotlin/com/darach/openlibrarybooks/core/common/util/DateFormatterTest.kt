package com.darach.openlibrarybooks.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class DateFormatterTest {

    @Test
    fun `formatDate formats timestamp without time`() {
        val timestamp = 1609459200000L // Jan 1, 2021 00:00:00 UTC
        val result = DateFormatter.formatDate(timestamp, includeTime = false)

        assertNotNull(result)
        assertTrue(result.contains("Jan") || result.contains("Dec"))
        assertTrue(result.contains("2021") || result.contains("2020"))
    }

    @Test
    fun `formatDate formats timestamp with time`() {
        val timestamp = 1609459200000L // Jan 1, 2021 00:00:00 UTC
        val result = DateFormatter.formatDate(timestamp, includeTime = true)

        assertNotNull(result)
        assertTrue(result.contains(":"))
    }

    @Test
    fun `formatDate formats Date object without time`() {
        val date = Date(1609459200000L)
        val result = DateFormatter.formatDate(date, includeTime = false)

        assertNotNull(result)
        assertTrue(result.contains("Jan") || result.contains("Dec"))
    }

    @Test
    fun `formatDate formats Date object with time`() {
        val date = Date(1609459200000L)
        val result = DateFormatter.formatDate(date, includeTime = true)

        assertNotNull(result)
        assertTrue(result.contains(":"))
    }

    @Test
    fun `formatRelativeTime shows 'Just now' for recent timestamps`() {
        val now = System.currentTimeMillis()
        val result = DateFormatter.formatRelativeTime(now)

        assertEquals("Just now", result)
    }

    @Test
    fun `formatRelativeTime shows 'Just now' for future timestamps`() {
        val future = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        val result = DateFormatter.formatRelativeTime(future)

        assertEquals("Just now", result)
    }

    @Test
    fun `formatRelativeTime shows minutes ago`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)
        val result = DateFormatter.formatRelativeTime(timestamp)

        assertTrue(result.contains("minute"))
        assertTrue(result.contains("ago"))
    }

    @Test
    fun `formatRelativeTime shows singular minute`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)
        val result = DateFormatter.formatRelativeTime(timestamp)

        assertTrue(result.contains("1 minute ago"))
    }

    @Test
    fun `formatRelativeTime shows hours ago`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)
        val result = DateFormatter.formatRelativeTime(timestamp)

        assertTrue(result.contains("hour"))
        assertTrue(result.contains("ago"))
    }

    @Test
    fun `formatRelativeTime shows singular hour`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        val result = DateFormatter.formatRelativeTime(timestamp)

        assertTrue(result.contains("1 hour ago"))
    }

    @Test
    fun `formatRelativeTime shows days ago`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
        val result = DateFormatter.formatRelativeTime(timestamp)

        assertTrue(result.contains("day"))
        assertTrue(result.contains("ago"))
    }

    @Test
    fun `formatRelativeTime shows singular day`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val result = DateFormatter.formatRelativeTime(timestamp)

        assertTrue(result.contains("1 day ago"))
    }

    @Test
    fun `formatRelativeTime shows formatted date for old timestamps`() {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val result = DateFormatter.formatRelativeTime(timestamp)

        // Should return formatted date, not relative time
        assertNotNull(result)
        // Should not contain "ago"
        assertFalse(result.contains("ago"))
    }

    @Test
    fun `formatForApi formats timestamp in ISO 8601 format`() {
        val timestamp = 1609459200000L // Jan 1, 2021
        val result = DateFormatter.formatForApi(timestamp)

        // Should be in yyyy-MM-dd format
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `parseFromApi parses valid date string`() {
        val dateString = "2021-01-01"
        val result = DateFormatter.parseFromApi(dateString)

        assertNotNull(result)
    }

    @Test
    fun `parseFromApi returns null for null input`() {
        val result = DateFormatter.parseFromApi(null)
        assertNull(result)
    }

    @Test
    fun `parseFromApi returns null for blank input`() {
        val result = DateFormatter.parseFromApi("   ")
        assertNull(result)
    }

    @Test
    fun `parseFromApi returns null for invalid date format`() {
        val result = DateFormatter.parseFromApi("not a date")
        assertNull(result)
    }

    @Test
    fun `parseFromApi roundtrip with formatForApi preserves date`() {
        val originalTimestamp = 1609459200000L // Jan 1, 2021
        val dateString = DateFormatter.formatForApi(originalTimestamp)
        val parsedTimestamp = DateFormatter.parseFromApi(dateString)

        assertNotNull(parsedTimestamp)
        // Compare dates (ignoring time component)
        val originalDate = DateFormatter.formatForApi(originalTimestamp)
        val parsedDate = DateFormatter.formatForApi(parsedTimestamp!!)
        assertEquals(originalDate, parsedDate)
    }

    @Test
    fun `now returns current timestamp`() {
        val before = System.currentTimeMillis()
        val now = DateFormatter.now()
        val after = System.currentTimeMillis()

        assertTrue(now >= before)
        assertTrue(now <= after)
    }

    private fun assertFalse(condition: Boolean) {
        assertTrue(!condition)
    }
}
