@file:Suppress("MatchingDeclarationName", "TooManyFunctions") // File contains multiple screen-related declarations

package com.darach.openlibrarybooks.feature.favourites

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.designsystem.component.BookCover
import com.darach.openlibrarybooks.core.designsystem.component.EmptyState
import com.darach.openlibrarybooks.core.designsystem.component.EmptyStateType
import com.darach.openlibrarybooks.core.designsystem.component.ErrorState
import com.darach.openlibrarybooks.core.designsystem.component.ErrorType
import com.darach.openlibrarybooks.core.designsystem.component.FavoriteIconButton
import com.darach.openlibrarybooks.core.designsystem.component.ReadingStatusBadge
import com.darach.openlibrarybooks.core.designsystem.component.ShimmerBox
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
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
 * Entry point for accessing FavouritesRepository in FavouritesScreen.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FavouritesScreenEntryPoint {
    fun favouritesRepository(): FavouritesRepository
}

/**
 * Favourites screen - displays user's favourite books in a list layout.
 * Shows large horizontal cards with cover image, title, authors, and description.
 *
 * @param modifier Optional modifier for the screen
 * @param viewModel The ViewModel managing favourites state
 * @param bookDetailsViewModel The ViewModel managing book details bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavouritesViewModel = hiltViewModel(),
    bookDetailsViewModel: BookDetailsViewModel = hiltViewModel(),
) {
    // Get FavouritesRepository using Hilt EntryPoint
    val context = LocalContext.current
    val favouritesRepository = androidx.compose.runtime.remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            FavouritesScreenEntryPoint::class.java,
        ).favouritesRepository()
    }

    // Remember CompositeDisposable for RxJava subscriptions
    val compositeDisposable = androidx.compose.runtime.remember { CompositeDisposable() }

    val favouritesUiState by viewModel.favouritesUiState.collectAsStateWithLifecycle()

    // Bottom sheet state for book details
    var showBookDetails by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Box(modifier = modifier.fillMaxSize()) {
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
            FavouritesList(
                books = favouritesUiState.data,
                onBookClick = onBookClick,
                onFavoriteToggle = onFavoriteToggle,
                modifier = modifier,
            )
        }
        is UiState.Empty -> {
            EmptyState(
                message = "No favourites yet. Mark books as favourites from your library and they'll appear here.",
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
                message = "Loading favourites...",
                emptyStateType = EmptyStateType.NO_BOOKS_FOUND,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Loading state for favourites screen.
 * Shows skeleton cards in list layout.
 */
@Composable
private fun FavouritesLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(6) {
            FavoriteBookCardSkeleton()
        }
    }
}

/**
 * List of favourite books in large horizontal cards.
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = books,
            key = { book -> book.id },
        ) { book ->
            FavoriteBookCard(
                book = book,
                onClick = { onBookClick(book) },
                onFavoriteToggle = { onFavoriteToggle(book.id) },
            )
        }
    }
}

/**
 * Large horizontal card for displaying a favourite book.
 * Shows cover image on the left, title, authors, and reading status on the right.
 * Includes a heart icon button for unfavoriting.
 *
 * @param book The book to display
 * @param onClick Callback when the card is clicked
 * @param onFavoriteToggle Callback when the favorite button is clicked
 * @param modifier Optional modifier
 */
@Composable
private fun FavoriteBookCard(
    book: Book,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animate scale when favorite status changes
    val scale by animateFloatAsState(
        targetValue = if (book.isFavorite) 1.0f else 0.9f,
        animationSpec = tween(durationMillis = 200),
        label = "favorite_scale",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .scale(scale),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            BookCover(
                coverUrl = book.coverUrl,
                title = book.title,
                width = 100.dp,
                height = 160.dp,
            )

            Spacer(modifier = Modifier.width(16.dp))

            BookDetailsSection(
                book = book,
                onFavoriteToggle = onFavoriteToggle,
            )
        }
    }
}

/**
 * Book details section for favorite card.
 * Shows title, authors, reading status, and favorite button.
 */
@Composable
private fun RowScope.BookDetailsSection(book: Book, onFavoriteToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(160.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BookTitleAndAuthors(
            title = book.title,
            authors = book.authors,
        )

        BookStatusBar(
            readingStatus = book.readingStatus,
            isFavorite = book.isFavorite,
            onFavoriteToggle = onFavoriteToggle,
        )
    }
}

/**
 * Book title and authors section.
 */
@Composable
private fun BookTitleAndAuthors(title: String, authors: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        if (authors.isNotEmpty()) {
            Text(
                text = authors.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Book status bar with reading status badge and favorite button.
 */
@Composable
private fun BookStatusBar(readingStatus: ReadingStatus, isFavorite: Boolean, onFavoriteToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReadingStatusBadge(readingStatus)
        FavoriteIconButton(isFavorite, onFavoriteToggle)
    }
}

/**
 * Reading status badge.
 */
// ReadingStatusBadge is now extracted to core/designsystem

// FavoriteButton is now extracted to core/designsystem (FavoriteIconButton)

// BookCoverSection is now replaced with BookCover from core/designsystem

/**
 * Skeleton loading state for favorite book card.
 */
@Composable
private fun FavoriteBookCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Cover skeleton
            ShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium),
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Details skeleton
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Title skeleton
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp),
                )

                // Author skeleton
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp),
                )

                Spacer(modifier = Modifier.weight(1f))

                // Status skeleton
                ShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp),
                )
            }
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
        FavoriteBookCard(
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
