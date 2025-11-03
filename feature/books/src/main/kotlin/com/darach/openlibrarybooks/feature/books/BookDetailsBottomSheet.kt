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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                    onClose = onDismiss,
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
                    Text("No details available", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/**
 * Loading state for book details.
 * Displays shimmering placeholders while data is being fetched.
 */
@Composable
private fun BookDetailsLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LoadingHeaderPlaceholders()
        LoadingContentPlaceholders()
    }
}

/**
 * Loading placeholders for header section (cover, title, authors, metadata).
 * Two-column layout with cover on left, content on right.
 */
@Composable
private fun LoadingHeaderPlaceholders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cover image placeholder - left side, smaller
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
            // Title placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(28.dp)
                    .clip(MaterialTheme.shapes.small),
            )

            // Authors placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp)
                    .clip(MaterialTheme.shapes.small),
            )

            // Spacer to push metadata to bottom
            Box(modifier = Modifier.weight(1f))

            // Metadata chips placeholder
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(2) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(100.dp)
                            .height(28.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            }
        }
    }
}

/**
 * Loading placeholders for content section (subjects and description).
 */
@Composable
private fun LoadingContentPlaceholders() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Subjects section placeholder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(70.dp)
                            .height(32.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            }
        }

        // Description section placeholder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small),
                )
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
                contentDescription = "Close",
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
 *
 * @param workDetails Work details to display
 */
@Composable
private fun BookHeaderSection(workDetails: WorkDetails) {
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

        // Right side: Title, authors, and metadata
        Column(
            modifier = Modifier
                .weight(1f)
                .height(180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Title
            Text(
                text = workDetails.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

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
}

/**
 * Main content for book details.
 * Displays cover image, title, authors, metadata, and description.
 *
 * @param workDetails Work details to display
 * @param isFavourite Whether the book is marked as favourite
 * @param onFavouriteToggle Callback to toggle favourite status
 * @param onClose Callback to close the bottom sheet
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailsContent(
    workDetails: WorkDetails,
    isFavourite: Boolean,
    onFavouriteToggle: () -> Unit,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Action buttons at top right with reduced padding
            ActionButtons(
                isFavourite = isFavourite,
                onFavouriteToggle = onFavouriteToggle,
                onClose = onClose,
                modifier = Modifier
                    .padding(8.dp),
            )
            // Two-column header: cover on left, title/authors/metadata on right
            BookHeaderSection(workDetails = workDetails)

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
 * Action buttons for favourite toggle and close.
 * Positioned at the top right with reduced padding (8.dp).
 * Favourite button includes scale animation on toggle.
 *
 * @param isFavourite Whether the book is favourited
 * @param onFavouriteToggle Callback to toggle favourite
 * @param onClose Callback to close the sheet
 * @param modifier Optional modifier
 */
@Composable
private fun ActionButtons(
    isFavourite: Boolean,
    onFavouriteToggle: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Close button
        FilledIconButton(
            onClick = onClose,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
            )
        }

        // Favourite button with scale animation - using extracted component
        FavoriteFilledIconButton(
            isFavorite = isFavourite,
            onToggle = onFavouriteToggle,
        )
    }
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
                        text = "Published: $date",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            if (editionCount > 0) {
                val editionLabel = "$editionCount edition${if (editionCount != 1) "s" else ""}"
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
                text = "Subjects",
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
            contentDescription = "Cover for $title",
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
            text = "About",
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
                    text = if (expanded) "Read less" else "Read more",
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
private fun BookDetailsContentPreview() {
    OpenLibraryTheme {
        BookDetailsContent(
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
            onClose = {},
        )
    }
}
