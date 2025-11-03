package com.darach.openlibrarybooks.core.common.ui

/**
 * Sealed interface representing the state of a UI component or screen.
 *
 * This pattern provides a type-safe way to represent different states
 * of data loading and presentation in the UI layer.
 *
 * @param T The type of data being managed by this state
 */
sealed interface UiState<out T> {
    /**
     * Initial state before any action has been taken.
     * No data has been requested or loaded yet.
     */
    data object Idle : UiState<Nothing>

    /**
     * Loading state indicating that data is being fetched.
     * UI should display a loading indicator.
     */
    data object Loading : UiState<Nothing>

    /**
     * Success state with the loaded data.
     *
     * @property data The successfully loaded data
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Error state indicating that an error occurred during data loading.
     *
     * @property message Human-readable error message
     * @property throwable The underlying exception (optional)
     */
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>

    /**
     * Empty state indicating that the operation succeeded but returned no data.
     * Useful for distinguishing between loading errors and legitimately empty results.
     */
    data object Empty : UiState<Nothing>
}

/**
 * Extension function to check if the state is loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension function to check if the state is success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension function to check if the state is error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Extension function to check if the state is empty
 */
fun <T> UiState<T>.isEmpty(): Boolean = this is UiState.Empty

/**
 * Extension function to get data if state is success, null otherwise
 */
fun <T> UiState<T>.dataOrNull(): T? = (this as? UiState.Success)?.data
