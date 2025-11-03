@file:Suppress("MatchingDeclarationName", "TooManyFunctions") // File contains multiple screen-related declarations

package com.darach.openlibrarybooks.feature.favourites

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.common.util.NetworkConnectivity
import com.darach.openlibrarybooks.core.designsystem.component.BookCard
import com.darach.openlibrarybooks.core.designsystem.component.BookCardSkeleton
import com.darach.openlibrarybooks.core.designsystem.component.EmptyState
import com.darach.openlibrarybooks.core.designsystem.component.EmptyStateType
import com.darach.openlibrarybooks.core.designsystem.component.ErrorState
import com.darach.openlibrarybooks.core.designsystem.component.ErrorType
import com.darach.openlibrarybooks.core.designsystem.component.OfflineIndicator
import com.darach.openlibrarybooks.core.designsystem.component.TriangularPattern
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.designsystem.theme.goldOchre
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import com.darach.openlibrarybooks.feature.books.BookDetailsBottomSheet
import com.darach.openlibrarybooks.feature.books.BookDetailsViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy

/**
 * Entry point for accessing FavouritesRepository and NetworkConnectivity in FavouritesScreen.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FavouritesScreenEntryPoint {
    fun favouritesRepository(): FavouritesRepository

    fun networkConnectivity(): NetworkConnectivity
}

/**
 * Favourites screen - displays user's favourite books in a list layout.
 * Shows large horizontal cards with cover image, title, authors, and description.
 *
 * @param modifier Optional modifier for the screen
 * @param viewModel The ViewModel managing favourites state
 * @param bookDetailsViewModel The ViewModel managing book details bottom sheet
 */
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavouritesViewModel = hiltViewModel(),
    bookDetailsViewModel: BookDetailsViewModel = hiltViewModel(),
) {
    // Get dependencies using Hilt EntryPoint
    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            FavouritesScreenEntryPoint::class.java,
        )
    }
    val favouritesRepository = remember { entryPoint.favouritesRepository() }
    val networkConnectivity = remember { entryPoint.networkConnectivity() }

    // Remember CompositeDisposable for RxJava subscriptions
    val compositeDisposable = remember { CompositeDisposable() }

    val favouritesUiState by viewModel.favouritesUiState.collectAsStateWithLifecycle()

    // Observe network connectivity
    val isOnline by networkConnectivity.observeConnectivity()
        .collectAsStateWithLifecycle(initialValue = true)

    // Bottom sheet state for book details
    var showBookDetails by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Offline indicator at the top
            OfflineIndicator(
                isOffline = !isOnline,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            )

            // Main content
            FavouritesScreenContent(
                favouritesUiState = favouritesUiState,
                onBookClick = { book ->
                    // Load book details and show bottom sheet
                    bookDetailsViewModel.loadBookDetails(
                        workId = book.workKey ?: "",
                        editionId = book.editionKey,
                        bookId = book.id,
                    )
                    showBookDetails = true
                },
                onFavoriteToggle = { bookId ->
                    // Toggle favourite using repository
                    favouritesRepository.toggleFavourite(bookId)
                        .subscribeBy(
                            onComplete = {
                                // Success - no action needed as UI will update automatically
                            },
                            onError = { error ->
                                // TODO: Show error to user
                            },
                        )
                        .addTo(compositeDisposable)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Book details bottom sheet
        if (showBookDetails) {
            @Suppress("ViewModelForwarding") // ViewModel is scoped to this screen, safe to forward
            BookDetailsBottomSheet(
                viewModel = bookDetailsViewModel,
                sheetState = sheetState,
                onDismiss = { showBookDetails = false },
            )
        }
    }
}

/**
 * Stateless content composable for the favourites screen.
 * Handles different UI states (loading, success, empty, error).
 *
 * @param favouritesUiState Current UI state with favourites data
 * @param onBookClick Callback when a book is clicked
 * @param onFavoriteToggle Callback when the favorite button is toggled
 * @param modifier Optional modifier for the screen
 */
