@file:Suppress("MatchingDeclarationName")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.darach.openlibrarybooks.feature.books

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.designsystem.component.BookCard
import com.darach.openlibrarybooks.core.designsystem.component.BookCardSkeleton
import com.darach.openlibrarybooks.core.designsystem.component.EmptyState
import com.darach.openlibrarybooks.core.designsystem.component.EmptyStateType
import com.darach.openlibrarybooks.core.designsystem.component.ErrorState
import com.darach.openlibrarybooks.core.designsystem.component.ErrorType
import com.darach.openlibrarybooks.core.designsystem.component.OfflineIndicator
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus

/**
 * Callbacks for the books screen.
 *
 * @property onBookClick Callback when a book is clicked
 * @property onFilterClick Callback when filter button is clicked
 * @property onSortClick Callback when sort button is clicked
 */
data class BooksScreenCallbacks(
    val onBookClick: (Book) -> Unit = {},
    val onFilterClick: () -> Unit = {},
    val onSortClick: () -> Unit = {},
)

/**
 * State holder for books screen content.
 */
private data class BooksContentState(
    val booksUiState: UiState<List<Book>>,
    val isRefreshing: Boolean,
    val isOffline: Boolean,
    val snackbarHostState: SnackbarHostState,
)

/**
 * Books screen - displays the user's personal reading collection.
 *
 * Features:
 * - Adaptive grid layout that adjusts for different screen sizes
 * - Pull-to-refresh for syncing with Open Library API
 * - Loading states with skeleton screens
 * - Error states with retry functionality
 * - Empty states for no books
 * - Smooth animations for item placement
 * - Offline indicator
 * - Filter and sort buttons
 *
 * @param modifier Modifier to be applied to the screen
 * @param viewModel ViewModel for managing books state
 * @param username Open Library username for syncing (defaults to placeholder)
 * @param callbacks Callbacks for user interactions
 */
@Composable
fun BooksScreen(
    modifier: Modifier = Modifier,
    viewModel: BooksViewModel = hiltViewModel(),
    username: String = "mekBot",
    callbacks: BooksScreenCallbacks = BooksScreenCallbacks(),
) {
    val booksUiState by viewModel.booksUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger initial book fetch when screen is first displayed
    LaunchedEffect(username) {
        viewModel.refresh(username)
    }

    // Handle error messages
    HandleErrorMessages(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onClearError = viewModel::clearErrorMessage,
    )

    // Determine if offline based on UI state
    val isOffline = booksUiState is UiState.Error &&
        (booksUiState as UiState.Error).message.contains("internet", ignoreCase = true)

    val contentState = BooksContentState(
        booksUiState = booksUiState,
        isRefreshing = isRefreshing,
        isOffline = isOffline,
        snackbarHostState = snackbarHostState,
    )

    BooksScreenContent(
        modifier = modifier,
        state = contentState,
        callbacks = callbacks,
        onRefresh = { viewModel.refresh(username) },
        onRetry = { viewModel.refresh(username) },
    )
}

/**
 * Handles error messages display in snackbar.
 */
@Composable
private fun HandleErrorMessages(
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onClearError: () -> Unit,
) {
    val currentOnClearError by rememberUpdatedState(onClearError)
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            currentOnClearError()
        }
    }
}

/**
 * Main content of the books screen with scaffold and state handling.
 */
@Composable
private fun BooksScreenContent(
    state: BooksContentState,
    callbacks: BooksScreenCallbacks,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                BooksTopAppBar(
                    onFilterClick = callbacks.onFilterClick,
                    onSortClick = callbacks.onSortClick,
                )
                OfflineIndicator(isOffline = state.isOffline)
            }
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            BooksStateContent(
                uiState = state.booksUiState,
                isRefreshing = state.isRefreshing,
                onBookClick = callbacks.onBookClick,
                onRetry = onRetry,
            )
        }
    }
}

/**
 * Renders the appropriate UI based on the current UiState.
 */
@Composable
private fun BooksStateContent(
    uiState: UiState<List<Book>>,
    isRefreshing: Boolean,
    onBookClick: (Book) -> Unit,
    onRetry: () -> Unit,
) {
    when (val state = uiState) {
        is UiState.Idle -> {
            // Initial state - show empty state (shouldn't normally see this as fetch is triggered immediately)
            EmptyState(
                message = "Loading your books from Open Library...",
                emptyStateType = EmptyStateType.NO_BOOKS_IN_LIBRARY,
            )
        }
        is UiState.Loading -> {
            BooksLoadingState()
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(
                    message = "Your library is empty. Pull to refresh and sync your books from Open Library.",
                    emptyStateType = EmptyStateType.NO_BOOKS_IN_LIBRARY,
                )
            } else {
                BooksGrid(
                    books = state.data,
                    isRefreshing = isRefreshing,
                    onBookClick = onBookClick,
                )
            }
        }
        is UiState.Error -> {
            ErrorState(
                message = state.message,
                errorType = when {
                    state.message.contains("internet", ignoreCase = true) -> ErrorType.NETWORK
                    state.message.contains("server", ignoreCase = true) -> ErrorType.SERVER
                    else -> ErrorType.GENERIC
                },
                onRetry = onRetry,
            )
        }
        is UiState.Empty -> {
            EmptyState(
                message = "Your library is empty. Pull to refresh and sync your books from Open Library.",
                emptyStateType = EmptyStateType.NO_BOOKS_IN_LIBRARY,
            )
        }
    }
}

