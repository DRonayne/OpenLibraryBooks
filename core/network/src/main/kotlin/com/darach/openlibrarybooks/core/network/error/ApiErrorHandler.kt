package com.darach.openlibrarybooks.core.network.error

import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object for handling API errors and converting them to typed exceptions.
 *
 * Provides extension functions to wrap RxJava streams with error handling,
 * automatically mapping low-level exceptions to domain-specific ApiException types.
 */
object ApiErrorHandler {

    /**
     * Wraps a Single in error handling, converting exceptions to ApiResult.
     *
     * Maps common exceptions (IOException, HttpException, etc.) to appropriate
     * ApiException types and wraps the result in ApiResult for type-safe handling.
     *
     * Usage:
     * ```
     * api.getWork(key)
     *     .toApiResult()
     *     .subscribe { result ->
     *         when (result) {
     *             is ApiResult.Success -> // handle success
     *             is ApiResult.Error -> // handle error
     *         }
     *     }
     * ```
     */
    fun <T : Any> Single<T>.toApiResult(): Single<ApiResult<T>> = this
        .map<ApiResult<T>> { ApiResult.Success(it) }
        .onErrorReturn { error ->
            ApiResult.Error(error.toApiException())
        }

    /**
     * Converts a Throwable to an appropriate ApiException.
     *
     * Maps common exception types to domain-specific exceptions:
     * - UnknownHostException -> NoInternetException
     * - SocketTimeoutException -> TimeoutException
     * - HttpException -> ServerException (with HTTP code)
     * - Others -> UnknownException
     */
    fun Throwable.toApiException(): ApiException = when (this) {
        is UnknownHostException -> ApiException.NoInternetException(
            "Unable to connect to server. Please check your internet connection.",
        )

        is SocketTimeoutException -> ApiException.TimeoutException(
            "Request timed out. Please try again.",
        )

        is HttpException -> {
            val code = code()
            val errorMessage = response()?.errorBody()?.string()
                ?: getHttpErrorMessage(code)
            ApiException.ServerException(code, errorMessage)
        }

        is IOException -> ApiException.NoInternetException(
            message ?: "Network error occurred. Please check your connection.",
        )

        is ApiException -> this

        else -> ApiException.UnknownException(
            message ?: "An unexpected error occurred",
            this,
        )
    }

    /**
     * Gets a user-friendly error message for HTTP status codes.
     */
    private fun getHttpErrorMessage(code: Int): String = when (code) {
        400 -> "Bad request. Please check your input."
        401 -> "Unauthorised. Please log in again."
        403 -> "Access forbidden."
        404 -> "Resource not found."
        429 -> "Too many requests. Please try again later."
        in 500..599 -> "Server error. Please try again later."
        else -> "HTTP error $code occurred"
    }
}
