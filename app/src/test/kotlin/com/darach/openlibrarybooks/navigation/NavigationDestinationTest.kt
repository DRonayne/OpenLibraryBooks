package com.darach.openlibrarybooks.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for type-safe navigation destinations.
 * Verifies that @Serializable routes are properly configured.
 */
class NavigationDestinationTest {

    @Test
    fun `BooksRoute should be a singleton object`() {
        // Verify that BooksRoute is accessible
        assertNotNull(BooksRoute)
    }

    @Test
    fun `FavouritesRoute should be a singleton object`() {
        // Verify that FavouritesRoute is accessible
        assertNotNull(FavouritesRoute)
    }

    @Test
    fun `SettingsRoute should be a singleton object`() {
        // Verify that SettingsRoute is accessible
        assertNotNull(SettingsRoute)
    }

    @Test
    fun `BookDetailsRoute should accept workId parameter`() {
        // Given a work ID
        val workId = "OL45804W"

        // When creating a BookDetailsRoute
        val route = BookDetailsRoute(workId = workId)

        // Then it should contain the work ID
        assertEquals(workId, route.workId)
        assertEquals(null, route.editionId)
    }

    @Test
    fun `BookDetailsRoute should accept optional editionId parameter`() {
        // Given work and edition IDs
        val workId = "OL45804W"
        val editionId = "OL7353617M"

        // When creating a BookDetailsRoute with both parameters
        val route = BookDetailsRoute(workId = workId, editionId = editionId)

        // Then it should contain both IDs
        assertEquals(workId, route.workId)
        assertEquals(editionId, route.editionId)
    }

    @Test
    fun `TopLevelDestination should contain all main destinations`() {
        // Given the top level destinations
        val destinations = TopLevelDestination.entries

        // Then it should contain exactly three destinations
        assertEquals(3, destinations.size)

        // And they should be Books, Favourites, and Settings
        val destinationNames = destinations.map { it.name }
        assertEquals(listOf("BOOKS", "FAVOURITES", "SETTINGS"), destinationNames)
    }

    @Test
    fun `TopLevelDestination BOOKS should have correct properties`() {
        // Given the BOOKS destination
        val books = TopLevelDestination.BOOKS

        // Then it should have the correct label
        assertEquals("Books", books.label)

        // And icons should be set
        assertNotNull(books.iconSelected)
        assertNotNull(books.iconUnselected)
    }

    @Test
    fun `TopLevelDestination FAVOURITES should have correct properties`() {
        // Given the FAVOURITES destination
        val favourites = TopLevelDestination.FAVOURITES

        // Then it should have the correct label
        assertEquals("Favourites", favourites.label)

        // And icons should be set
        assertNotNull(favourites.iconSelected)
        assertNotNull(favourites.iconUnselected)
    }

    @Test
    fun `TopLevelDestination SETTINGS should have correct properties`() {
        // Given the SETTINGS destination
        val settings = TopLevelDestination.SETTINGS

        // Then it should have the correct label
        assertEquals("Settings", settings.label)

        // And icons should be set
        assertNotNull(settings.iconSelected)
        assertNotNull(settings.iconUnselected)
    }
}
