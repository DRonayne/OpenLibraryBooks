package com.darach.openlibrarybooks.core.network.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResultTest {

    @Test
    fun `Success holds data correctly`() {
        val data = "test data"
        val result: ApiResult<String> = ApiResult.Success(data)

        assertTrue(result is ApiResult.Success)
        assertEquals(data, (result as ApiResult.Success).data)
    }

    @Test
    fun `Error holds exception and message`() {
        val exception = ApiException.UnknownException("Test error")
        val message = "Custom message"
        val result: ApiResult<String> = ApiResult.Error(exception, message)

        assertTrue(result is ApiResult.Error)
        assertEquals(exception, (result as ApiResult.Error).exception)
        assertEquals(message, result.message)
    }

    @Test
    fun `Error uses exception message when custom message not provided`() {
        val exception = ApiException.UnknownException("Exception message")
        val result: ApiResult<String> = ApiResult.Error(exception)

        assertTrue(result is ApiResult.Error)
        assertEquals("Exception message", (result as ApiResult.Error).message)
    }

    @Test
    fun `isSuccess returns true for Success`() {
        val result: ApiResult<String> = ApiResult.Success("data")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `isSuccess returns false for Error`() {
        val result: ApiResult<String> = ApiResult.Error(ApiException.UnknownException())
        assertFalse(result.isSuccess)
    }

    @Test
    fun `isError returns true for Error`() {
        val result: ApiResult<String> = ApiResult.Error(ApiException.UnknownException())
        assertTrue(result.isError)
    }

    @Test
    fun `isError returns false for Success`() {
        val result: ApiResult<String> = ApiResult.Success("data")
        assertFalse(result.isError)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val data = "test data"
        val result: ApiResult<String> = ApiResult.Success(data)

        assertEquals(data, result.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Error`() {
        val result: ApiResult<String> = ApiResult.Error(ApiException.UnknownException())

        assertNull(result.getOrNull())
    }

    @Test
    fun `errorOrNull returns Error for Error result`() {
        val exception = ApiException.UnknownException("Test")
        val result: ApiResult<String> = ApiResult.Error(exception)

        val error = result.errorOrNull()
        assertTrue(error is ApiResult.Error)
        assertEquals(exception, error?.exception)
    }

    @Test
    fun `errorOrNull returns null for Success`() {
        val result: ApiResult<String> = ApiResult.Success("data")

        assertNull(result.errorOrNull())
    }

    @Test
    fun `map transforms Success data`() {
        val result: ApiResult<Int> = ApiResult.Success(5)

        val mapped = result.map { it * 2 }

        assertTrue(mapped is ApiResult.Success)
        assertEquals(10, (mapped as ApiResult.Success).data)
    }

    @Test
    fun `map preserves Error`() {
        val exception = ApiException.UnknownException("Test")
        val result: ApiResult<Int> = ApiResult.Error(exception)

        val mapped = result.map { it * 2 }

        assertTrue(mapped is ApiResult.Error)
        assertEquals(exception, (mapped as ApiResult.Error).exception)
    }

    @Test
    fun `onSuccess executes action for Success`() {
        var executed = false
        val result: ApiResult<String> = ApiResult.Success("data")

        result.onSuccess { executed = true }

        assertTrue(executed)
    }

    @Test
    fun `onSuccess does not execute for Error`() {
        var executed = false
        val result: ApiResult<String> = ApiResult.Error(ApiException.UnknownException())

        result.onSuccess { executed = true }

        assertFalse(executed)
    }

    @Test
    fun `onError executes action for Error`() {
        var executed = false
        val result: ApiResult<String> = ApiResult.Error(ApiException.UnknownException())

        result.onError { executed = true }

        assertTrue(executed)
    }

    @Test
    fun `onError does not execute for Success`() {
        var executed = false
        val result: ApiResult<String> = ApiResult.Success("data")

        result.onError { executed = true }

        assertFalse(executed)
    }

    @Test
    fun `chaining onSuccess and onError works correctly`() {
        var successExecuted = false
        var errorExecuted = false

        val successResult: ApiResult<String> = ApiResult.Success("data")
        successResult
            .onSuccess { successExecuted = true }
            .onError { errorExecuted = true }

        assertTrue(successExecuted)
        assertFalse(errorExecuted)
    }

    @Test
    fun `ApiResult can hold different data types`() {
        val stringResult: ApiResult<String> = ApiResult.Success("text")
        val intResult: ApiResult<Int> = ApiResult.Success(42)
        val listResult: ApiResult<List<String>> = ApiResult.Success(listOf("a", "b"))

        assertEquals("text", stringResult.getOrNull())
        assertEquals(42, intResult.getOrNull())
        assertEquals(listOf("a", "b"), listResult.getOrNull())
    }
}
