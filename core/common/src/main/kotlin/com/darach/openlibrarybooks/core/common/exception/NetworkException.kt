package com.darach.openlibrarybooks.core.common.exception

/**
 * Sealed hierarchy for network-related exceptions.
 *
 * This allows us to differentiate between different types of network failures
 * and provide appropriate UI feedback and retry strategies for each.
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Thrown when there's no internet connection available.
     *
     * UI should show "No internet connection" message and only allow retry when online.
     */
    class NoInternetException(message: String = "No internet connection", cause: Throwable? = null) :
        NetworkException(message, cause)

    /**
     * Thrown when a network request times out.
     *
     * UI should show "Request timed out" message and allow retry.
     */
    class TimeoutException(message: String = "Request timed out", cause: Throwable? = null) :
        NetworkException(message, cause)

    /**
     * Thrown when the API returns an error response (4xx or 5xx).
     *
     * UI should show appropriate message based on status code.
     *
     * @param statusCode The HTTP status code
     * @param message The error message
     */
    class ApiException(
        val statusCode: Int,
        message: String = "Server error (HTTP $statusCode)",
        cause: Throwable? = null,
    ) : NetworkException(message, cause) {
        val isClientError: Boolean get() = statusCode in 400..499
        val isServerError: Boolean get() = statusCode in 500..599
    }

    /**
     * Thrown for unexpected errors that don't fit other categories.
     *
     * UI should show "Something went wrong" message.
     */
    class UnknownException(message: String = "Something went wrong", cause: Throwable? = null) :
        NetworkException(message, cause)
}

/**
 * Maps a general [Throwable] to a specific [NetworkException] type.
 *
 * This helper function examines the exception and returns the appropriate
 * NetworkException subclass based on the error type.
 *
 * @param hasInternet Whether the device currently has internet connectivity
 * @return The mapped NetworkException
 */
fun Throwable.toNetworkException(hasInternet: Boolean = true): NetworkException = when (this) {
    // Check for no internet first - this is the most common case
    is java.net.UnknownHostException -> {
        if (!hasInternet) {
            NetworkException.NoInternetException(cause = this)
        } else {
            // DNS resolution failed even though we have internet
            NetworkException.ApiException(
                statusCode = 0,
                message = "Could not resolve host",
                cause = this,
            )
        }
    }

    // Timeout exceptions
    is java.net.SocketTimeoutException,
    is java.util.concurrent.TimeoutException,
    -> NetworkException.TimeoutException(cause = this)

    // Retrofit/HTTP exceptions
    is retrofit2.HttpException -> {
        NetworkException.ApiException(
            statusCode = code(),
            message = message(),
            cause = this,
        )
    }

    // IO exceptions
    is java.io.IOException -> {
        if (!hasInternet) {
            NetworkException.NoInternetException(cause = this)
        } else {
            NetworkException.UnknownException(
                message = "Network error: ${message ?: "Unknown"}",
                cause = this,
            )
        }
    }

    // Already a NetworkException, return as is
    is NetworkException -> this

    // Unknown error type
    else -> NetworkException.UnknownException(
        message = "Unexpected error: ${message ?: "Unknown"}",
        cause = this,
    )
}

/**
 * Gets a user-friendly error message for this exception.
 */
fun NetworkException.getUserMessage(): String = when (this) {
    is NetworkException.NoInternetException -> "No internet connection"
    is NetworkException.TimeoutException -> "Request timed out"
    is NetworkException.ApiException -> when {
        statusCode == 404 -> "Resource not found"
        isClientError -> "Invalid request"
        isServerError -> "Server error"
        else -> "Something went wrong"
    }
    is NetworkException.UnknownException -> "Something went wrong"
}

/**
 * Determines if this exception allows retry.
 */
fun NetworkException.canRetry(): Boolean = when (this) {
    is NetworkException.NoInternetException -> false // Only retry when online
    is NetworkException.TimeoutException -> true
    is NetworkException.ApiException -> isServerError // Only retry server errors
    is NetworkException.UnknownException -> true
}
