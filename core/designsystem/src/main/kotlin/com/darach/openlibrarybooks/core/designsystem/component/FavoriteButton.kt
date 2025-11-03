package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.designsystem.theme.goldOchre

/**
 * A favourite button with a heart icon that animates when toggled.
 *
 * Displays a filled heart when favourited, outlined heart when not.
 * Scales up slightly when favourited for visual feedback.
 *
 * @param isFavorite Whether the item is currently favourited
 * @param onToggle Callback when the button is clicked
 * @param modifier Optional modifier for the button
 */
@Composable
fun FavoriteIconButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    // Animate scale when favourite status changes
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "favorite_scale",
    )

    IconButton(
        onClick = onToggle,
        modifier = modifier.scale(scale),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = goldOchre,
        )
    }
}

/**
 * A filled favourite button with a heart icon that animates when toggled.
 *
 * Uses a filled icon button style with different container colours based on state.
 * Displays a filled heart when favourited, outlined heart when not.
 * Scales up slightly when favourited for visual feedback.
 *
 * @param isFavorite Whether the item is currently favourited
 * @param onToggle Callback when the button is clicked
 * @param modifier Optional modifier for the button
 */
@Composable
fun FavoriteFilledIconButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    // Animate scale when favourite status changes
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "favorite_scale",
    )

    FilledIconButton(
        onClick = onToggle,
        modifier = modifier.scale(scale),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isFavorite) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = goldOchre,
        )
    }
}

/**
 * A favourite button positioned in the top-end corner of a Box.
 *
 * This is a [BoxScope] extension that automatically positions the button
 * in the top-end corner with padding. Perfect for overlaying on book cards.
 *
 * @param isFavorite Whether the item is currently favourited
 * @param onToggle Callback when the button is clicked
 */
@Composable
fun BoxScope.FavoriteIconButton(isFavorite: Boolean, onToggle: () -> Unit) {
    // Animate scale when favourite status changes
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "favorite_scale",
    )

    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .scale(scale),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = if (isFavorite) goldOchre else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(name = "Not Favorite")
@Composable
private fun FavoriteIconButtonNotFavoritePreview() {
    OpenLibraryTheme {
        FavoriteIconButton(
            isFavorite = false,
            onToggle = {},
        )
    }
}

@Preview(name = "Is Favorite")
@Composable
private fun FavoriteIconButtonFavoritePreview() {
    OpenLibraryTheme {
        FavoriteIconButton(
            isFavorite = true,
            onToggle = {},
        )
    }
}

@Preview(name = "Filled - Not Favorite")
@Composable
private fun FavoriteFilledIconButtonNotFavoritePreview() {
    OpenLibraryTheme {
        FavoriteFilledIconButton(
            isFavorite = false,
            onToggle = {},
        )
    }
}

@Preview(name = "Filled - Is Favorite")
@Composable
private fun FavoriteFilledIconButtonFavoritePreview() {
    OpenLibraryTheme {
        FavoriteFilledIconButton(
            isFavorite = true,
            onToggle = {},
        )
    }
}

@Preview(name = "Dark - Is Favorite")
@Composable
private fun FavoriteIconButtonDarkPreview() {
    OpenLibraryTheme(darkTheme = true) {
        FavoriteIconButton(
            isFavorite = true,
            onToggle = {},
        )
    }
}
