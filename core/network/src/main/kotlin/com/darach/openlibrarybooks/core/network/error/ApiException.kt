package com.darach.openlibrarybooks.core.network.error

/**
 * Sealed class hierarchy for API-related exceptions.
 *
 * Categorises errors into distinct types for better error handling
 * and user messaging throughout the app.
 */
sealed class ApiException(message: String) : Exception(message) {

    /**
     * No internet connection available.
     *
     * Occurs when the device has no network connectivity.
     */
    class NoInternetException(message: String = "No internet connection available") : ApiException(message)

    /**
     * Request timeout occurred.
     *
     * Occurs when the API request takes too long to complete.
     * Could be due to slow connection or server issues.
     */
    class TimeoutException(message: String = "Request timed out") : ApiException(message)

    /**
     * Server-side error occurred.
     *
     * Occurs when the server returns an error response (4xx or 5xx HTTP codes).
     *
     * @param code HTTP status code
     * @param serverMessage Optional error message from server
     */
    class ServerException(val code: Int, val serverMessage: String? = null) :
        ApiException(
            serverMessage ?: "Server error: $code",
        ) {
        /**
         * Checks if error is a client error (4xx).
         */
        val isClientError: Boolean
            get() = code in 400..499

        /**
         * Checks if error is a server error (5xx).
         */
        val isServerError: Boolean
            get() = code in 500..599
    }

    /**
     * Unknown or unexpected error occurred.
     *
     * Catch-all for errors that don't fit other categories.
     * Wraps the original exception for debugging.
     */
    class UnknownException(message: String = "An unknown error occurred", override val cause: Throwable? = null) :
        ApiException(message)
}
