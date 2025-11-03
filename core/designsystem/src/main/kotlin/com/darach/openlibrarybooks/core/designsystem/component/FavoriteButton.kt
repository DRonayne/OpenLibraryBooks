package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.designsystem.theme.goldOchre
import com.darach.openlibrarybooks.core.designsystem.theme.primaryDark
import com.darach.openlibrarybooks.core.designsystem.theme.primaryLight
import com.darach.openlibrarybooks.core.designsystem.util.rememberHapticFeedback
import com.darach.openlibrarybooks.core.designsystem.util.springSpec

/**
 * A favourite button with a heart icon that animates when toggled.
 *
 * Displays a filled heart when favourited, outlined heart when not.
 * Animates with spring physics and rotation when favourited for delightful feedback.
 * Includes haptic feedback on toggle for tactile confirmation.
 *
 * @param isFavorite Whether the item is currently favourited
 * @param onToggle Callback when the button is clicked
 * @param modifier Optional modifier for the button
 */
@Composable
fun FavoriteIconButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = rememberHapticFeedback()

    // Animate scale with spring physics for bouncy, natural movement (respects reduced motion)
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = springSpec(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "favorite_scale",
    )

    // No rotation - it stays level
    val rotation = 0f

    // Check if we're using static (green/gold) colours or dynamic theming
    val currentPrimary = MaterialTheme.colorScheme.primary
    val isUsingStaticColors = currentPrimary == primaryLight || currentPrimary == primaryDark

    // Use gold for non-dynamic theme, tertiary for dynamic themes
    val iconTint = if (isUsingStaticColors) goldOchre else MaterialTheme.colorScheme.tertiary

    IconButton(
        onClick = {
            haptic.click()
            onToggle()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = rotation
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = iconTint,
        )
    }
}

/**
 * A filled favourite button with a heart icon that animates when toggled.
 *
 * Uses a filled icon button style with different container colours based on state.
 * Displays a filled heart when favourited, outlined heart when not.
 * Animates with spring physics and rotation when favourited for delightful feedback.
 * Includes haptic feedback on toggle for tactile confirmation.
 *
 * @param isFavorite Whether the item is currently favourited
 * @param onToggle Callback when the button is clicked
 * @param modifier Optional modifier for the button
 */
@Composable
fun FavoriteFilledIconButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = rememberHapticFeedback()

    // Animate scale with spring physics for bouncy, natural movement (respects reduced motion)
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = springSpec(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "favorite_scale",
    )

    // No rotation - it stays level
    val rotation = 0f

    // Check if we're using static (green/gold) colours or dynamic theming
    val currentPrimary = MaterialTheme.colorScheme.primary
    val isUsingStaticColors = currentPrimary == primaryLight || currentPrimary == primaryDark

    // Use gold for non-dynamic theme, tertiary for dynamic themes
    val favoriteIconTint = if (isUsingStaticColors) {
        goldOchre
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    FilledIconButton(
        onClick = {
            haptic.click()
            onToggle()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = rotation
        },
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
            tint = if (isFavorite) favoriteIconTint else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * A favourite button positioned in the top-end corner of a Box.
 *
 * This is a [BoxScope] extension that automatically positions the button
 * in the top-end corner with padding. Perfect for overlaying on book cards.
 * Animates with spring physics and rotation when favourited for delightful feedback.
 * Includes haptic feedback on toggle for tactile confirmation.
 *
 * @param isFavorite Whether the item is currently favourited
 * @param onToggle Callback when the button is clicked
 * @param modifier Optional modifier for custom styling and positioning
 */
@Composable
fun BoxScope.FavoriteIconButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = rememberHapticFeedback()

    // Animate scale with spring physics for bouncy, natural movement (respects reduced motion)
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = springSpec(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "favorite_scale",
    )

    // No rotation - it stays level
    val rotation = 0f

    // Check if we're using static (green/gold) colours or dynamic theming
    val currentPrimary = MaterialTheme.colorScheme.primary
    val isUsingStaticColors = currentPrimary == primaryLight || currentPrimary == primaryDark

    // Use gold for non-dynamic theme, tertiary for dynamic themes
    val favoriteIconTint = if (isUsingStaticColors) {
        goldOchre
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    IconButton(
        onClick = {
            haptic.click()
            onToggle()
        },
        modifier = modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = if (isFavorite) favoriteIconTint else MaterialTheme.colorScheme.onSurface,
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
