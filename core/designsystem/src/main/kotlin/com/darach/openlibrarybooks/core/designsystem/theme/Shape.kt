package com.darach.openlibrarybooks.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shape scale for Open Library Books.
 *
 * Defines the corner shapes used throughout the app for components like:
 * - Cards
 * - Buttons
 * - Text fields
 * - Dialogs
 * - Bottom sheets
 *
 * Following M3 guidelines:
 * - Extra Small: 4dp - Chips, small buttons
 * - Small: 8dp - Buttons, text fields
 * - Medium: 12dp - Cards, dialogs
 * - Large: 16dp - Extended FABs, bottom sheets
 * - Extra Large: 28dp - Large cards, prominent surfaces
 */
val OpenLibraryShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
