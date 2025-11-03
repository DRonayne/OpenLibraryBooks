package com.darach.openlibrarybooks.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.openlibrarybooks.core.domain.model.FilterOptions
import com.darach.openlibrarybooks.core.domain.model.Settings
import com.darach.openlibrarybooks.core.domain.model.SortOption
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import com.darach.openlibrarybooks.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Sealed interface representing the validation state of the username.
 *
 * Used to provide reactive UI feedback during username validation.
 */
sealed interface UsernameValidationState {
    /**
     * No validation in progress.
     */
    data object Idle : UsernameValidationState

    /**
     * Username validation is currently in progress.
     */
    data object Validating : UsernameValidationState

    /**
     * Username is valid and exists on Open Library.
     */
    data object Valid : UsernameValidationState

    /**
     * Username is invalid or does not exist on Open Library.
     *
     * @property errorMessage User-friendly error message explaining why validation failed
     */
    data class Invalid(val errorMessage: String) : UsernameValidationState
}

/**
 * ViewModel for managing the settings screen.
 *
 * Handles user settings including username validation, theme preferences,
 * sort/filter options, and cache management. Uses StateFlows to expose
 * reactive UI state to Compose screens.
 *
 * @property settingsRepository Repository for accessing and updating settings
 * @property booksRepository Repository for managing books cache
 * @property favouritesRepository Repository for managing favourites cache
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val booksRepository: BooksRepository,
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    /**
     * StateFlow exposing the current app settings.
     *
     * Updates reactively whenever any setting changes.
     */
    val settings: StateFlow<Settings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings(),
        )

    /**
     * StateFlow exposing the username validation state.
     *
     * Updates during validation to provide UI feedback (loading, success, error).
     */
    private val _validationState = MutableStateFlow<UsernameValidationState>(UsernameValidationState.Idle)
    val validationState: StateFlow<UsernameValidationState> = _validationState.asStateFlow()

    /**
     * StateFlow exposing whether the clear cache confirmation dialog should be shown.
     */
    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    /**
     * StateFlow exposing whether a cache clear operation is in progress.
     */
    private val _isClearingCache = MutableStateFlow(false)
    val isClearingCache: StateFlow<Boolean> = _isClearingCache.asStateFlow()

    /**
     * StateFlow exposing error messages for display in snackbars.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        Log.d(TAG, "SettingsViewModel initialised")
    }

    /**
     * Validate a username by attempting to fetch their reading lists from Open Library.
     *
     * Makes an API call to /people/{username}/books/want-to-read.json with limit=1.
     * Sets the validation state to Validating during the check, then Valid on success
     * (200 OK) or Invalid on failure (404 or other errors).
     *
     * Uses RxJava with a 10-second timeout to prevent hanging on slow connections.
     *
     * @param username The Open Library username to validate
     */
    fun validateUsername(username: String) {
        if (username.isBlank()) {
            _validationState.value = UsernameValidationState.Invalid("Username cannot be empty")
            Log.d(TAG, "Username validation failed: empty username")
            return
        }

        Log.d(TAG, "Starting username validation for: $username")
        _validationState.value = UsernameValidationState.Validating

        settingsRepository.validateUsername(username)
            .subscribeBy(
                onSuccess = { isValid ->
                    if (isValid) {
                        Log.i(TAG, "Username validation successful: $username")
                        _validationState.value = UsernameValidationState.Valid
                    } else {
                        Log.w(TAG, "Username validation failed: $username not found")
                        _validationState.value = UsernameValidationState.Invalid(
                            "Username '$username' not found on Open Library",
                        )
                    }
                },
                onError = { throwable ->
                    Log.e(TAG, "Username validation error for: $username", throwable)
                    val errorMessage = when (throwable) {
                        is java.util.concurrent.TimeoutException ->
                            "Validation timed out. Please check your internet connection."
                        is java.net.UnknownHostException,
                        is java.net.SocketTimeoutException,
                        -> "No internet connection. Please check your network."
                        is java.io.IOException -> "Network error. Please try again."
                        else -> "Validation failed: ${throwable.message ?: "Unknown error"}"
                    }
                    _validationState.value = UsernameValidationState.Invalid(errorMessage)
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Update the username after successful validation.
     *
     * Should only be called after the username has been validated via [validateUsername].
     * Resets the validation state after updating. Also clears all cache (books and favourites)
     * and triggers a fresh sync with the new username.
     *
     * @param username The new username to save
     */
    fun updateUsername(username: String) {
        Log.d(TAG, "Updating username to: $username")

        settingsRepository.updateUsername(username)
            .andThen(booksRepository.clearCache())
            .andThen(favouritesRepository.clearAllFavourites())
            .andThen(booksRepository.sync(username))
            .andThen(settingsRepository.updateLastSyncTimestamp(System.currentTimeMillis()))
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Username updated successfully to: $username and cache cleared")
                    _validationState.value = UsernameValidationState.Idle
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to update username and clear cache", throwable)
                    _errorMessage.value = "Failed to save username: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Toggle dark mode on or off.
     *
     * Updates the setting and provides immediate UI feedback through the settings StateFlow.
     *
     * @param enabled True to enable dark mode, false to disable
     */
    fun toggleDarkMode(enabled: Boolean) {
        Log.d(TAG, "Toggling dark mode: $enabled")

        settingsRepository.toggleDarkMode(enabled)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Dark mode updated successfully: $enabled")
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to toggle dark mode", throwable)
                    _errorMessage.value = "Failed to update dark mode: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Toggle dynamic theming (Material You) on or off.
     *
     * Updates the setting and provides immediate UI feedback through the settings StateFlow.
     *
     * @param enabled True to enable dynamic theming, false to disable
     */
    fun toggleDynamicTheme(enabled: Boolean) {
        Log.d(TAG, "Toggling dynamic theme: $enabled")

        settingsRepository.toggleDynamicTheme(enabled)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Dynamic theme updated successfully: $enabled")
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to toggle dynamic theme", throwable)
                    _errorMessage.value = "Failed to update dynamic theme: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Update the sort option for book lists.
     *
     * @param sortOption The new sort option to apply
     */
    fun updateSortOption(sortOption: SortOption) {
        Log.d(TAG, "Updating sort option: $sortOption")

        settingsRepository.updateSortOption(sortOption)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Sort option updated successfully: $sortOption")
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to update sort option", throwable)
                    _errorMessage.value = "Failed to update sort option: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Update the filter options for book lists.
     *
     * @param filterOptions The new filter options to apply
     */
    fun updateFilterOptions(filterOptions: FilterOptions) {
        Log.d(TAG, "Updating filter options: $filterOptions")

        settingsRepository.updateFilterOptions(filterOptions)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Filter options updated successfully")
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to update filter options", throwable)
                    _errorMessage.value = "Failed to update filter options: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Format the last sync timestamp for display in the UI.
     *
     * Converts a millisecond timestamp to a user-friendly formatted string.
     * Returns "Never" if the timestamp is 0 (no sync has occurred yet).
     *
     * @param timestamp The timestamp in milliseconds since epoch
     * @return Formatted date string or "Never" if no sync
     */
    fun formatLastSyncTimestamp(timestamp: Long): String {
        if (timestamp == 0L) {
            return "Never"
        }

        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to format timestamp: $timestamp", e)
            "Unknown"
        }
    }

    /**
     * Show the clear cache confirmation dialog.
     */
    fun showClearCacheDialog() {
        Log.d(TAG, "Showing clear cache dialog")
        _showClearCacheDialog.value = true
    }

    /**
     * Dismiss the clear cache confirmation dialog.
     */
    fun dismissClearCacheDialog() {
        Log.d(TAG, "Dismissing clear cache dialog")
        _showClearCacheDialog.value = false
    }

    /**
     * Clear all cached books and favourites.
     *
     * Displays a confirmation dialog state, then clears both books and favourites
     * from the local database. After clearing, triggers an automatic sync to
     * refetch fresh data from the API.
     *
     * Shows progress during the operation and handles errors gracefully.
     */
    fun clearCache() {
        Log.d(TAG, "Starting cache clear operation")
        _isClearingCache.value = true
        _showClearCacheDialog.value = false

        // Clear both books and favourites
        booksRepository.clearCache()
            .andThen(favouritesRepository.clearAllFavourites())
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Cache cleared successfully")
                    _isClearingCache.value = false

                    // Trigger auto-sync after clearing cache
                    val currentUsername = settings.value.username
                    if (currentUsername.isNotBlank()) {
                        Log.d(TAG, "Triggering auto-sync after cache clear for user: $currentUsername")
                        triggerAutoSync(currentUsername)
                    } else {
                        Log.w(TAG, "Cannot trigger auto-sync: no username set")
                    }
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to clear cache", throwable)
                    _isClearingCache.value = false
                    _errorMessage.value = "Failed to clear cache: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Trigger an automatic sync after clearing the cache.
     *
     * Performs a background sync to refetch books from the API and update
     * the last sync timestamp.
     *
     * @param username The Open Library username to sync for
     */
    private fun triggerAutoSync(username: String) {
        Log.d(TAG, "Starting auto-sync for user: $username")

        booksRepository.sync(username)
            .andThen(settingsRepository.updateLastSyncTimestamp(System.currentTimeMillis()))
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Auto-sync completed successfully")
                },
                onError = { throwable ->
                    Log.e(TAG, "Auto-sync failed", throwable)
                    _errorMessage.value = "Sync failed after clearing cache: ${throwable.message}"
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Reset the validation state to Idle.
     *
     * Useful for clearing validation state when the user closes a dialog
     * or navigates away from the username input.
     */
    fun resetValidationState() {
        Log.d(TAG, "Resetting validation state to Idle")
        _validationState.value = UsernameValidationState.Idle
    }

    /**
     * Clear the current error message.
     *
     * Typically called after showing the error in a snackbar.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Disposes of RxJava subscriptions when ViewModel is cleared.
     *
     * Prevents memory leaks by cleaning up all active subscriptions.
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, disposing RxJava subscriptions")
        compositeDisposable.dispose()
    }
}
