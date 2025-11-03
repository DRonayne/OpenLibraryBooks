package com.darach.openlibrarybooks.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for EmptyState composable and related types.
 */
class EmptyStateTest {
    @Test
    fun `EmptyStateType enum should have correct values`() {
        // Verify all empty state types exist
        val emptyStateTypes = EmptyStateType.values()

        assertEquals(4, emptyStateTypes.size)
        assertEquals(EmptyStateType.NO_BOOKS_FOUND, emptyStateTypes[0])
        assertEquals(EmptyStateType.NO_BOOKS_IN_LIBRARY, emptyStateTypes[1])
        assertEquals(EmptyStateType.NO_FAVOURITES, emptyStateTypes[2])
        assertEquals(EmptyStateType.NO_BOOKS_IN_LIST, emptyStateTypes[3])
    }

    @Test
    fun `EmptyStateType NO_BOOKS_FOUND should be the default`() {
        // Verify NO_BOOKS_FOUND is the first enum value (used as default)
        assertEquals(EmptyStateType.NO_BOOKS_FOUND, EmptyStateType.values()[0])
    }
}
