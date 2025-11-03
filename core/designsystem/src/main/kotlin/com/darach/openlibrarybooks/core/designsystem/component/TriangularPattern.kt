package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.designsystem.theme.darkGreen
import com.darach.openlibrarybooks.core.designsystem.theme.lightGreenAccent
import com.darach.openlibrarybooks.core.designsystem.theme.midGreen
import kotlin.random.Random

// Pattern constants
private const val GRID_PADDING = 4

/**
 * Reusable triangular geometric background pattern.
 *
 * Creates a repeating grid of right-angled triangles with alternating tones
 * to produce a faceted texture effect. The pattern adapts to light/dark themes
 * and optionally supports dynamic color theming. Can include a primary color overlay.
 *
 * The pattern is stable and won't randomise on recomposition - uses a fixed seed
 * for consistent pattern generation.
 *
 * @param modifier Modifier to be applied to the pattern
 * @param triangleSize Size of each triangle's base/height in dp
 * @param useDynamicColor Whether to use Material3 dynamic colors from theme instead of fixed greens
 * @param customColors Optional custom color palette to use instead of green or dynamic colors
 * @param overlayColor Optional overlay color to apply on top of the pattern.
 *        Defaults to primary at 75% opacity. Pass Color.Transparent for no overlay.
 */
@Composable
fun TriangularPattern(
    modifier: Modifier = Modifier,
    triangleSize: Float = 280f,
    useDynamicColor: Boolean = false,
    customColors: List<Color>? = null,
    overlayColor: Color? = null,
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Get colors based on theme and dynamic color preference
    val colors = customColors ?: if (useDynamicColor) {
        // Use Material3 color scheme for dynamic theming
        getDynamicPatternColors(isDarkTheme)
    } else {
        // Use fixed green palette
        getGreenPatternColors(isDarkTheme)
    }

    // Determine the overlay color: use provided overlay, or adapt based on theme mode
    val finalOverlayColor = overlayColor ?: if (customColors != null) {
        // Custom colors = no overlay (used in Settings)
        Color.Transparent
    } else if (useDynamicColor) {
        // Dynamic theme = lighter overlay to maintain text visibility
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        // Non-dynamic (green/gold) theme = stronger overlay for brand color
        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Calculate how many triangles fit in width and height
        val cols = (width / triangleSize).toInt() + GRID_PADDING
        val rows = (height / triangleSize).toInt() + GRID_PADDING

        // Draw triangular pattern
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * triangleSize
                val y = row * triangleSize

                // Use stable random colors from the palette based on row/col position
                // Reset random seed for each position to ensure stability
                val positionSeed = row * 1000 + col
                val positionRandom = Random(positionSeed)

                val color = colors[positionRandom.nextInt(colors.size)]
                val secondColor = colors[positionRandom.nextInt(colors.size)]

                // Draw two triangles per cell
                if ((row + col) % 2 == 0) {
                    drawTopLeftTriangle(x, y, triangleSize, color)
                    drawBottomRightTriangle(x, y, triangleSize, secondColor)
                } else {
                    drawTopRightTriangle(x, y, triangleSize, color)
                    drawBottomLeftTriangle(x, y, triangleSize, secondColor)
                }
            }
        }

        // Draw overlay if not transparent
        if (finalOverlayColor != Color.Transparent) {
            drawRect(
                color = finalOverlayColor,
                size = size,
            )
        }
    }
}

/**
 * Get green pattern colors for light or dark theme
 * Only uses medium to dark greens to match the design image
 */
@Composable
private fun getGreenPatternColors(isDarkTheme: Boolean): List<Color> = if (isDarkTheme) {
    // Darker, more muted greens for dark theme
    listOf(
        darkGreen.copy(alpha = 0.6f),
        midGreen.copy(alpha = 0.4f),
        lightGreenAccent.copy(alpha = 0.3f),
    )
} else {
    // Full saturation greens for light theme - only medium to dark tones
    listOf(
        darkGreen,
        midGreen,
        lightGreenAccent,
    )
}

/**
 * Get dynamic pattern colors from Material3 theme
 */
@Composable
private fun getDynamicPatternColors(isDarkTheme: Boolean): List<Color> {
    val colorScheme = MaterialTheme.colorScheme
    return if (isDarkTheme) {
        listOf(
            colorScheme.primaryContainer.copy(alpha = 0.4f),
            colorScheme.secondaryContainer.copy(alpha = 0.3f),
            colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        )
    } else {
        listOf(
            colorScheme.primaryContainer.copy(alpha = 0.8f),
            colorScheme.secondaryContainer.copy(alpha = 0.7f),
            colorScheme.tertiaryContainer.copy(alpha = 0.6f),
        )
    }
}

/**
 * Draw a triangle in the top-left corner of a square
 */
private fun DrawScope.drawTopLeftTriangle(x: Float, y: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y)
        lineTo(x + size, y)
        lineTo(x, y + size)
        close()
    }
    drawPath(path, color)
}

/**
 * Draw a triangle in the bottom-right corner of a square
 */
private fun DrawScope.drawBottomRightTriangle(x: Float, y: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(x + size, y)
        lineTo(x + size, y + size)
        lineTo(x, y + size)
        close()
    }
    drawPath(path, color)
}

/**
 * Draw a triangle in the top-right corner of a square
 */
private fun DrawScope.drawTopRightTriangle(x: Float, y: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y)
        lineTo(x + size, y)
        lineTo(x + size, y + size)
        close()
    }
    drawPath(path, color)
}

/**
 * Draw a triangle in the bottom-left corner of a square
 */
private fun DrawScope.drawBottomLeftTriangle(x: Float, y: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y)
        lineTo(x + size, y + size)
        lineTo(x, y + size)
        close()
    }
    drawPath(path, color)
}

// Previews

@Preview(
    name = "Light Theme - Green Pattern",
    widthDp = 1400,
    heightDp = 350,
    showBackground = true,
)
@Composable
private fun TriangularPatternLightPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(useDynamicColor = false)
        }
    }
}

@Preview(
    name = "Dark Theme - Green Pattern",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
)
@Composable
private fun TriangularPatternDarkPreview() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(useDynamicColor = false)
        }
    }
}

@Preview(
    name = "Light Theme - Dynamic Color",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
)
@Composable
private fun TriangularPatternDynamicLightPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(useDynamicColor = true)
        }
    }
}

@Preview(
    name = "Dark Theme - Dynamic Color",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
)
@Composable
private fun TriangularPatternDynamicDarkPreview() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(useDynamicColor = true)
        }
    }
}

@Preview(
    name = "Tablet - Light",
    widthDp = 1024,
    heightDp = 768,
    showBackground = true,
)
@Composable
private fun TriangularPatternTabletPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(useDynamicColor = false)
        }
    }
}

@Preview(
    name = "Small Triangle Size",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
)
@Composable
private fun TriangularPatternSmallPreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(triangleSize = 40f, useDynamicColor = false)
        }
    }
}

@Preview(
    name = "Large Triangle Size",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
)
@Composable
private fun TriangularPatternLargePreview() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangularPattern(triangleSize = 120f, useDynamicColor = false)
        }
    }
}
