package com.darach.openlibrarybooks.core.common.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {

    @Test
    fun `UiState Idle is created correctly`() {
        val state: UiState<String> = UiState.Idle
        assertTrue(state is UiState.Idle)
    }

    @Test
    fun `UiState Loading is created correctly`() {
        val state: UiState<String> = UiState.Loading
        assertTrue(state is UiState.Loading)
    }

    @Test
    fun `UiState Success holds data`() {
        val data = "test data"
        val state: UiState<String> = UiState.Success(data)

        assertTrue(state is UiState.Success)
        assertEquals(data, (state as UiState.Success).data)
    }

    @Test
    fun `UiState Error holds message and throwable`() {
        val message = "Error message"
        val throwable = RuntimeException("Test exception")
        val state: UiState<String> = UiState.Error(message, throwable)

        assertTrue(state is UiState.Error)
        assertEquals(message, (state as UiState.Error).message)
        assertEquals(throwable, state.throwable)
    }

    @Test
    fun `UiState Error can be created without throwable`() {
        val message = "Error message"
        val state: UiState<String> = UiState.Error(message)

        assertTrue(state is UiState.Error)
        assertEquals(message, (state as UiState.Error).message)
        assertNull(state.throwable)
    }

    @Test
    fun `UiState Empty is created correctly`() {
        val state: UiState<String> = UiState.Empty
        assertTrue(state is UiState.Empty)
    }

    // Extension Function Tests

    @Test
    fun `isLoading returns true for Loading state`() {
        val state: UiState<String> = UiState.Loading
        assertTrue(state.isLoading())
    }

    @Test
    fun `isLoading returns false for non-Loading states`() {
        assertFalse(UiState.Idle.isLoading())
        assertFalse(UiState.Success("data").isLoading())
        assertFalse(UiState.Error("error").isLoading())
        assertFalse(UiState.Empty.isLoading())
    }

    @Test
    fun `isSuccess returns true for Success state`() {
        val state: UiState<String> = UiState.Success("data")
        assertTrue(state.isSuccess())
    }

    @Test
    fun `isSuccess returns false for non-Success states`() {
        assertFalse(UiState.Idle.isSuccess())
        assertFalse(UiState.Loading.isSuccess())
        assertFalse(UiState.Error("error").isSuccess())
        assertFalse(UiState.Empty.isSuccess())
    }

    @Test
    fun `isError returns true for Error state`() {
        val state: UiState<String> = UiState.Error("error")
        assertTrue(state.isError())
    }

    @Test
    fun `isError returns false for non-Error states`() {
        assertFalse(UiState.Idle.isError())
        assertFalse(UiState.Loading.isError())
        assertFalse(UiState.Success("data").isError())
        assertFalse(UiState.Empty.isError())
    }

    @Test
    fun `isEmpty returns true for Empty state`() {
        val state: UiState<String> = UiState.Empty
        assertTrue(state.isEmpty())
    }

    @Test
    fun `isEmpty returns false for non-Empty states`() {
        assertFalse(UiState.Idle.isEmpty())
        assertFalse(UiState.Loading.isEmpty())
        assertFalse(UiState.Success("data").isEmpty())
        assertFalse(UiState.Error("error").isEmpty())
    }

    @Test
    fun `dataOrNull returns data for Success state`() {
        val data = "test data"
        val state: UiState<String> = UiState.Success(data)

        assertEquals(data, state.dataOrNull())
    }

    @Test
    fun `dataOrNull returns null for non-Success states`() {
        assertNull(UiState.Idle.dataOrNull())
        assertNull(UiState.Loading.dataOrNull())
        assertNull(UiState.Error("error").dataOrNull())
        assertNull(UiState.Empty.dataOrNull())
    }

    @Test
    fun `UiState can hold different data types`() {
        val stringState: UiState<String> = UiState.Success("text")
        val intState: UiState<Int> = UiState.Success(42)
        val listState: UiState<List<String>> = UiState.Success(listOf("a", "b"))

        assertEquals("text", stringState.dataOrNull())
        assertEquals(42, intState.dataOrNull())
        assertEquals(listOf("a", "b"), listState.dataOrNull())
    }

    @Test
    fun `UiState Idle is singleton`() {
        val state1: UiState<String> = UiState.Idle
        val state2: UiState<Int> = UiState.Idle

        assertTrue(state1 === state2)
    }

    @Test
    fun `UiState Loading is singleton`() {
        val state1: UiState<String> = UiState.Loading
        val state2: UiState<Int> = UiState.Loading

        assertTrue(state1 === state2)
    }

    @Test
    fun `UiState Empty is singleton`() {
        val state1: UiState<String> = UiState.Empty
        val state2: UiState<Int> = UiState.Empty

        assertTrue(state1 === state2)
    }

    @Test
    fun `UiState Success instances with same data are equal`() {
        val state1 = UiState.Success("data")
        val state2 = UiState.Success("data")

        assertEquals(state1, state2)
    }

    @Test
    fun `UiState Error instances with same message are equal`() {
        val state1 = UiState.Error("error")
        val state2 = UiState.Error("error")

        assertEquals(state1, state2)
    }
}
