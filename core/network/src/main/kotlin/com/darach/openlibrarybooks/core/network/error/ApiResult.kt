package com.darach.openlibrarybooks.core.network.error

/**
 * Sealed class representing the result of an API call.
 *
 * Wraps successful responses and various error types to provide
 * type-safe error handling throughout the app.
 *
 * @param T The type of data expected on success
 */
sealed class ApiResult<out T> {
    /**
     * Successful API response with data.
     *
     * @param data The response data
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * API call failed with an error.
     *
     * @param exception The error that occurred
     * @param message Optional error message
     */
    data class Error(val exception: ApiException, val message: String? = exception.message) : ApiResult<Nothing>()

    /**
     * Helper to check if result is successful.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Helper to check if result is an error.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Get data if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Get error if failed, null otherwise.
     */
    fun errorOrNull(): Error? = this as? Error

    /**
     * Map the success data to another type.
     */
    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Execute action if successful.
     */
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Execute action if error occurred.
     */
    inline fun onError(action: (Error) -> Unit): ApiResult<T> {
        if (this is Error) action(this)
        return this
    }
}