/**
 * Top app bar for the books screen.
 *
 * Displays the app title with filter and sort action buttons.
 */
@Composable
private fun BooksTopAppBar(onFilterClick: () -> Unit, onSortClick: () -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "Books",
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        actions = {
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter books",
                )
            }
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort books",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

/**
 * Grid of books with adaptive column sizing.
 *
 * Features:
 * - Automatically calculates columns based on screen width
 * - Smooth animations when items are added/removed
 * - Fade-in animation for books
 * - Fade-out animation during refresh
 */
@Composable
private fun BooksGrid(
    books: List<Book>,
    isRefreshing: Boolean,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animate the visibility of the grid during refresh
    AnimatedVisibility(
        visible = !isRefreshing,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200)),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            items(
                items = books,
                key = { book -> book.id },
            ) { book ->
                BookCard(
                    book = book,
                    onClick = { onBookClick(book) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 200),
                        placementSpec = tween(durationMillis = 300),
                    ),
                )
            }
        }
    }

    // Show shimmer skeletons during refresh
    AnimatedVisibility(
        visible = isRefreshing,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
        BooksLoadingState()
    }
}

/**
 * Loading state with skeleton book cards.
 *
 * Displays a grid of skeleton cards to indicate loading.
 */
@Composable
private fun BooksLoadingState(modifier: Modifier = Modifier) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        items(12) { index ->
            BookCardSkeleton(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// Previews

@Preview(name = "Books Screen - Loading", showBackground = true)
@Composable
private fun BooksScreenLoadingPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        BooksLoadingState()
    }
}

@Preview(name = "Books Screen - Empty State", showBackground = true)
@Composable
private fun BooksScreenEmptyPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        EmptyState(
            message = "Your library is empty. Pull to refresh and sync your books from Open Library.",
            emptyStateType = EmptyStateType.NO_BOOKS_IN_LIBRARY,
        )
    }
}

@Preview(name = "Books Screen - Error State", showBackground = true)
@Composable
private fun BooksScreenErrorPreview() {
    OpenLibraryTheme {
        ErrorState(
            message = "No internet connection. Please check your network settings and try again.",
            errorType = ErrorType.NETWORK,
            onRetry = {},
        )
    }
}

@Preview(name = "Books Grid - With Books - Light", showBackground = true)
@Composable
private fun BooksGridPreviewLight() {
    val sampleBooks = listOf(
        Book(
            id = "1",
            title = "The Hobbit",
            authors = listOf("J.R.R. Tolkien"),
            coverUrl = null,
            readingStatus = ReadingStatus.WantToRead,
            isFavorite = false,
        ),
        Book(
            id = "2",
            title = "The Lord of the Rings",
            authors = listOf("J.R.R. Tolkien"),
            coverUrl = null,
            readingStatus = ReadingStatus.CurrentlyReading,
            isFavorite = true,
        ),
        Book(
            id = "3",
            title = "1984",
            authors = listOf("George Orwell"),
            coverUrl = null,
            readingStatus = ReadingStatus.AlreadyRead,
            isFavorite = false,
        ),
        Book(
            id = "4",
            title = "To Kill a Mockingbird",
            authors = listOf("Harper Lee"),
            coverUrl = null,
            readingStatus = ReadingStatus.WantToRead,
            isFavorite = true,
        ),
    )

    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        BooksGrid(
            books = sampleBooks,
            isRefreshing = false,
            onBookClick = {},
        )
    }
}

@Preview(name = "Books Grid - With Books - Dark", showBackground = true)
@Composable
private fun BooksGridPreviewDark() {
    val sampleBooks = listOf(
        Book(
            id = "1",
            title = "The Hobbit",
            authors = listOf("J.R.R. Tolkien"),
            coverUrl = null,
            readingStatus = ReadingStatus.WantToRead,
            isFavorite = false,
        ),
        Book(
            id = "2",
            title = "Harry Potter and the Philosopher's Stone",
            authors = listOf("J.K. Rowling"),
            coverUrl = null,
            readingStatus = ReadingStatus.CurrentlyReading,
            isFavorite = true,
        ),
    )

    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        BooksGrid(
            books = sampleBooks,
            isRefreshing = false,
            onBookClick = {},
        )
    }
}

@Preview(name = "Books Top App Bar", showBackground = true)
@Composable
private fun BooksTopAppBarPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        BooksTopAppBar(
            onFilterClick = {},
            onSortClick = {},
        )
    }
}
