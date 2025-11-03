package com.darach.openlibrarybooks.feature.books

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.designsystem.component.ErrorState
import com.darach.openlibrarybooks.core.designsystem.component.ErrorType
import com.darach.openlibrarybooks.core.designsystem.component.FavoriteFilledIconButton
import com.darach.openlibrarybooks.core.designsystem.component.ShimmerBox
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.domain.model.WorkDetails

/**
 * Bottom sheet displaying detailed information about a book.
 *
 * Fetches work details from the API and displays cover, title, authors,
 * metadata, and description with expandable read more functionality.
 *
 * @param onDismiss Callback invoked when the bottom sheet is dismissed
 * @param modifier Optional modifier for the bottom sheet
 * @param viewModel ViewModel managing book details state
 * @param sheetState State of the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookDetailsViewModel = hiltViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val workDetailsState by viewModel.workDetailsState.collectAsStateWithLifecycle()
    val isFavourite by viewModel.isFavourite.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        when (workDetailsState) {
            is UiState.Loading -> {
                BookDetailsLoadingState()
            }
            is UiState.Success -> {
                val workDetails = (workDetailsState as UiState.Success<WorkDetails>).data
                BookDetailsContent(
                    workDetails = workDetails,
                    isFavourite = isFavourite,
                    onFavouriteToggle = { viewModel.toggleFavourite() },
                )
            }
            is UiState.Error -> {
                val error = workDetailsState as UiState.Error
                BookDetailsErrorState(
                    message = error.message,
                    onRetry = { viewModel.retry() },
                    onClose = onDismiss,
                )
            }
            else -> {
                // Idle or Empty - shouldn't happen but handle gracefully
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.error_occurred), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/**
 * Loading state for book details.
 * Displays shimmering placeholders while data is being fetched.
 */
@Suppress("LongMethod")
@Composable
private fun BookDetailsLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header placeholders: cover, title, authors, metadata
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Cover image placeholder
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium),
            )

            // Right side content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(28.dp).clip(MaterialTheme.shapes.small))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp).clip(MaterialTheme.shapes.small))
                Box(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(2) {
                        ShimmerBox(modifier = Modifier.width(100.dp).height(28.dp).clip(MaterialTheme.shapes.small))
                    }
                }
            }
        }

        // Content placeholders: subjects and description
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Subjects
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(modifier = Modifier.width(80.dp).height(20.dp).clip(MaterialTheme.shapes.small))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        ShimmerBox(modifier = Modifier.width(70.dp).height(32.dp).clip(MaterialTheme.shapes.small))
                    }
                }
            }

            // Description
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(modifier = Modifier.width(60.dp).height(20.dp).clip(MaterialTheme.shapes.small))
                repeat(4) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(16.dp).clip(MaterialTheme.shapes.small))
                }
            }
        }
    }
}

/**
 * Error state for book details.
 * Displays an error message with retry and close actions.
 *
 * @param message Error message to display
 * @param onRetry Callback for retry action
 * @param onClose Callback to close the bottom sheet
 */
@Composable
private fun BookDetailsErrorState(message: String, onRetry: () -> Unit, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
    ) {
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.close),
            )
        }

        // Error state centered
        ErrorState(
            errorType = ErrorType.NETWORK,
            message = message,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        )
    }
}

/**
 * Header section with cover image, title, authors, and metadata in two-column layout.
 * Includes favourite button next to the title.
 *
 * @param workDetails Work details to display
 * @param isFavourite Whether the book is favourited
 * @param onFavouriteToggle Callback to toggle favourite
 */
@Composable
private fun BookHeaderSection(workDetails: WorkDetails, isFavourite: Boolean, onFavouriteToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cover image - smaller, on the left
        BookCoverSection(
            coverIds = workDetails.coverIds,
            title = workDetails.title,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp),
        )

        // Right side: Title with favourite button, authors, and metadata
        BookHeaderContent(
            workDetails = workDetails,
            isFavourite = isFavourite,
            onFavouriteToggle = onFavouriteToggle,
        )
    }
}

/**
 * Content section with title, authors, and metadata.
 */
@Composable
private fun RowScope.BookHeaderContent(workDetails: WorkDetails, isFavourite: Boolean, onFavouriteToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Title with favourite button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = workDetails.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            // Favourite button positioned next to title
            FavouriteActionButton(
                isFavourite = isFavourite,
                onFavouriteToggle = onFavouriteToggle,
            )
        }

        // Authors
        if (workDetails.authors.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = workDetails.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Spacer to push metadata to bottom
        Box(modifier = Modifier.weight(1f))

        // Metadata chips
        MetadataSection(
            firstPublishDate = workDetails.firstPublishDate,
            editionCount = workDetails.coverIds.size,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Main content for book details.
 * Displays cover image, title, authors, metadata, and description.
 *
 * @param workDetails Work details to display
 * @param isFavourite Whether the book is marked as favourite
 * @param onFavouriteToggle Callback to toggle favourite status
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailsContent(workDetails: WorkDetails, isFavourite: Boolean, onFavouriteToggle: () -> Unit) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Two-column header: cover on left, title/authors/metadata on right, favourite button next to title
            BookHeaderSection(
                workDetails = workDetails,
                isFavourite = isFavourite,
                onFavouriteToggle = onFavouriteToggle,
            )

            // Subjects
            SubjectsSection(subjects = workDetails.subjects)

            // Description with read more/less
            workDetails.description?.let { description ->
                ExpandableDescription(
                    description = description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
            }
        }
    }
}

