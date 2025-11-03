package com.darach.openlibrarybooks.core.network.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiExceptionTest {

    @Test
    fun `NoInternetException created with default message`() {
        val exception = ApiException.NoInternetException()

        assertEquals("No internet connection available", exception.message)
    }

    @Test
    fun `NoInternetException created with custom message`() {
        val customMessage = "Custom internet error"
        val exception = ApiException.NoInternetException(customMessage)

        assertEquals(customMessage, exception.message)
    }

    @Test
    fun `TimeoutException created with default message`() {
        val exception = ApiException.TimeoutException()

        assertEquals("Request timed out", exception.message)
    }

    @Test
    fun `TimeoutException created with custom message`() {
        val customMessage = "Custom timeout error"
        val exception = ApiException.TimeoutException(customMessage)

        assertEquals(customMessage, exception.message)
    }

    @Test
    fun `ServerException created with code only`() {
        val exception = ApiException.ServerException(code = 500)

        assertEquals(500, exception.code)
        assertEquals("Server error: 500", exception.message)
        assertNull(exception.serverMessage)
    }

    @Test
    fun `ServerException created with code and server message`() {
        val serverMessage = "Internal server error"
        val exception = ApiException.ServerException(code = 500, serverMessage = serverMessage)

        assertEquals(500, exception.code)
        assertEquals(serverMessage, exception.message)
        assertEquals(serverMessage, exception.serverMessage)
    }

    @Test
    fun `ServerException isClientError returns true for 4xx codes`() {
        val exception400 = ApiException.ServerException(code = 400)
        val exception404 = ApiException.ServerException(code = 404)
        val exception499 = ApiException.ServerException(code = 499)

        assertTrue(exception400.isClientError)
        assertTrue(exception404.isClientError)
        assertTrue(exception499.isClientError)
    }

    @Test
    fun `ServerException isClientError returns false for non-4xx codes`() {
        val exception399 = ApiException.ServerException(code = 399)
        val exception500 = ApiException.ServerException(code = 500)

        assertFalse(exception399.isClientError)
        assertFalse(exception500.isClientError)
    }

    @Test
    fun `ServerException isServerError returns true for 5xx codes`() {
        val exception500 = ApiException.ServerException(code = 500)
        val exception503 = ApiException.ServerException(code = 503)
        val exception599 = ApiException.ServerException(code = 599)

        assertTrue(exception500.isServerError)
        assertTrue(exception503.isServerError)
        assertTrue(exception599.isServerError)
    }

    @Test
    fun `ServerException isServerError returns false for non-5xx codes`() {
        val exception400 = ApiException.ServerException(code = 400)
        val exception600 = ApiException.ServerException(code = 600)

        assertFalse(exception400.isServerError)
        assertFalse(exception600.isServerError)
    }

    @Test
    fun `UnknownException created with default message`() {
        val exception = ApiException.UnknownException()

        assertEquals("An unknown error occurred", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `UnknownException created with custom message`() {
        val customMessage = "Custom error"
        val exception = ApiException.UnknownException(customMessage)

        assertEquals(customMessage, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `UnknownException created with cause`() {
        val cause = RuntimeException("Original error")
        val exception = ApiException.UnknownException(cause = cause)

        assertEquals("An unknown error occurred", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `UnknownException created with custom message and cause`() {
        val customMessage = "Custom error"
        val cause = RuntimeException("Original error")
        val exception = ApiException.UnknownException(customMessage, cause)

        assertEquals(customMessage, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `All exceptions are subclasses of ApiException`() {
        val noInternet = ApiException.NoInternetException()
        val timeout = ApiException.TimeoutException()
        val server = ApiException.ServerException(500)
        val unknown = ApiException.UnknownException()

        assertTrue(noInternet is ApiException)
        assertTrue(timeout is ApiException)
        assertTrue(server is ApiException)
        assertTrue(unknown is ApiException)
    }

    @Test
    fun `Common HTTP error codes have correct client error status`() {
        val badRequest = ApiException.ServerException(400)
        val unauthorized = ApiException.ServerException(401)
        val forbidden = ApiException.ServerException(403)
        val notFound = ApiException.ServerException(404)
        val tooManyRequests = ApiException.ServerException(429)

        assertTrue(badRequest.isClientError)
        assertTrue(unauthorized.isClientError)
        assertTrue(forbidden.isClientError)
        assertTrue(notFound.isClientError)
        assertTrue(tooManyRequests.isClientError)

        assertFalse(badRequest.isServerError)
        assertFalse(unauthorized.isServerError)
        assertFalse(forbidden.isServerError)
        assertFalse(notFound.isServerError)
        assertFalse(tooManyRequests.isServerError)
    }

    @Test
    fun `Common HTTP error codes have correct server error status`() {
        val internalError = ApiException.ServerException(500)
        val serviceUnavailable = ApiException.ServerException(503)

        assertTrue(internalError.isServerError)
        assertTrue(serviceUnavailable.isServerError)

        assertFalse(internalError.isClientError)
        assertFalse(serviceUnavailable.isClientError)
    }
}
