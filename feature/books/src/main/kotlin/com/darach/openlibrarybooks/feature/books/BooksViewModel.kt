package com.darach.openlibrarybooks.feature.books

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.FilterOptions
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.SortOption
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.feature.books.BookFilters.applyFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the books list screen.
 *
 * Handles fetching books from the repository, applying filters and sorting,
 * and coordinating pull-to-refresh operations. Uses StateFlows to expose
 * reactive UI state to Compose screens.
 *
 * @property booksRepository Repository for accessing book data
 */
@HiltViewModel
class BooksViewModel @Inject constructor(private val booksRepository: BooksRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val TAG = "BooksViewModel"
    }

    // Mutable state for filters and sorting
    private val _filterOptions = MutableStateFlow(FilterOptions(readingStatuses = setOf(ReadingStatus.WantToRead)))
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val _sortOption = MutableStateFlow<SortOption>(SortOption.DateAddedNewest)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Shared flow for refresh triggers with debouncing
    private val refreshTrigger = MutableSharedFlow<String>()

    // Error messages for snackbar display
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Main UI state exposing the filtered and sorted book list.
     *
     * Combines the repository Flow with filter and sort options to produce
     * a reactive stream of UI state that updates whenever books change or
     * filters are applied.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val booksUiState: StateFlow<UiState<List<Book>>> = combine(
        _filterOptions,
        _sortOption,
    ) { filters, sort ->
        Pair(filters, sort)
    }.flatMapLatest { (filters, sort) ->
        booksRepository.getBooks()
            .map { books ->
                val filtered = books.applyFilters(filters)
                val sorted = applySorting(filtered, sort)
                Log.d(
                    TAG,
                    "Books filtered and sorted: ${books.size} total -> " +
                        "${filtered.size} filtered -> ${sorted.size} sorted (sort: $sort)",
                )
                when {
                    sorted.isEmpty() && books.isEmpty() -> UiState.Empty
                    sorted.isEmpty() -> UiState.Success(emptyList())
                    else -> UiState.Success(sorted)
                }
            }
            .catch { throwable ->
                Log.e(TAG, "Error in books flow", throwable)
                emit(
                    UiState.Error(
                        message = handleError(throwable),
                        throwable = throwable,
                    ),
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading,
    )

    /**
     * Exposes all unique authors from cached books for filtering.
     */
    val availableAuthors: StateFlow<List<String>> = booksRepository.getBooks()
        .map { books ->
            books.flatMap { it.authors }
                .distinct()
                .sorted()
        }
        .catch { throwable ->
            Log.e(TAG, "Error fetching available authors", throwable)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    /**
     * Exposes all unique subjects from cached books for filtering.
     */
    val availableSubjects: StateFlow<List<String>> = booksRepository.getBooks()
        .map { books ->
            books.flatMap { it.subjects }
                .distinct()
                .sorted()
        }
        .catch { throwable ->
            Log.e(TAG, "Error fetching available subjects", throwable)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    /**
     * Exposes the year range from cached books for the range slider.
     * Returns a Pair of (minYear, maxYear). Defaults to (1900, current year) if no books.
     */
    val yearRange: StateFlow<Pair<Int, Int>> = booksRepository.getBooks()
        .map { books ->
            val years = books.mapNotNull { it.publishYear }
            if (years.isNotEmpty()) {
                Pair(years.minOrNull() ?: 1900, years.maxOrNull() ?: 2024)
            } else {
                Pair(1900, 2024)
            }
        }
        .catch { throwable ->
            Log.e(TAG, "Error fetching year range", throwable)
            emit(Pair(1900, 2024))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Pair(1900, 2024),
        )

    init {
        setupRefreshHandler()
    }

    /**
     * Sets up the debounced refresh handler.
     *
     * Listens to refresh triggers and debounces them to prevent multiple
     * simultaneous API calls within 500ms.
     */
    @OptIn(FlowPreview::class)
    private fun setupRefreshHandler() {
        viewModelScope.launch {
            refreshTrigger
                .debounce(500)
                .collect { username ->
                    performRefresh(username)
                }
        }
        Log.i(TAG, "Refresh handler setup complete with 500ms debounce")
    }

    /**
     * Triggers a pull-to-refresh operation.
     *
     * Debounced to 500ms to prevent multiple simultaneous API calls.
     * Updates the refresh loading state and shows error snackbar on failure.
     *
     * @param username Open Library username to sync books for
     */
    fun refresh(username: String) {
        Log.d(TAG, "Refresh triggered for username: $username")
        viewModelScope.launch {
            refreshTrigger.emit(username)
        }
    }

    /**
     * Performs the actual refresh operation by syncing with the repository.
     *
     * @param username Open Library username to sync books for
     */
    private fun performRefresh(username: String) {
        if (_isRefreshing.value) {
            Log.d(TAG, "Refresh already in progress for username: $username, skipping")
            return
        }

        Log.d(TAG, "Starting refresh for username: $username")
        _isRefreshing.value = true
        _errorMessage.value = null

        booksRepository.sync(username)
            .subscribeBy(
                onComplete = {
                    Log.i(TAG, "Refresh completed successfully for username: $username")
                    _isRefreshing.value = false
                },
                onError = { throwable ->
                    Log.e(TAG, "Refresh failed for username: $username", throwable)
                    _isRefreshing.value = false
                    _errorMessage.value = handleRefreshError(throwable)
                },
            )
            .addTo(compositeDisposable)
    }

    /**
     * Updates the filter options.
     *
     * Triggers immediate UI updates as the filtered book list is recalculated
     * reactively through the combined flow.
     *
     * @param filters New filter options to apply
     */
    fun updateFilters(filters: FilterOptions) {
        Log.d(TAG, "Filter options updated: statuses=${filters.readingStatuses}, isFavourite=${filters.isFavorite}")
        _filterOptions.value = filters
    }

    /**
     * Updates the sort option.
     *
     * Triggers immediate UI updates as the sorted book list is recalculated
     * reactively through the combined flow.
     *
     * @param sort New sort option to apply
     */
    fun updateSort(sort: SortOption) {
        Log.d(TAG, "Sort option updated: $sort")
        _sortOption.value = sort
    }

    /**
     * Clears the current error message (typically after showing it in a snackbar).
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Applies sorting to the book list based on the selected sort option.
     *
     * Uses Kotlin comparators to sort the list efficiently. Handles null values
     * in publish year by placing them at the end of sorted lists.
     *
     * @param books List of books to sort
     * @param sort Sort option to apply
     * @return Sorted list of books
     */
    private fun applySorting(books: List<Book>, sort: SortOption): List<Book> = when (sort) {
        is SortOption.TitleAscending -> {
            books.sortedBy { it.title.lowercase() }
        }
        is SortOption.TitleDescending -> {
            books.sortedByDescending { it.title.lowercase() }
        }
        is SortOption.AuthorAscending -> {
            books.sortedBy { book ->
                book.authors.firstOrNull()?.lowercase() ?: ""
            }
        }
        is SortOption.AuthorDescending -> {
            books.sortedByDescending { book ->
                book.authors.firstOrNull()?.lowercase() ?: ""
            }
        }
        is SortOption.PublishYearNewest -> {
            books.sortedByDescending { it.publishYear ?: Int.MIN_VALUE }
        }
        is SortOption.PublishYearOldest -> {
            books.sortedBy { it.publishYear ?: Int.MAX_VALUE }
        }
        is SortOption.DateAddedNewest -> {
            books.sortedByDescending { it.dateAdded }
        }
        is SortOption.DateAddedOldest -> {
            books.sortedBy { it.dateAdded }
        }
    }

    /**
     * Handles errors from the books flow and converts them to user-friendly messages.
     *
     * Differentiates between network errors, parsing errors, and other exceptions.
     *
     * @param throwable The exception that occurred
     * @return User-friendly error message
     */
    private fun handleError(throwable: Throwable): String = when (throwable) {
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        -> "No internet connection. Showing cached books."
        is java.io.IOException -> "Network error. Showing cached books."
        else -> "Failed to load books: ${throwable.message ?: "Unknown error"}"
    }

    /**
     * Handles errors from refresh operations and converts them to user-friendly messages.
     *
     * These errors are shown in snackbars, while keeping the existing book data visible.
     *
     * @param throwable The exception that occurred during refresh
     * @return User-friendly error message for snackbar
     */
    private fun handleRefreshError(throwable: Throwable): String = when (throwable) {
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        -> "No internet connection. Can't refresh books."
        is java.io.IOException -> "Network error. Failed to refresh books."
        else -> "Failed to refresh: ${throwable.message ?: "Unknown error"}"
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