/**
 * Action button for favourite toggle.
 * Positioned in the header next to the title.
 * Favourite button includes scale animation on toggle.
 *
 * @param isFavourite Whether the book is favourited
 * @param onFavouriteToggle Callback to toggle favourite
 * @param modifier Optional modifier
 */
@Composable
private fun FavouriteActionButton(isFavourite: Boolean, onFavouriteToggle: () -> Unit, modifier: Modifier = Modifier) {
    // Favourite button with scale animation - using extracted component
    FavoriteFilledIconButton(
        isFavorite = isFavourite,
        onToggle = onFavouriteToggle,
        modifier = modifier,
    )
}

/**
 * Metadata section displaying publication info.
 *
 * @param firstPublishDate First publication date
 * @param editionCount Number of editions
 * @param modifier Optional modifier for the container
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetadataSection(firstPublishDate: String?, editionCount: Int, modifier: Modifier = Modifier) {
    if (firstPublishDate != null || editionCount > 0) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            firstPublishDate?.let { date ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.published) + " $date",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            if (editionCount > 0) {
                val editionLabel = "$editionCount ${stringResource(
                    if (editionCount != 1) R.string.edition_plural else R.string.edition_singular,
                )}"
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = editionLabel,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

/**
 * Subjects section with chips.
 *
 * @param subjects List of subject tags
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectsSection(subjects: List<String>) {
    if (subjects.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.subjects),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                subjects.take(10).forEach { subject ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = subject,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * Book cover section with optimised Coil loading.
 *
 * Uses SubcomposeAsyncImage for efficient loading with placeholders.
 * Checks Coil's memory cache first for instant loading if image was previously loaded.
 * Uses medium (M) cover size optimised for the smaller display area.
 *
 * @param coverIds List of cover IDs from the work
 * @param title Book title for fallback initials
 * @param modifier Modifier to control size and layout
 */
@Composable
private fun BookCoverSection(coverIds: List<Int>, title: String, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coverUrl = coverIds.firstOrNull()?.let { coverId ->
        // Use M (Medium) size for compact detail view - faster loading than L
        "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
    }

    // If we have a cover, show it with exact size
    if (coverUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(coverUrl)
                .memoryCacheKey(coverUrl) // Use URL as cache key
                .diskCacheKey(coverUrl) // Disk cache optimisation
                .placeholderMemoryCacheKey(coverUrl) // Check memory cache first for instant display
                .build(),
            contentDescription = stringResource(R.string.book_cover, title),
            modifier = modifier.clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
            loading = {
                // Loading placeholder - matches container size
                ShimmerBox(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(MaterialTheme.shapes.medium),
                )
            },
            error = {
                // Error fallback - show initials without gradient (compact view)
                BookCoverInitials(
                    title = title,
                    modifier = Modifier.matchParentSize(),
                    showGradient = false,
                )
            },
        )
    } else {
        // No cover ID available - show initials without gradient
        BookCoverInitials(
            title = title,
            modifier = modifier,
            showGradient = false,
        )
    }
}

/**
 * Fallback cover displaying book title initials.
 * Can display with or without gradient overlay.
 *
 * @param title Book title to extract initials from
 * @param modifier Optional modifier for the container
 * @param showGradient Whether to show gradient overlay (default true for large covers)
 */
@Composable
private fun BookCoverInitials(title: String, modifier: Modifier = Modifier, showGradient: Boolean = false) {
    val initials = title
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )

        // Gradient overlay for better text readability (only for large covers)
        if (showGradient) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            ),
                        ),
                    ),
            )
        }
    }
}

/**
 * Expandable description component with read more/less functionality.
 *
 * @param description The full description text
 * @param modifier Optional modifier
 * @param collapsedMaxLines Maximum lines to show when collapsed
 */
@Composable
private fun ExpandableDescription(description: String, modifier: Modifier = Modifier, collapsedMaxLines: Int = 4) {
    var expanded by remember { mutableStateOf(false) }
    var showReadMore by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                // Determine if text is truncated
                if (!expanded && textLayoutResult.hasVisualOverflow) {
                    showReadMore = true
                }
            },
        )

        // Read more/less button
        AnimatedVisibility(
            visible = showReadMore || expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = stringResource(if (expanded) R.string.read_less else R.string.read_more),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

// Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BookHeaderSectionPreview() {
    OpenLibraryTheme {
        BookHeaderSection(
            workDetails = WorkDetails(
                workKey = "/works/OL45804W",
                title = "The Lord of the Rings",
                description = "The Lord of the Rings is an epic high-fantasy novel by the English author and " +
                    "scholar J. R. R. Tolkien. Set in Middle-earth, the story began as a sequel to Tolkien's " +
                    "1937 children's book The Hobbit, but eventually developed into a much larger work.",
                subjects = listOf("Fantasy", "Adventure", "Classic Literature", "Epic", "Magic"),
                authors = listOf("J. R. R. Tolkien"),
                coverIds = listOf(12345),
                firstPublishDate = "1954",
            ),
            isFavourite = false,
            onFavouriteToggle = {},
        )
    }
}
