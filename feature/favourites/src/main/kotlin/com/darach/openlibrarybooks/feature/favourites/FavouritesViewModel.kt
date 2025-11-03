package com.darach.openlibrarybooks.feature.favourites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.openlibrarybooks.core.common.analytics.FirebaseAnalyticsHelper
import com.darach.openlibrarybooks.core.common.crashlytics.FirebaseCrashlyticsHelper
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for managing the favourites screen.
 *
 * Handles displaying favourite books, toggling favourite status with optimistic updates,
 * and clearing all favourites. Uses StateFlows to expose reactive UI state to Compose screens.
 *
 * Optimistic updates are used for toggling favourites to provide immediate UI feedback
 * before the database operation completes, with rollback on failure.
 *
 * @property favouritesRepository Repository for accessing favourites data
 */
@HiltViewModel
class FavouritesViewModel
@Inject
constructor(
    private val favouritesRepository: FavouritesRepository,
    private val analyticsHelper: FirebaseAnalyticsHelper,
    private val crashlyticsHelper: FirebaseCrashlyticsHelper,
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val TAG = "FavouritesViewModel"
    }

    /**
     * Main UI state exposing the list of favourite books.
     *
     * Emits UiState with the list of favourites, handling loading, success, error, and empty states.
     * Updates reactively whenever favourites change in the database.
     */
    val favouritesUiState: StateFlow<UiState<List<Book>>> = favouritesRepository.getFavourites()
        .map { books ->
            Log.d(TAG, "Favourites updated: ${books.size} books")
            if (books.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(books)
            }
        }
        .catch { throwable ->
            Log.e(TAG, "Error in favourites flow", throwable)
            crashlyticsHelper.recordViewModelError(
                viewModelName = "FavouritesViewModel",
                action = "load_favourites",
                throwable = throwable,
            )
            analyticsHelper.logError(
                errorType = "favourites_load_error",
                errorMessage = throwable.message ?: "Unknown error",
                screenName = FirebaseAnalyticsHelper.SCREEN_FAVOURITES,
            )
            emit(
                UiState.Error(
                    message = handleError(throwable),
                    throwable = throwable,
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading,
        )

    /**
     * StateFlow tracking favourite status for individual books by their composite key.
     *
     * Maps book IDs to their favourite status (true if favourited, false otherwise).
     * Used for displaying favourite status in the UI and implementing optimistic updates.
     */
    private val _favouriteStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favouriteStatusMap: StateFlow<Map<String, Boolean>> = _favouriteStatusMap.asStateFlow()

    /**
     * Favourite count exposed as a StateFlow.
     *
     * Updates reactively whenever the number of favourites changes.
     */
    val favouriteCount: StateFlow<Int> = favouritesRepository.getFavouriteCount()
        .catch { throwable ->
            Log.e(TAG, "Error fetching favourite count", throwable)
            emit(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0,
        )

    /**
     * Toggle the favourite status of a book with optimistic updates.
     *
     * Immediately updates the UI by modifying the favourite status map, then performs
     * the database operation asynchronously. If the operation fails, the UI state is
     * rolled back to maintain consistency.
     *
     * @param bookId The composite key of the book to toggle
     */
    fun toggleFavourite(bookId: String, bookTitle: String = "") {
        // Optimistic update: immediately update the UI state
        val currentStatus = _favouriteStatusMap.value[bookId] ?: false
        val newStatus = !currentStatus
        _favouriteStatusMap.value = _favouriteStatusMap.value + (bookId to newStatus)
        Log.d(TAG, "Optimistically toggled favourite for $bookId: $currentStatus -> $newStatus")

        // Perform the actual database operation
        favouritesRepository.toggleFavourite(bookId)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Successfully toggled favourite for book: $bookId")
                    // Log analytics event based on the new status
                    if (newStatus) {
                        analyticsHelper.logBookFavourited(bookId, bookTitle)
                    } else {
                        analyticsHelper.logBookUnfavourited(bookId, bookTitle)
                    }
                    // The favouritesUiState will update automatically from the repository Flow
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to toggle favourite for book: $bookId", throwable)
                    crashlyticsHelper.recordBookError(
                        bookId = bookId,
                        action = "toggle_favourite",
                        throwable = throwable,
                    )
                    analyticsHelper.logError(
                        errorType = "toggle_favourite_error",
                        errorMessage = throwable.message ?: "Unknown error",
                        screenName = FirebaseAnalyticsHelper.SCREEN_FAVOURITES,
                    )
                    // Rollback the optimistic update on error
                    _favouriteStatusMap.value = _favouriteStatusMap.value + (bookId to currentStatus)
                    Log.w(TAG, "Rolled back optimistic update for $bookId to $currentStatus")
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Clear all favourite books from the database.
     *
     * Removes all books from the favourites list. This operation cannot be undone.
     * Shows an error if the operation fails.
     */
    fun clearAllFavourites() {
        Log.d(TAG, "Clearing all favourites")
        val currentCount = favouriteCount.value

        favouritesRepository.clearAllFavourites()
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Successfully cleared all favourites")
                    analyticsHelper.logEvent("favourites_cleared") {
                        putInt("count", currentCount)
                    }
                    _favouriteStatusMap.value = emptyMap()
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to clear all favourites", throwable)
                    crashlyticsHelper.recordViewModelError(
                        viewModelName = "FavouritesViewModel",
                        action = "clear_all_favourites",
                        throwable = throwable,
                    )
                    analyticsHelper.logError(
                        errorType = "clear_favourites_error",
                        errorMessage = throwable.message ?: "Unknown error",
                        screenName = FirebaseAnalyticsHelper.SCREEN_FAVOURITES,
                    )
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Check if a book is favourited and update the status map.
     *
     * Useful for checking individual book status when needed (e.g., when navigating
     * to a book details screen).
     *
     * @param bookId The composite key of the book to check
     */
    fun checkFavouriteStatus(bookId: String) {
        favouritesRepository.isFavourite(bookId)
            .subscribeBy(
                onSuccess = { isFavourite ->
                    Log.d(TAG, "Favourite status for $bookId: $isFavourite")
                    _favouriteStatusMap.value = _favouriteStatusMap.value + (bookId to isFavourite)
                },
                onError = { throwable ->
                    Log.e(TAG, "Failed to check favourite status for $bookId", throwable)
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Handles errors from the favourites flow and converts them to user-friendly messages.
     *
     * @param throwable The exception that occurred
     * @return User-friendly error message
     */
    private fun handleError(throwable: Throwable): String = if (throwable is java.io.IOException) {
        "Failed to load favourites from database."
    } else {
        "Failed to load favourites: ${throwable.message ?: "Unknown error"}"
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
