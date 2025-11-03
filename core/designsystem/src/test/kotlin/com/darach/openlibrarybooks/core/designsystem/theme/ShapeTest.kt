package com.darach.openlibrarybooks.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Material 3 shape configuration.
 */
class ShapeTest {

    @Test
    fun `OpenLibraryShapes should have correct corner radii`() {
        // Given the shape configuration
        val shapes = OpenLibraryShapes

        // Then shapes should match M3 specifications
        assertEquals(
            "Extra small should be 4dp",
            RoundedCornerShape(4.dp),
            shapes.extraSmall,
        )
        assertEquals(
            "Small should be 8dp",
            RoundedCornerShape(8.dp),
            shapes.small,
        )
        assertEquals(
            "Medium should be 12dp",
            RoundedCornerShape(12.dp),
            shapes.medium,
        )
        assertEquals(
            "Large should be 16dp",
            RoundedCornerShape(16.dp),
            shapes.large,
        )
        assertEquals(
            "Extra large should be 28dp",
            RoundedCornerShape(28.dp),
            shapes.extraLarge,
        )
    }
}
