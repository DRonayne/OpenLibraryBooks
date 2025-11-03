package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus

/**
 * Card component for displaying a book with cover, title, authors, and favourite status.
 *
 * Includes:
 * - Book cover image with Coil loading and placeholder for missing covers
 * - Title and author text with ellipsize for long text
 * - Favourite icon overlay
 * - Loading state with shimmer effect
 * - Click callback for navigation
 *
 * @param book The book to display
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun BookCard(book: Book, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            BookCoverImage(
                coverUrl = book.coverUrl,
                title = book.title,
                isFavorite = book.isFavorite,
            )
            BookInfo(
                title = book.title,
                authors = book.authors,
            )
        }
    }
}

/**
 * Displays the book cover image with favourite icon overlay.
 * Uses AsyncImage for better performance in lists (not SubcomposeAsyncImage).
 */
@Composable
private fun BookCoverImage(coverUrl: String?, title: String, isFavorite: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (coverUrl.isNullOrEmpty()) {
            CoverPlaceholder()
        } else {
            LoadedCoverImage(coverUrl, title)
        }

        if (isFavorite) {
            FavoriteIconOverlay()
        }
    }
}

/**
 * Displays placeholder icon for missing book covers.
 */
@Composable
private fun CoverPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "No cover available",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Loads and displays a book cover with shimmer loading and error states.
 */
@Composable
private fun LoadedCoverImage(coverUrl: String, title: String) {
    val painter = rememberAsyncImagePainter(model = coverUrl)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.67f),
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = "Cover for $title",
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )

        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
            }
            is AsyncImagePainter.State.Error -> {
                CoverPlaceholder()
            }
            else -> {
                // Image loaded successfully
            }
        }
    }
}

/**
 * Favorite icon overlay shown on top-right of cover.
 */
@Composable
private fun BoxScope.FavoriteIconOverlay() {
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Favourite",
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(24.dp),
        tint = MaterialTheme.colorScheme.error,
    )
}

/**
 * Displays book title and authors.
 */
@Composable
private fun BookInfo(title: String, authors: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (authors.isNotEmpty()) {
            Text(
                text = authors.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Skeleton loading state for [BookCard].
 * Matches the exact dimensions and layout of a real book card.
 *
 * @param modifier Modifier to be applied to the skeleton
 */
@Composable
fun BookCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Cover skeleton
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f),
                shape = MaterialTheme.shapes.medium,
            )

            // Text skeleton
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Title skeleton (2 lines)
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp),
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Author skeleton (1 line)
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp),
                )
            }
        }
    }
}

@Preview(name = "Book Card - Light", showBackground = true)
@Composable
private fun BookCardPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookCard(
                book = Book(
                    id = "1",
                    title = "The Hobbit",
                    authors = listOf("J.R.R. Tolkien"),
                    coverUrl = null,
                    readingStatus = ReadingStatus.WantToRead,
                    isFavorite = false,
                ),
                onClick = {},
                modifier = Modifier.width(160.dp),
            )

            BookCard(
                book = Book(
                    id = "2",
                    title = "A Very Long Book Title That Should Be Truncated Properly",
                    authors = listOf("Author One", "Author Two", "Author Three"),
                    coverUrl = null,
                    readingStatus = ReadingStatus.CurrentlyReading,
                    isFavorite = true,
                ),
                onClick = {},
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Preview(name = "Book Card - Dark", showBackground = true)
@Composable
private fun BookCardPreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        BookCard(
            book = Book(
                id = "1",
                title = "The Lord of the Rings",
                authors = listOf("J.R.R. Tolkien"),
                coverUrl = null,
                readingStatus = ReadingStatus.AlreadyRead,
                isFavorite = true,
            ),
            onClick = {},
            modifier = Modifier
                .width(160.dp)
                .padding(16.dp),
        )
    }
}

@Preview(name = "Book Card Skeleton - Light", showBackground = true)
@Composable
private fun BookCardSkeletonPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BookCardSkeleton(modifier = Modifier.width(160.dp))
            BookCardSkeleton(modifier = Modifier.width(160.dp))
        }
    }
}

@Preview(name = "Book Card Skeleton - Dark", showBackground = true)
@Composable
private fun BookCardSkeletonPreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        BookCardSkeleton(
            modifier = Modifier
                .width(160.dp)
                .padding(16.dp),
        )
    }
}
