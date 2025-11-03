package com.darach.openlibrarybooks.core.network.error

import com.darach.openlibrarybooks.core.network.error.ApiErrorHandler.toApiException
import com.darach.openlibrarybooks.core.network.error.ApiErrorHandler.toApiResult
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiErrorHandlerTest {

    @Test
    fun `toApiResult wraps success value in ApiResult Success`() {
        val testData = "test data"
        val single = Single.just(testData)

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Success)
        assertEquals(testData, (result as ApiResult.Success).data)
    }

    @Test
    fun `toApiResult wraps UnknownHostException in NoInternetException`() {
        val single = Single.error<String>(UnknownHostException("Unable to resolve host"))

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is ApiException.NoInternetException)
        assertTrue(error.message?.contains("internet connection") == true)
    }

    @Test
    fun `toApiResult wraps SocketTimeoutException in TimeoutException`() {
        val single = Single.error<String>(SocketTimeoutException("Connection timed out"))

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is ApiException.TimeoutException)
        assertTrue(error.message?.contains("timed out") == true)
    }

    @Test
    fun `toApiResult wraps HttpException in ServerException with code`() {
        val httpException = HttpException(
            Response.error<String>(
                404,
                "Not found".toResponseBody(),
            ),
        )
        val single = Single.error<String>(httpException)

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is ApiException.ServerException)
        assertEquals(404, (error.exception as ApiException.ServerException).code)
    }

    @Test
    fun `toApiResult wraps IOException in NoInternetException`() {
        val single = Single.error<String>(IOException("Network error"))

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is ApiException.NoInternetException)
    }

    @Test
    fun `toApiResult wraps unknown exception in UnknownException`() {
        val single = Single.error<String>(RuntimeException("Unknown error"))

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is ApiException.UnknownException)
        assertEquals("Unknown error", (error.exception as ApiException.UnknownException).cause?.message)
    }

    @Test
    fun `toApiException converts UnknownHostException correctly`() {
        val exception = UnknownHostException("Host not found")

        val apiException = exception.toApiException()

        assertTrue(apiException is ApiException.NoInternetException)
        assertTrue(apiException.message?.contains("internet connection") == true)
    }

    @Test
    fun `toApiException converts SocketTimeoutException correctly`() {
        val exception = SocketTimeoutException("Timeout")

        val apiException = exception.toApiException()

        assertTrue(apiException is ApiException.TimeoutException)
        assertTrue(apiException.message?.contains("timed out") == true)
    }

    @Test
    fun `toApiException converts HttpException with 400 code`() {
        val httpException = HttpException(
            Response.error<String>(
                400,
                "Bad request".toResponseBody(),
            ),
        )

        val apiException = httpException.toApiException()

        assertTrue(apiException is ApiException.ServerException)
        val serverException = apiException as ApiException.ServerException
        assertEquals(400, serverException.code)
        assertTrue(serverException.isClientError)
        assertFalse(serverException.isServerError)
    }

    @Test
    fun `toApiException converts HttpException with 401 code`() {
        val httpException = HttpException(
            Response.error<String>(
                401,
                "Unauthorised".toResponseBody(),
            ),
        )

        val apiException = httpException.toApiException()

        assertTrue(apiException is ApiException.ServerException)
        assertEquals(401, (apiException as ApiException.ServerException).code)
    }

    @Test
    fun `toApiException converts HttpException with 404 code`() {
        val httpException = HttpException(
            Response.error<String>(
                404,
                "Not found".toResponseBody(),
            ),
        )

        val apiException = httpException.toApiException()

        assertTrue(apiException is ApiException.ServerException)
        assertEquals(404, (apiException as ApiException.ServerException).code)
    }

    @Test
    fun `toApiException converts HttpException with 429 code`() {
        val httpException = HttpException(
            Response.error<String>(
                429,
                "Too many requests".toResponseBody(),
            ),
        )

        val apiException = httpException.toApiException()

        assertTrue(apiException is ApiException.ServerException)
        assertEquals(429, (apiException as ApiException.ServerException).code)
    }

    @Test
    fun `toApiException converts HttpException with 500 code`() {
        val httpException = HttpException(
            Response.error<String>(
                500,
                "Internal server error".toResponseBody(),
            ),
        )

        val apiException = httpException.toApiException()

        assertTrue(apiException is ApiException.ServerException)
        val serverException = apiException as ApiException.ServerException
        assertEquals(500, serverException.code)
        assertFalse(serverException.isClientError)
        assertTrue(serverException.isServerError)
    }

    @Test
    fun `toApiException converts IOException correctly`() {
        val exception = IOException("Network error")

        val apiException = exception.toApiException()

        assertTrue(apiException is ApiException.NoInternetException)
        assertTrue(apiException.message?.contains("Network error") == true)
    }

    @Test
    fun `toApiException preserves ApiException`() {
        val originalException = ApiException.NoInternetException("Original message")

        val apiException = originalException.toApiException()

        assertEquals(originalException, apiException)
    }

    @Test
    fun `toApiException converts unknown exception to UnknownException`() {
        val exception = RuntimeException("Something went wrong")

        val apiException = exception.toApiException()

        assertTrue(apiException is ApiException.UnknownException)
        val unknownException = apiException as ApiException.UnknownException
        assertEquals("Something went wrong", unknownException.message)
        assertEquals(exception, unknownException.cause)
    }

    @Test
    fun `toApiException converts exception with null message`() {
        val exception = RuntimeException(null as String?)

        val apiException = exception.toApiException()

        assertTrue(apiException is ApiException.UnknownException)
        assertEquals("An unexpected error occurred", apiException.message)
    }

    @Test
    fun `toApiResult preserves data type`() {
        data class TestData(val value: String)
        val testData = TestData("test")
        val single = Single.just(testData)

        val result = single.toApiResult().blockingGet()

        assertTrue(result is ApiResult.Success)
        assertEquals(testData, (result as ApiResult.Success).data)
    }

    private fun assertFalse(b: Boolean) {
        org.junit.Assert.assertFalse(b)
    }
}
