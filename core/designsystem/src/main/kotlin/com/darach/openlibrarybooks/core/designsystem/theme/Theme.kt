package com.darach.openlibrarybooks.core.designsystem.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Light colour scheme for Open Library Books app
 */
private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

/**
 * Dark colour scheme for Open Library Books app.
 * Optimised for low-light viewing with proper contrast ratios.
 */
private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

/**
 * Animates primary colours in a ColorScheme.
 */
@Composable
private fun ColorScheme.animatePrimaryColors(animationSpec: AnimationSpec<Color>): Triple<Color, Color, Color> {
    val primary by animateColorAsState(primary, animationSpec, label = "primary")
    val onPrimary by animateColorAsState(onPrimary, animationSpec, label = "onPrimary")
    val primaryContainer by animateColorAsState(primaryContainer, animationSpec, label = "primaryContainer")
    return Triple(primary, onPrimary, primaryContainer)
}

/**
 * Animates secondary colours in a ColorScheme.
 */
@Composable
private fun ColorScheme.animateSecondaryColors(animationSpec: AnimationSpec<Color>): Triple<Color, Color, Color> {
    val secondary by animateColorAsState(secondary, animationSpec, label = "secondary")
    val onSecondary by animateColorAsState(onSecondary, animationSpec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(secondaryContainer, animationSpec, label = "secondaryContainer")
    return Triple(secondary, onSecondary, secondaryContainer)
}

/**
 * Animates surface colours in a ColorScheme.
 */
@Composable
private fun ColorScheme.animateSurfaceColors(animationSpec: AnimationSpec<Color>): Pair<Color, Color> {
    val surface by animateColorAsState(surface, animationSpec, label = "surface")
    val onSurface by animateColorAsState(onSurface, animationSpec, label = "onSurface")
    return Pair(surface, onSurface)
}

/**
 * Animates color changes in a ColorScheme for smooth theme transitions.
 *
 * @return Animated colour scheme with smooth transitions
 */
@Composable
private fun ColorScheme.animate(): ColorScheme {
    val animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)
    val (primary, onPrimary, primaryContainer) = animatePrimaryColors(animationSpec)
    val (secondary, onSecondary, secondaryContainer) = animateSecondaryColors(animationSpec)
    val (surface, onSurface) = animateSurfaceColors(animationSpec)

    return copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = animateColorAsState(
            onSecondaryContainer,
            animationSpec,
            label = "onSecondaryContainer",
        ).value,
        surface = surface,
        onSurface = onSurface,
    )
}

/**
 * Material 3 Expressive theme for Open Library Books.
 *
 * Supports:
 * - Light and dark modes
 * - Dynamic colours (Material You) on Android 12+
 * - Smooth animated theme transitions
 * - Custom typography (Merriweather + Open Sans) and shapes
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param dynamicColor Whether to use dynamic colours from system wallpaper (Android 12+).
 * @param animateThemeChanges Whether to animate colour transitions between theme changes.
 * @param content The composable content to theme.
 */
@Composable
fun OpenLibraryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    animateThemeChanges: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Select appropriate colour scheme
    val baseColorScheme = when {
        // Use dynamic colours on Android 12+ if enabled
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // Fallback to static colour schemes
        darkTheme -> darkScheme
        else -> lightScheme
    }

    // Apply smooth colour animations if enabled
    val animatedColorScheme = if (animateThemeChanges) {
        baseColorScheme.animate()
    } else {
        baseColorScheme
    }

    // Use Merriweather and Open Sans for typography
    val typography = createTypography(
        displayFontFamily = displayFontFamily,
        bodyFontFamily = bodyFontFamily,
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = typography,
        shapes = OpenLibraryShapes,
        content = content,
    )
}
