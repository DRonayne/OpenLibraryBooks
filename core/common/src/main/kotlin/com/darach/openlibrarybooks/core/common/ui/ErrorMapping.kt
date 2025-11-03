package com.darach.openlibrarybooks.core.common.ui

import android.content.Context
import com.darach.openlibrarybooks.core.common.R
import com.darach.openlibrarybooks.core.common.exception.NetworkException

/**
 * Maps a [NetworkException] to a user-friendly error message.
 *
 * @param context Android context for accessing string resources
 * @return A user-friendly error message suitable for display in the UI
 */
fun NetworkException.toUserMessage(context: Context): String = when (this) {
    is NetworkException.NoInternetException ->
        context.getString(R.string.error_no_internet)
    is NetworkException.TimeoutException ->
        context.getString(R.string.error_timeout)
    is NetworkException.ApiException -> when {
        statusCode == 404 -> context.getString(R.string.error_not_found)
        isClientError -> context.getString(R.string.error_invalid_request)
        isServerError -> context.getString(R.string.error_server_unavailable)
        else -> context.getString(R.string.error_generic)
    }
    is NetworkException.UnknownException ->
        context.getString(R.string.error_generic)
}

/**
 * Determines if this exception allows retry based on its type.
 *
 * @param isOnline Whether the device currently has internet connectivity
 * @return true if retry should be allowed, false otherwise
 */
fun NetworkException.shouldAllowRetry(isOnline: Boolean): Boolean = when (this) {
    is NetworkException.NoInternetException -> isOnline // Only retry when back online
    is NetworkException.TimeoutException -> true
    is NetworkException.ApiException -> isServerError // Only retry server errors
    is NetworkException.UnknownException -> true
}
