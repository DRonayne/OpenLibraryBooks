package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.R
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme

/**
 * Empty state component with illustration, message, and subtle animation.
 *
 * Features:
 * - Different icons and messages for various empty state contexts
 * - Subtle pulsing animation on the icon
 * - Theme-adaptive colours
 *
 * @param message The message to display
 * @param modifier Modifier to be applied to the container
 * @param emptyStateType The type of empty state for contextual icon
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    emptyStateType: EmptyStateType = EmptyStateType.NO_BOOKS_FOUND,
) {
    // Subtle pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "empty-state-pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "empty-state-alpha",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon with subtle pulsing animation
        Icon(
            imageVector = when (emptyStateType) {
                EmptyStateType.NO_BOOKS_FOUND -> Icons.AutoMirrored.Filled.MenuBook
                EmptyStateType.NO_BOOKS_IN_LIBRARY -> Icons.AutoMirrored.Filled.MenuBook
                EmptyStateType.NO_FAVOURITES -> Icons.Default.FavoriteBorder
                EmptyStateType.NO_BOOKS_IN_LIST -> Icons.AutoMirrored.Filled.MenuBook
            },
            contentDescription = stringResource(R.string.empty_state_icon),
            modifier = Modifier
                .size(96.dp)
                .alpha(alpha),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message text
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Types of empty states for different contexts.
 */
enum class EmptyStateType {
    /**
     * No books found in search/filter results
     */
    NO_BOOKS_FOUND,

    /**
     * No books in the user's library yet
     */
    NO_BOOKS_IN_LIBRARY,

    /**
     * No favourite books marked
     */
    NO_FAVOURITES,

    /**
     * No books in a specific reading list (want to read, currently reading, already read)
     */
    NO_BOOKS_IN_LIST,
}

/**
 * Convenience composable for "no books found" empty state.
 *
 * @param modifier Modifier to be applied to the empty state
 */
@Composable
fun NoBooksFoundEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        message = stringResource(R.string.no_books_found),
        modifier = modifier,
        emptyStateType = EmptyStateType.NO_BOOKS_FOUND,
    )
}

/**
 * Convenience composable for "no books in library" empty state.
 *
 * @param modifier Modifier to be applied to the empty state
 */
@Composable
fun NoLibraryBooksEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        message = stringResource(R.string.library_empty),
        modifier = modifier,
        emptyStateType = EmptyStateType.NO_BOOKS_IN_LIBRARY,
    )
}

/**
 * Convenience composable for "no favourites" empty state.
 *
 * @param modifier Modifier to be applied to the empty state
 */
@Composable
fun NoFavouritesEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        message = stringResource(R.string.no_favourite_books),
        modifier = modifier,
        emptyStateType = EmptyStateType.NO_FAVOURITES,
    )
}

@Preview(name = "Empty State - No Books Found - Light", showBackground = true)
@Composable
private fun EmptyStateNoBooksFoundPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        NoBooksFoundEmptyState()
    }
}

@Preview(name = "Empty State - No Books Found - Dark", showBackground = true)
@Composable
private fun EmptyStateNoBooksFoundPreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        NoBooksFoundEmptyState()
    }
}

@Preview(name = "Empty State - No Library Books", showBackground = true)
@Composable
private fun EmptyStateNoLibraryBooksPreview() {
    OpenLibraryTheme {
        NoLibraryBooksEmptyState()
    }
}

@Preview(name = "Empty State - No Favourites", showBackground = true)
@Composable
private fun EmptyStateNoFavouritesPreview() {
    OpenLibraryTheme {
        NoFavouritesEmptyState()
    }
}

@Preview(name = "Empty State - Custom Message", showBackground = true)
@Composable
private fun EmptyStateCustomPreview() {
    OpenLibraryTheme {
        EmptyState(
            message = "No books in this reading list",
            emptyStateType = EmptyStateType.NO_BOOKS_IN_LIST,
        )
    }
}
