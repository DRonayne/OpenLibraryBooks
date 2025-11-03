package com.darach.openlibrarybooks.feature.books

import android.util.Log
import androidx.lifecycle.ViewModel
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.domain.model.EditionDetails
import com.darach.openlibrarybooks.core.domain.model.WorkDetails
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for managing the book details bottom sheet.
 *
 * Fetches detailed work information from the repository, manages favourite status,
 * and coordinates loading/error states. Uses RxJava for data fetching and StateFlows
 * for reactive UI state.
 *
 * @property booksRepository Repository for accessing work and edition details
 * @property favouritesRepository Repository for managing favourite books
 */
@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val TAG = "BookDetailsViewModel"
    }

    // Current book being displayed
    private var currentWorkId: String? = null
    private var currentEditionId: String? = null
    private var currentBookId: String? = null // Composite key for favorite operations

    // UI state for work details
    private val _workDetailsState = MutableStateFlow<UiState<WorkDetails>>(UiState.Idle)
    val workDetailsState: StateFlow<UiState<WorkDetails>> = _workDetailsState.asStateFlow()

    // UI state for edition details (optional)
    private val _editionDetailsState = MutableStateFlow<UiState<EditionDetails?>>(UiState.Idle)
    val editionDetailsState: StateFlow<UiState<EditionDetails?>> = _editionDetailsState.asStateFlow()

    // Favourite status
    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite.asStateFlow()

    /**
     * Load book details for a specific work.
     *
     * @param workId The work ID to load details for (e.g., "OL45804W")
     * @param editionId Optional edition ID to load additional edition details
     * @param bookId The book's composite key (title_author) for favorite operations
     */
    fun loadBookDetails(workId: String, editionId: String? = null, bookId: String? = null) {
        Log.i(TAG, "Loading book details for workId: $workId, editionId: $editionId, bookId: $bookId")
        currentWorkId = workId
        currentEditionId = editionId
        currentBookId = bookId
        loadWorkDetails()
        editionId?.let { loadEditionDetails(it) }
        checkFavouriteStatus()
    }

    /**
     * Load work details from the repository.
     * Updates the UI state based on success or failure.
     */
    private fun loadWorkDetails() {
        val workId = currentWorkId ?: run {
            Log.e(TAG, "Cannot load work details: workId is null")
            return
        }

        Log.d(TAG, "Loading work details for: $workId")
        _workDetailsState.value = UiState.Loading

        booksRepository.getWorkDetails(workId)
            .subscribeBy(
                onSuccess = { workDetails ->
                    Log.i(TAG, "Successfully loaded work details: ${workDetails.title}")
                    _workDetailsState.value = UiState.Success(workDetails)
                },
                onError = { error ->
                    Log.e(TAG, "Failed to load work details for $workId", error)
                    _workDetailsState.value = UiState.Error(
                        message = "Failed to load book details. Please try again.",
                        throwable = error,
                    )
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Load edition details from the repository (optional).
     * Updates the UI state based on success or failure.
     *
     * @param editionKey The edition key to load details for
     */
    private fun loadEditionDetails(editionKey: String) {
        Log.d(TAG, "Loading edition details for: $editionKey")
        _editionDetailsState.value = UiState.Loading

        booksRepository.getEditionDetails(editionKey)
            .subscribeBy(
                onSuccess = { editionDetails ->
                    Log.i(TAG, "Successfully loaded edition details: ${editionDetails.title}")
                    _editionDetailsState.value = UiState.Success(editionDetails)
                },
                onError = { error ->
                    Log.w(TAG, "Failed to load edition details for $editionKey (non-critical)", error)
                    // Don't show error for edition details as it's optional
                    _editionDetailsState.value = UiState.Success(null)
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Check if the current book is marked as favourite.
     * Uses the book's composite key (title_author) for checking favorite status.
     */
    private fun checkFavouriteStatus() {
        val bookId = currentBookId ?: run {
            Log.w(TAG, "Cannot check favourite status: bookId is null")
            _isFavourite.value = false
            return
        }

        favouritesRepository.isFavourite(bookId)
            .subscribeBy(
                onSuccess = { isFav ->
                    _isFavourite.value = isFav
                    Log.d(TAG, "Favourite status for $bookId: $isFav")
                },
                onError = { error ->
                    Log.w(TAG, "Failed to check favourite status for $bookId", error)
                    // Default to false on error
                    _isFavourite.value = false
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Toggle favourite status for the current book.
     * Uses the book's composite key (title_author) for toggling favorite status.
     */
    fun toggleFavourite() {
        val bookId = currentBookId ?: run {
            Log.e(TAG, "Cannot toggle favourite: bookId is null")
            return
        }

        Log.d(TAG, "Toggling favourite status for bookId: $bookId")

        // Store current value for potential revert
        val previousValue = _isFavourite.value

        // Optimistic update - update UI immediately
        _isFavourite.value = !previousValue

        favouritesRepository.toggleFavourite(bookId)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Successfully toggled favourite status for $bookId to ${_isFavourite.value}")
                },
                onError = { error ->
                    Log.e(TAG, "Failed to toggle favourite status for $bookId", error)
                    // Revert optimistic update on error
                    _isFavourite.value = previousValue
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Retry loading work details after a failure.
     */
    fun retry() {
        val workId = currentWorkId
        val editionId = currentEditionId
        Log.i(TAG, "Retrying work details load for: $workId")
        loadWorkDetails()
        editionId?.let { loadEditionDetails(it) }
    }

    /**
     * Clean up RxJava subscriptions when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        Log.d(TAG, "BookDetailsViewModel cleared, disposed subscriptions")
    }
}