@Composable
internal fun FavouritesScreenContent(
    favouritesUiState: UiState<List<Book>>,
    onBookClick: (Book) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (favouritesUiState) {
        is UiState.Loading -> {
            FavouritesLoadingState(modifier = modifier)
        }
        is UiState.Success -> {
            Column(modifier = modifier.fillMaxSize()) {
                // Header with favourite count
                FavouritesHeader(favouriteCount = favouritesUiState.data.size)

                // List of favourite books
                FavouritesList(
                    books = favouritesUiState.data,
                    onBookClick = onBookClick,
                    onFavoriteToggle = onFavoriteToggle,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        is UiState.Empty -> {
            EmptyState(
                message = stringResource(R.string.no_favourites),
                emptyStateType = EmptyStateType.NO_BOOKS_FOUND,
                modifier = modifier.fillMaxSize(),
            )
        }
        is UiState.Error -> {
            ErrorState(
                message = favouritesUiState.message,
                errorType = ErrorType.GENERIC,
                onRetry = {}, // No retry action for favourites
                modifier = modifier.fillMaxSize(),
            )
        }
        else -> {
            // Idle state - shouldn't happen but handle gracefully
            EmptyState(
                message = stringResource(R.string.loading_favourites),
                emptyStateType = EmptyStateType.NO_BOOKS_FOUND,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Header for the favourites screen with triangular pattern background.
 * Displays the screen title and favourite count in a visually distinct banner.
 *
 * @param favouriteCount Number of favourite books to display
 * @param modifier Optional modifier for the header
 */
@Suppress("LongMethod") // UI composition function with necessary structure
@Composable
private fun FavouritesHeader(favouriteCount: Int, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()

    // Create custom gold-tone colors for the triangular pattern
    val goldColors = listOf(
        goldOchre.copy(alpha = if (isDarkTheme) 0.3f else 0.6f),
        goldOchre.copy(alpha = if (isDarkTheme) 0.2f else 0.4f),
        goldOchre.copy(alpha = if (isDarkTheme) 0.15f else 0.3f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        // Triangular pattern background with gold tones
        TriangularPattern(
            modifier = Modifier.matchParentSize(),
            triangleSize = 80f,
            customColors = goldColors,
            overlayColor = Color.Transparent,
        )

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // Title with display font
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.my_favourites),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Favourite count badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = goldOchre.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp),
                        )
                        Text(
                            text = "$favouriteCount ${if (favouriteCount == 1) "book" else "books"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Loading state for favourites screen.
 * Shows skeleton cards in adaptive grid layout matching the main view.
 */
@Composable
private fun FavouritesLoadingState(modifier: Modifier = Modifier) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        items(24) { index ->
            BookCardSkeleton(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Grid of favourite books with adaptive column sizing.
 *
 * Features:
 * - Automatically calculates columns based on screen width (160dp minimum)
 * - Smooth animations when items are added/removed
 * - Enhanced spacing and visual flourishes for favourites
 * - Uses favourite variant of BookCard with gold border and elevated styling
 *
 * @param books List of favourite books
 * @param onBookClick Callback when a book is clicked
 * @param onFavoriteToggle Callback when the favorite button is toggled
 * @param modifier Optional modifier
 */
@Composable
private fun FavouritesList(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
    ) {
        items(
            items = books,
            key = { book -> book.id },
        ) { book ->
            BookCard(
                book = book,
                onClick = { onBookClick(book) },
                onFavoriteToggle = { onFavoriteToggle(book.id) },
                isFavouriteVariant = true,
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(durationMillis = 300),
                    fadeOutSpec = tween(durationMillis = 200),
                    placementSpec = tween(durationMillis = 300),
                ),
            )
        }
    }
}

// Previews

@Preview(name = "Favourites Screen - Empty - Light")
@Composable
private fun FavouritesScreenEmptyPreview() {
    OpenLibraryTheme(darkTheme = false) {
        FavouritesScreenContent(
            favouritesUiState = UiState.Empty,
            onBookClick = {},
            onFavoriteToggle = {},
        )
    }
}

@Preview(name = "Favourites Screen - With Favourites - Light")
@Composable
private fun FavouritesScreenWithFavouritesPreview() {
    val sampleBooks = listOf(
        Book(
            id = "1",
            title = "The Hobbit",
            authors = listOf("J.R.R. Tolkien"),
            coverUrl = null,
            readingStatus = ReadingStatus.WantToRead,
            isFavorite = true,
        ),
        Book(
            id = "2",
            title = "The Lord of the Rings: The Fellowship of the Ring",
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
            isFavorite = true,
        ),
    )

    OpenLibraryTheme(darkTheme = false) {
        FavouritesScreenContent(
            favouritesUiState = UiState.Success(sampleBooks),
            onBookClick = {},
            onFavoriteToggle = {},
        )
    }
}

@Preview(name = "Favourites Screen - With Favourites - Dark")
@Composable
private fun FavouritesScreenWithFavouritesDarkPreview() {
    val sampleBooks = listOf(
        Book(
            id = "1",
            title = "The Hobbit",
            authors = listOf("J.R.R. Tolkien"),
            coverUrl = null,
            readingStatus = ReadingStatus.WantToRead,
            isFavorite = true,
        ),
        Book(
            id = "2",
            title = "To Kill a Mockingbird",
            authors = listOf("Harper Lee"),
            coverUrl = null,
            readingStatus = ReadingStatus.AlreadyRead,
            isFavorite = true,
        ),
    )

    OpenLibraryTheme(darkTheme = true) {
        FavouritesScreenContent(
            favouritesUiState = UiState.Success(sampleBooks),
            onBookClick = {},
            onFavoriteToggle = {},
        )
    }
}

@Preview(name = "Favourite Book Card - Light")
@Composable
private fun FavoriteBookCardPreview() {
    OpenLibraryTheme(darkTheme = false) {
        BookCard(
            book = Book(
                id = "1",
                title = "The Lord of the Rings: The Fellowship of the Ring",
                authors = listOf("J.R.R. Tolkien"),
                coverUrl = null,
                readingStatus = ReadingStatus.CurrentlyReading,
                isFavorite = true,
            ),
            onClick = {},
            onFavoriteToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Favourites Screen - Loading")
@Composable
private fun FavouritesScreenLoadingPreview() {
    OpenLibraryTheme(darkTheme = false) {
        FavouritesLoadingState()
    }
}
