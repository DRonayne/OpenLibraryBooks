package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme

/**
 * Displays a book cover image with automatic sizing.
 *
 * Shows the cover image if available, otherwise displays a placeholder
 * with a book icon. Supports both fixed dimensions and aspect ratio sizing.
 *
 * @param coverUrl URL to the book cover image (null or empty shows placeholder)
 * @param title Book title for content description
 * @param modifier Optional modifier for the cover container
 * @param width Fixed width for the cover (if null, uses fillMaxWidth)
 * @param height Fixed height for the cover (if null, uses aspect ratio 0.63)
 */
@Composable
fun BookCover(coverUrl: String?, title: String, modifier: Modifier = Modifier, width: Dp? = null, height: Dp? = null) {
    Box(
        modifier = modifier
            .then(
                when {
                    width != null && height != null ->
                        Modifier
                            .width(width)
                            .height(height)
                    width != null -> Modifier.width(width).aspectRatio(0.63f)
                    else -> Modifier.fillMaxWidth().aspectRatio(0.63f)
                },
            )
            .clip(MaterialTheme.shapes.medium),
    ) {
        if (coverUrl.isNullOrEmpty()) {
            // Placeholder with book icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
        } else {
            // Actual cover image
            AsyncImage(
                model = coverUrl,
                contentDescription = "Cover for $title",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview(name = "Cover with Image")
@Composable
private fun BookCoverWithImagePreview() {
    OpenLibraryTheme {
        BookCover(
            coverUrl = "https://covers.openlibrary.org/b/id/123456-L.jpg",
            title = "Sample Book",
        )
    }
}

@Preview(name = "Cover Placeholder")
@Composable
private fun BookCoverPlaceholderPreview() {
    OpenLibraryTheme {
        BookCover(
            coverUrl = null,
            title = "Sample Book",
        )
    }
}

// Removed BookCoverWithFavoriteButtonPreview as favorite button is now added separately in usage

@Preview(name = "Fixed Size Cover")
@Composable
private fun BookCoverFixedSizePreview() {
    OpenLibraryTheme {
        BookCover(
            coverUrl = null,
            title = "Sample Book",
            width = 100.dp,
            height = 160.dp,
        )
    }
}

@Preview(name = "Dark - Placeholder")
@Composable
private fun BookCoverDarkPreview() {
    OpenLibraryTheme(darkTheme = true) {
        BookCover(
            coverUrl = null,
            title = "Sample Book",
        )
    }
}
