package com.darach.openlibrarybooks.core.common.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for Firebase Crashlytics tracking throughout the app.
 * Provides crash reporting, non-fatal error tracking, and custom logging.
 */
@Singleton
@Suppress(
    "TooManyFunctions",
    "TooGenericExceptionCaught",
) // Helper needs many methods and catches all for graceful fallback
class FirebaseCrashlyticsHelper @Inject constructor(private val crashlytics: FirebaseCrashlytics) {

    companion object {
        private const val TAG = "FirebaseCrashlytics"

        // Custom keys
        const val KEY_SCREEN = "screen"
        const val KEY_USER_ACTION = "user_action"
        const val KEY_API_ENDPOINT = "api_endpoint"
        const val KEY_BOOK_ID = "book_id"
        const val KEY_SYNC_STATUS = "sync_status"
        const val KEY_FILTER_TYPE = "filter_type"
        const val KEY_SORT_TYPE = "sort_type"
    }

    /**
     * Records a non-fatal exception to Crashlytics.
     * Use this for caught exceptions that you want to track.
     */
    fun recordException(throwable: Throwable, message: String? = null) {
        try {
            message?.let { crashlytics.log(it) }
            crashlytics.recordException(throwable)
            Log.d(TAG, "Exception recorded: ${throwable.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record exception", e)
        }
    }

    /**
     * Logs a message to Crashlytics.
     * These logs are attached to crash reports for debugging context.
     */
    fun log(message: String) {
        try {
            crashlytics.log(message)
            Log.d(TAG, "Log message: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log message", e)
        }
    }

    /**
     * Sets a custom key-value pair for crash reports.
     * Use this to add context about the app state when crashes occur.
     */
    fun setCustomKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
            Log.d(TAG, "Custom key set: $key = $value")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: $key", e)
        }
    }

    /**
     * Sets a custom integer key-value pair.
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics.setCustomKey(key, value)
            Log.d(TAG, "Custom key set: $key = $value")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: $key", e)
        }
    }

    /**
     * Sets a custom boolean key-value pair.
     */
    fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics.setCustomKey(key, value)
            Log.d(TAG, "Custom key set: $key = $value")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: $key", e)
        }
    }

    /**
     * Sets the user identifier for crash reports.
     * This helps track which users are experiencing issues.
     */
    fun setUserId(userId: String?) {
        try {
            crashlytics.setUserId(userId ?: "anonymous")
            Log.d(TAG, "User ID set: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user ID", e)
        }
    }

    /**
     * Records an error with screen context.
     */
    fun recordScreenError(screen: String, throwable: Throwable) {
        setCustomKey(KEY_SCREEN, screen)
        recordException(throwable, "Error on screen: $screen")
    }

    /**
     * Records an API error with endpoint context.
     */
    fun recordApiError(endpoint: String, throwable: Throwable) {
        setCustomKey(KEY_API_ENDPOINT, endpoint)
        recordException(throwable, "API error at: $endpoint")
    }

    /**
     * Records a book-related error with context.
     */
    fun recordBookError(bookId: String, action: String, throwable: Throwable) {
        setCustomKey(KEY_BOOK_ID, bookId)
        setCustomKey(KEY_USER_ACTION, action)
        recordException(throwable, "Book error - ID: $bookId, Action: $action")
    }

    /**
     * Records a sync error with status.
     */
    fun recordSyncError(status: String, throwable: Throwable) {
        setCustomKey(KEY_SYNC_STATUS, status)
        recordException(throwable, "Sync error - Status: $status")
    }

    /**
     * Enables/disables Crashlytics collection.
     * Useful for respecting user privacy preferences.
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        try {
            crashlytics.setCrashlyticsCollectionEnabled(enabled)
            Log.d(TAG, "Crashlytics collection enabled: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set collection enabled state", e)
        }
    }

    /**
     * Sends any unsent crash reports.
     * Useful for forcing upload before the app closes.
     */
    fun sendUnsentReports() {
        try {
            crashlytics.sendUnsentReports()
            Log.d(TAG, "Unsent reports sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send unsent reports", e)
        }
    }

    /**
     * Records a ViewModel error with context.
     */
    fun recordViewModelError(viewModelName: String, action: String, throwable: Throwable) {
        log("ViewModel: $viewModelName, Action: $action")
        setCustomKey("view_model", viewModelName)
        setCustomKey(KEY_USER_ACTION, action)
        recordException(throwable)
    }

    /**
     * Records a repository error with context.
     */
    fun recordRepositoryError(repositoryName: String, operation: String, throwable: Throwable) {
        log("Repository: $repositoryName, Operation: $operation")
        setCustomKey("repository", repositoryName)
        setCustomKey("operation", operation)
        recordException(throwable)
    }

    /**
     * Records a worker error with context.
     */
    fun recordWorkerError(workerName: String, throwable: Throwable) {
        log("Worker: $workerName failed")
        setCustomKey("worker", workerName)
        recordException(throwable)
    }
}
