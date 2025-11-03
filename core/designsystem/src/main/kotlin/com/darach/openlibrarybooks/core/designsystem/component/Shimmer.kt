package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme

/**
 * Creates a shimmer brush that animates from left to right.
 * The brush uses theme-adaptive colours for light and dark modes.
 *
 * @param shimmerColors List of colours to use in the gradient. Defaults to theme-adaptive shimmer colours.
 * @return Animated brush for shimmer effect
 */
@Composable
fun rememberShimmerBrush(
    shimmerColors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ),
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-translate",
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation - 1000f, y = 0f),
        end = Offset(x = translateAnimation, y = 100f),
    )
}

/**
 * A reusable shimmer box that displays a loading placeholder with animated shimmer effect.
 * Automatically adapts to the current theme (light/dark mode).
 *
 * @param modifier Modifier to be applied to the shimmer box
 * @param shape Shape of the shimmer box. Defaults to MaterialTheme.shapes.medium
 */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.medium) {
    val shimmerBrush = rememberShimmerBrush()

    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush),
    )
}

@Preview(name = "Shimmer Box - Light", showBackground = true)
@Composable
private fun ShimmerBoxPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
    }
}

@Preview(name = "Shimmer Box - Dark", showBackground = true)
@Composable
private fun ShimmerBoxPreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
    }
}

@Preview(name = "Shimmer Circle", showBackground = true)
@Composable
private fun ShimmerCirclePreview() {
    OpenLibraryTheme {
        ShimmerBox(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
        )
    }
}
