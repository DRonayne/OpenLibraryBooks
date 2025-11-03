package com.darach.openlibrarybooks.core.designsystem.util

import android.content.ContentResolver
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val TAG = "ReducedMotion"

/**
 * Checks if the user has enabled reduced motion in system accessibility settings.
 *
 * When reduced motion is enabled, animations should be simplified or disabled
 * to prevent motion sickness and improve accessibility.
 *
 * @return true if reduced motion is enabled in system settings, false otherwise
 */
@Composable
fun isReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        try {
            val resolver: ContentResolver = context.contentResolver
            val animationScale = Settings.Global.getFloat(
                resolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f,
            )
            animationScale == 0f
        } catch (e: Settings.SettingNotFoundException) {
            Log.w(TAG, "Failed to read ANIMATOR_DURATION_SCALE setting, defaulting to reduced motion disabled", e)
            false
        }
    }
}

/**
 * Returns an appropriate animation spec based on reduced motion settings.
 *
 * If reduced motion is enabled, returns a snap animation (instant).
 * Otherwise, returns the provided animation spec.
 *
 * Usage:
 * ```
 * val scale by animateFloatAsState(
 *     targetValue = if (isFavorite) 1.15f else 1f,
 *     animationSpec = animationSpec(
 *         spring(
 *             dampingRatio = Spring.DampingRatioMediumBouncy,
 *             stiffness = Spring.StiffnessLow
 *         )
 *     ),
 *     label = "favorite_scale"
 * )
 * ```
 *
 * @param defaultSpec The animation spec to use when reduced motion is disabled
 * @return snap() if reduced motion is enabled, defaultSpec otherwise
 */
@Composable
fun <T> animationSpec(defaultSpec: AnimationSpec<T>): AnimationSpec<T> {
    val reducedMotion = isReducedMotionEnabled()
    return if (reducedMotion) snap() else defaultSpec
}

/**
 * Returns a spring animation spec that respects reduced motion settings.
 *
 * If reduced motion is enabled, returns snap (instant).
 * Otherwise, returns a spring animation with the specified parameters.
 *
 * @param dampingRatio The damping ratio for the spring (default: MediumBouncy)
 * @param stiffness The stiffness of the spring (default: Low)
 * @return Animation spec that respects reduced motion preferences
 */
@Composable
fun <T> springSpec(
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessLow,
): AnimationSpec<T> = animationSpec(spring(dampingRatio = dampingRatio, stiffness = stiffness))

/**
 * Returns a tween animation spec that respects reduced motion settings.
 *
 * If reduced motion is enabled, returns snap (instant).
 * Otherwise, returns a tween animation with the specified duration.
 *
 * @param durationMillis The duration of the tween animation in milliseconds
 * @return Animation spec that respects reduced motion preferences
 */
@Composable
fun <T> tweenSpec(durationMillis: Int): AnimationSpec<T> = animationSpec(tween(durationMillis = durationMillis))
