package com.darach.openlibrarybooks.core.common.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for Firebase Analytics tracking throughout the app.
 * Provides a clean interface for logging events with proper error handling.
 */
@Singleton
@Suppress("TooManyFunctions") // Analytics helper needs many methods for different event types
class FirebaseAnalyticsHelper @Inject constructor(private val analytics: FirebaseAnalytics) {

    companion object {
        private const val TAG = "FirebaseAnalytics"

        // Screen names
        const val SCREEN_BOOKS = "books_screen"
        const val SCREEN_FAVOURITES = "favourites_screen"
        const val SCREEN_SETTINGS = "settings_screen"
        const val SCREEN_BOOK_DETAILS = "book_details_screen"

        // Event names
        const val EVENT_APP_OPEN = "app_open"
        const val EVENT_BOOK_VIEWED = "book_viewed"
        const val EVENT_BOOK_FAVOURITED = "book_favourited"
        const val EVENT_BOOK_UNFAVOURITED = "book_unfavourited"
        const val EVENT_BOOKS_FILTERED = "books_filtered"
        const val EVENT_BOOKS_SORTED = "books_sorted"
        const val EVENT_BOOKS_SYNCED = "books_synced"
        const val EVENT_SEARCH_PERFORMED = "search_performed"
        const val EVENT_THEME_CHANGED = "theme_changed"
        const val EVENT_USERNAME_UPDATED = "username_updated"
        const val EVENT_WIDGET_UPDATED = "widget_updated"
        const val EVENT_ERROR_OCCURRED = "error_occurred"

        // Parameter names
        const val PARAM_BOOK_ID = "book_id"
        const val PARAM_BOOK_TITLE = "book_title"
        const val PARAM_FILTER_TYPE = "filter_type"
        const val PARAM_SORT_TYPE = "sort_type"
        const val PARAM_SEARCH_QUERY = "search_query"
        const val PARAM_THEME = "theme"
        const val PARAM_ERROR_TYPE = "error_type"
        const val PARAM_ERROR_MESSAGE = "error_message"
        const val PARAM_SYNC_SUCCESS = "sync_success"
        const val PARAM_BOOKS_COUNT = "books_count"
        const val PARAM_SCREEN_NAME = "screen_name"
    }

    /**
     * Logs a screen view event.
     * Automatically tracked if enabled in AndroidManifest, but can be called manually for consistency.
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        try {
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
            }
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
            Log.d(TAG, "Screen view logged: $screenName")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.e(TAG, "Failed to log screen view: $screenName", e)
        }
    }

    /**
     * Logs when the app is opened.
     */
    fun logAppOpen() {
        logEvent(EVENT_APP_OPEN)
    }

    /**
     * Logs when a book is viewed by the user.
     */
    fun logBookViewed(bookId: String, bookTitle: String) {
        logEvent(EVENT_BOOK_VIEWED) {
            putString(PARAM_BOOK_ID, bookId)
            putString(PARAM_BOOK_TITLE, bookTitle.take(100)) // Limit length
        }
    }

    /**
     * Logs when a book is added to favourites.
     */
    fun logBookFavourited(bookId: String, bookTitle: String) {
        logEvent(EVENT_BOOK_FAVOURITED) {
            putString(PARAM_BOOK_ID, bookId)
            putString(PARAM_BOOK_TITLE, bookTitle.take(100))
        }
    }

    /**
     * Logs when a book is removed from favourites.
     */
    fun logBookUnfavourited(bookId: String, bookTitle: String) {
        logEvent(EVENT_BOOK_UNFAVOURITED) {
            putString(PARAM_BOOK_ID, bookId)
            putString(PARAM_BOOK_TITLE, bookTitle.take(100))
        }
    }

    /**
     * Logs when books are filtered.
     */
    fun logBooksFiltered(filterType: String) {
        logEvent(EVENT_BOOKS_FILTERED) {
            putString(PARAM_FILTER_TYPE, filterType)
        }
    }

    /**
     * Logs when books are sorted.
     */
    fun logBooksSorted(sortType: String) {
        logEvent(EVENT_BOOKS_SORTED) {
            putString(PARAM_SORT_TYPE, sortType)
        }
    }

    /**
     * Logs when books sync operation completes.
     */
    fun logBooksSynced(success: Boolean, booksCount: Int) {
        logEvent(EVENT_BOOKS_SYNCED) {
            putBoolean(PARAM_SYNC_SUCCESS, success)
            putInt(PARAM_BOOKS_COUNT, booksCount)
        }
    }

    /**
     * Logs when a search is performed.
     */
    fun logSearchPerformed(query: String) {
        logEvent(EVENT_SEARCH_PERFORMED) {
            putString(PARAM_SEARCH_QUERY, query.take(100))
        }
    }

    /**
     * Logs when the app theme is changed.
     */
    fun logThemeChanged(theme: String) {
        logEvent(EVENT_THEME_CHANGED) {
            putString(PARAM_THEME, theme)
        }
    }

    /**
     * Logs when the username is updated.
     */
    fun logUsernameUpdated() {
        logEvent(EVENT_USERNAME_UPDATED)
    }

    /**
     * Logs when the widget is updated.
     */
    fun logWidgetUpdated(booksCount: Int) {
        logEvent(EVENT_WIDGET_UPDATED) {
            putInt(PARAM_BOOKS_COUNT, booksCount)
        }
    }

    /**
     * Logs when an error occurs in the app.
     */
    fun logError(errorType: String, errorMessage: String, screenName: String? = null) {
        logEvent(EVENT_ERROR_OCCURRED) {
            putString(PARAM_ERROR_TYPE, errorType)
            putString(PARAM_ERROR_MESSAGE, errorMessage.take(100))
            screenName?.let { putString(PARAM_SCREEN_NAME, it) }
        }
    }

    /**
     * Sets a user property for analytics segmentation.
     */
    fun setUserProperty(name: String, value: String) {
        try {
            analytics.setUserProperty(name, value)
            Log.d(TAG, "User property set: $name = $value")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.e(TAG, "Failed to set user property: $name", e)
        }
    }

    /**
     * Sets the user ID for analytics.
     */
    fun setUserId(userId: String?) {
        try {
            analytics.setUserId(userId)
            Log.d(TAG, "User ID set: $userId")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.e(TAG, "Failed to set user ID", e)
        }
    }

    /**
     * Generic event logging with optional parameters builder.
     * Can be used for custom events not covered by the specific methods above.
     */
    fun logEvent(eventName: String, params: (Bundle.() -> Unit)? = null) {
        try {
            val bundle = Bundle().apply {
                params?.invoke(this)
            }
            analytics.logEvent(eventName, bundle)
            Log.d(TAG, "Event logged: $eventName")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.e(TAG, "Failed to log event: $eventName", e)
        }
    }
}
