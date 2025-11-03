package com.darach.openlibrarybooks.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ErrorState composable and related types.
 */
class ErrorStateTest {
    @Test
    fun `ErrorType enum should have correct values`() {
        // Verify all error types exist
        val errorTypes = ErrorType.values()

        assertEquals(4, errorTypes.size)
        assertEquals(ErrorType.GENERIC, errorTypes[0])
        assertEquals(ErrorType.NETWORK, errorTypes[1])
        assertEquals(ErrorType.TIMEOUT, errorTypes[2])
        assertEquals(ErrorType.SERVER, errorTypes[3])
    }

    @Test
    fun `ErrorType GENERIC should be the default`() {
        // Verify GENERIC is the first enum value (used as default)
        assertEquals(ErrorType.GENERIC, ErrorType.values()[0])
    }
}
