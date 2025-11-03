@file:Suppress("MatchingDeclarationName", "TooManyFunctions")
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.darach.openlibrarybooks.feature.books

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.darach.openlibrarybooks.core.designsystem.theme.goldOchre
import com.darach.openlibrarybooks.core.designsystem.theme.primaryLight
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.FilterOptions
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.SortOption
import kotlinx.coroutines.launch

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
    bookDetailsViewModel: BookDetailsViewModel = hiltViewModel(),
    username: String = "mekBot",
    callbacks: BooksScreenCallbacks = BooksScreenCallbacks(),
) {
    val booksUiState by viewModel.booksUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val filterOptions by viewModel.filterOptions.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val availableAuthors by viewModel.availableAuthors.collectAsStateWithLifecycle()
    val availableSubjects by viewModel.availableSubjects.collectAsStateWithLifecycle()
    val yearRange by viewModel.yearRange.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // State for showing/hiding bottom sheets
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showBookDetails by remember { mutableStateOf(false) }

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
        callbacks = callbacks.copy(
            onBookClick = { book ->
                // Show book details in modal bottom sheet instead of navigating
                book.workKey?.let { workKey ->
                    val workId = workKey.removePrefix("/works/")
                    val editionId = book.editionKey?.removePrefix("/books/")
                    bookDetailsViewModel.loadBookDetails(workId, editionId)
                    showBookDetails = true
                }
            },
            onFilterClick = { showFilterSheet = true },
            onSortClick = { showSortSheet = true },
        ),
        onRefresh = { viewModel.refresh(username) },
        onRetry = { viewModel.refresh(username) },
        filterOptions = filterOptions,
        onReadingStatusChange = { statuses ->
            viewModel.updateFilters(filterOptions.copy(readingStatuses = statuses))
        },
    )

    // Show filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilters = filterOptions,
            availableAuthors = availableAuthors,
            availableSubjects = availableSubjects,
            yearRange = yearRange,
            onApplyFilters = { filters ->
                viewModel.updateFilters(filters)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }

    // Show sort bottom sheet
    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortSelect = { sort ->
                viewModel.updateSort(sort)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false },
        )
    }

    // Show book details bottom sheet
    if (showBookDetails) {
        BookDetailsBottomSheet(
            viewModel = bookDetailsViewModel,
            onDismiss = { showBookDetails = false },
        )
    }
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
@Suppress("LongParameterList")
@Composable
private fun BooksScreenContent(
    state: BooksContentState,
    callbacks: BooksScreenCallbacks,
    filterOptions: FilterOptions,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onReadingStatusChange: (Set<ReadingStatus>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                BooksTopAppBar(
                    onFilterClick = callbacks.onFilterClick,
                    onSortClick = callbacks.onSortClick,
                    filterOptions = filterOptions,
                    onReadingStatusChange = onReadingStatusChange,
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
    when (uiState) {
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
            if (uiState.data.isEmpty()) {
                EmptyState(
                    message = "Your library is empty. Pull to refresh and sync your books from Open Library.",
                    emptyStateType = EmptyStateType.NO_BOOKS_IN_LIBRARY,
                )
            } else {
                BooksGrid(
                    books = uiState.data,
                    isRefreshing = isRefreshing,
                    onBookClick = onBookClick,
                )
            }
        }
        is UiState.Error -> {
            ErrorState(
                message = uiState.message,
                errorType = when {
                    uiState.message.contains("internet", ignoreCase = true) -> ErrorType.NETWORK
                    uiState.message.contains("server", ignoreCase = true) -> ErrorType.SERVER
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
 * Shows a badge on the filter button when filters are active.
 * Includes a multi-select segmented button for reading status filtering.
 */
@ExperimentalMaterial3Api
@Composable
private fun BooksTopAppBar(
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    filterOptions: FilterOptions,
    onReadingStatusChange: (Set<ReadingStatus>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Calculate active filter count (excluding reading status which is now in the top bar)
    val activeFilterCount = buildList {
        if (filterOptions.yearFrom != null || filterOptions.yearTo != null) add(1)
        if (filterOptions.authors.isNotEmpty()) add(1)
        if (filterOptions.subjects.isNotEmpty()) add(1)
    }.size

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = "Books",
                    style = MaterialTheme.typography.headlineMedium,
                )
            },
            actions = {
                // Filter button with badge
                IconButton(onClick = onFilterClick) {
                    BadgedBox(
                        badge = {
                            if (activeFilterCount > 0) {
                                Badge {
                                    Text(text = activeFilterCount.toString())
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter books",
                        )
                    }
                }

                // Sort button
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

        // Reading Status Single-Select Connected Button Group
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            val options = listOf(
                "Want to Read" to ReadingStatus.WantToRead,
                "Reading" to ReadingStatus.CurrentlyReading,
                "Finished" to ReadingStatus.AlreadyRead,
            )

            // Determine which index is selected based on current filter
            val selectedIndex = when {
                ReadingStatus.WantToRead in filterOptions.readingStatuses -> 0
                ReadingStatus.CurrentlyReading in filterOptions.readingStatuses -> 1
                ReadingStatus.AlreadyRead in filterOptions.readingStatuses -> 2
                else -> 0 // Default to "Want to Read" if none selected
            }

            // Only use goldOchre for light theme with static colours (not dynamic)
            val isDarkTheme = isSystemInDarkTheme()
            val isUsingStaticColors = MaterialTheme.colorScheme.primary == primaryLight
            val shouldUseGoldOchre = !isDarkTheme && isUsingStaticColors

            options.forEachIndexed { index, (label, status) ->
                ToggleButton(
                    checked = index == selectedIndex,
                    onCheckedChange = {
                        val newStatuses = setOf(status)
                        onReadingStatusChange(newStatuses)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { role = Role.RadioButton },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = if (shouldUseGoldOchre) {
                        ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = goldOchre,
                            checkedContentColor = Color.Black,
                        )
                    } else {
                        ToggleButtonDefaults.toggleButtonColors()
                    },
                ) {
                    Text(
                        text = label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
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
        items(24) { index ->
            BookCardSkeleton(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Publication year range filter section using a range slider.
 */
@Composable
private fun YearRangeFilterSection(
    yearRange: Pair<Int, Int>,
    selectedYearRange: Pair<Int, Int>,
    onYearRangeChange: (Pair<Int, Int>) -> Unit,
) {
    Column {
        Text(
            text = "Publication Year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "${selectedYearRange.first} - ${selectedYearRange.second}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        RangeSlider(
            value = selectedYearRange.first.toFloat()..selectedYearRange.second.toFloat(),
            onValueChange = { range ->
                onYearRangeChange(Pair(range.start.toInt(), range.endInclusive.toInt()))
            },
            valueRange = yearRange.first.toFloat()..yearRange.second.toFloat(),
            steps = 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
        )
    }
}

/**
 * Filter bottom sheet for filtering books.
 *
 * Features:
 * - Publication year range with range slider
 * - Author filter with chips for available authors
 * - Subject filter with chips for available subjects
 * - Apply and Clear All buttons
 *
 * @param currentFilters Current filter options
 * @param availableAuthors List of all authors from cached books
 * @param availableSubjects List of all subjects from cached books
 * @param yearRange Min and max years available in the book collection
 * @param onApplyFilters Callback when filters are applied
 * @param onDismiss Callback when sheet is dismissed
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun FilterBottomSheet(
    currentFilters: FilterOptions,
    availableAuthors: List<String>,
    availableSubjects: List<String>,
    yearRange: Pair<Int, Int>,
    onApplyFilters: (FilterOptions) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Local state for editing filters before applying
    var selectedYearRange by remember {
        mutableStateOf(
            Pair(
                currentFilters.yearFrom ?: yearRange.first,
                currentFilters.yearTo ?: yearRange.second,
            ),
        )
    }
    var selectedAuthors by remember { mutableStateOf(currentFilters.authors.toSet()) }
    var selectedSubjects by remember { mutableStateOf(currentFilters.subjects.toSet()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            // Title
            Text(
                text = "Filter Books",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            // Publication Year Section
            YearRangeFilterSection(
                yearRange = yearRange,
                selectedYearRange = selectedYearRange,
                onYearRangeChange = { selectedYearRange = it },
            )

            HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

            // Author Section - only show if authors are available
            if (availableAuthors.isNotEmpty()) {
                Text(
                    text = "Authors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableAuthors.take(20).forEach { author ->
                        FilterChip(
                            selected = author in selectedAuthors,
                            onClick = {
                                selectedAuthors = if (author in selectedAuthors) {
                                    selectedAuthors - author
                                } else {
                                    selectedAuthors + author
                                }
                            },
                            label = { Text(author) },
                            leadingIcon = if (author in selectedAuthors) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))
            }

            // Subject Section - only show if subjects are available
            if (availableSubjects.isNotEmpty()) {
                Text(
                    text = "Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableSubjects.take(20).forEach { subject ->
                        FilterChip(
                            selected = subject in selectedSubjects,
                            onClick = {
                                selectedSubjects = if (subject in selectedSubjects) {
                                    selectedSubjects - subject
                                } else {
                                    selectedSubjects + subject
                                }
                            },
                            label = { Text(subject) },
                            leadingIcon = if (subject in selectedSubjects) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        selectedYearRange = yearRange
                        selectedAuthors = emptySet()
                        selectedSubjects = emptySet()
                        onApplyFilters(
                            currentFilters.copy(
                                yearFrom = null,
                                yearTo = null,
                                authors = emptyList(),
                                subjects = emptyList(),
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text("Clear All")
                }

                Button(
                    onClick = {
                        val filters = currentFilters.copy(
                            yearFrom = if (selectedYearRange.first ==
                                yearRange.first
                            ) {
                                null
                            } else {
                                selectedYearRange.first
                            },
                            yearTo = if (selectedYearRange.second ==
                                yearRange.second
                            ) {
                                null
                            } else {
                                selectedYearRange.second
                            },
                            authors = selectedAuthors.toList(),
                            subjects = selectedSubjects.toList(),
                        )
                        onApplyFilters(filters)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text("Apply")
                }
            }
        }
    }
}

/**
 * Sort bottom sheet for sorting books.
 *
 * Features:
 * - Radio button selection for all sort options
 * - Immediate application of sort selection
 * - Checkmark indicator on selected option
 * - Grouped by category (Title, Author, Date, Year)
 *
 * @param currentSort Current sort option
 * @param onSortSelect Callback when sort is selected
 * @param onDismiss Callback when sheet is dismissed
 */
@Composable
fun SortBottomSheet(
    currentSort: SortOption,
    onSortSelect: (SortOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // All available sort options
    val sortOptions = listOf(
        SortOption.TitleAscending,
        SortOption.TitleDescending,
        SortOption.AuthorAscending,
        SortOption.AuthorDescending,
        SortOption.DateAddedNewest,
        SortOption.DateAddedOldest,
        SortOption.PublishYearNewest,
        SortOption.PublishYearOldest,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            // Title
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            // Sort options
            sortOptions.forEach { sortOption ->
                SortOptionRow(
                    sortOption = sortOption,
                    isSelected = currentSort == sortOption,
                    onClick = {
                        onSortSelect(sortOption)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                )
            }
        }
    }
}

/**
 * Single sort option row with radio button and checkmark.
 *
 * @param sortOption The sort option to display
 * @param isSelected Whether this option is currently selected
 * @param onClick Callback when the row is clicked
 */
@Composable
private fun SortOptionRow(
    sortOption: SortOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected,
                onValueChange = { onClick() },
                role = Role.RadioButton,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null, // Handled by toggleable
        )

        Text(
            text = sortOption.displayName(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
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
            filterOptions = FilterOptions(),
            onReadingStatusChange = {},
        )
    }
}

@Preview(name = "Books Top App Bar - With Active Filters", showBackground = true)
@Composable
private fun BooksTopAppBarWithFiltersPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        BooksTopAppBar(
            onFilterClick = {},
            onSortClick = {},
            filterOptions = FilterOptions(
                readingStatuses = setOf(ReadingStatus.CurrentlyReading),
                yearFrom = 1900,
                yearTo = 2000,
            ),
            onReadingStatusChange = {},
        )
    }
}

@Preview(name = "Filter Bottom Sheet", showBackground = true)
@Composable
private fun FilterBottomSheetPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        FilterBottomSheet(
            currentFilters = FilterOptions(),
            availableAuthors = listOf("J.R.R. Tolkien", "George Orwell", "Harper Lee"),
            availableSubjects = listOf("Fantasy", "Science Fiction", "Classic Literature"),
            yearRange = Pair(1900, 2024),
            onApplyFilters = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Filter Bottom Sheet - With Filters", showBackground = true)
@Composable
private fun FilterBottomSheetWithFiltersPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        FilterBottomSheet(
            currentFilters = FilterOptions(
                readingStatuses = setOf(ReadingStatus.CurrentlyReading),
                yearFrom = 1900,
                yearTo = 2000,
                authors = listOf("J.R.R. Tolkien"),
                subjects = listOf("Fantasy"),
            ),
            availableAuthors = listOf("J.R.R. Tolkien", "George Orwell", "Harper Lee"),
            availableSubjects = listOf("Fantasy", "Science Fiction", "Classic Literature"),
            yearRange = Pair(1900, 2024),
            onApplyFilters = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Sort Bottom Sheet", showBackground = true)
@Composable
private fun SortBottomSheetPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        SortBottomSheet(
            currentSort = SortOption.DateAddedNewest,
            onSortSelect = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Sort Bottom Sheet - Title Sort", showBackground = true)
@Composable
private fun SortBottomSheetTitlePreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        SortBottomSheet(
            currentSort = SortOption.TitleAscending,
            onSortSelect = {},
            onDismiss = {},
        )
    }
}
