package com.darach.openlibrarybooks.core.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Composable wrapper for haptic feedback that provides easy access to haptic feedback functionality.
 *
 * Usage:
 * ```
 * val haptic = rememberHapticFeedback()
 * IconButton(onClick = {
 *     haptic.click()
 *     // ... handle click
 * }) { ... }
 * ```
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    val haptic = LocalHapticFeedback.current
    return remember { HapticFeedbackHelper(haptic) }
}

/**
 * Helper class providing convenient methods for triggering different types of haptic feedback.
 *
 * Encapsulates common haptic feedback patterns used throughout the app:
 * - Light click: For standard button presses and toggles
 * - Long press: For confirming long press gestures
 * - Text handle move: For dragging or repositioning interactions
 *
 * @property haptic The underlying HapticFeedback implementation from Compose
 */
class HapticFeedbackHelper(private val haptic: HapticFeedback) {
    /**
     * Triggers a light click feedback.
     * Use for standard button presses, favourite toggles, card clicks, etc.
     */
    fun click() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Triggers a long press feedback.
     * Use to confirm long press gestures on cards or other elements.
     */
    fun longPress() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Triggers a text handle move feedback (subtle).
     * Can be used for drag interactions or filter/sort changes.
     */
    fun subtle() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
