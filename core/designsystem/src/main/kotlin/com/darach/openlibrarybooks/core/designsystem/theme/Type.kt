package com.darach.openlibrarybooks.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive typography scale for Open Library Books.
 *
 * Uses:
 * - Display/Headline/Title: Merriweather (serif, for elegance and readability)
 * - Body/Label: Open Sans (sans-serif, for clarity and modern feel)
 *
 * Note: Font families should be configured at the app level with GoogleFont provider.
 * This baseline typography can be customised with font families at runtime.
 */
private val baseline = Typography()

/**
 * Creates display text styles with custom font family.
 */
private fun createDisplayStyles(fontFamily: FontFamily) = Triple(
    baseline.displayLarge.copy(
        fontFamily = fontFamily,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    baseline.displayMedium.copy(fontFamily = fontFamily, fontSize = 45.sp, lineHeight = 52.sp),
    baseline.displaySmall.copy(fontFamily = fontFamily, fontSize = 36.sp, lineHeight = 44.sp),
)

/**
 * Creates headline text styles with custom font family.
 */
private fun createHeadlineStyles(fontFamily: FontFamily) = Triple(
    baseline.headlineLarge.copy(fontFamily = fontFamily, fontSize = 32.sp, lineHeight = 40.sp),
    baseline.headlineMedium.copy(fontFamily = fontFamily, fontSize = 28.sp, lineHeight = 36.sp),
    baseline.headlineSmall.copy(fontFamily = fontFamily, fontSize = 24.sp, lineHeight = 32.sp),
)

/**
 * Creates title text styles with custom font family.
 */
private fun createTitleStyles(fontFamily: FontFamily) = Triple(
    baseline.titleLarge.copy(
        fontFamily = fontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
    ),
    baseline.titleMedium.copy(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontWeight = FontWeight.Medium,
    ),
    baseline.titleSmall.copy(
        fontFamily = fontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium,
    ),
)

/**
 * Creates body text styles with custom font family.
 */
private fun createBodyStyles(fontFamily: FontFamily) = Triple(
    baseline.bodyLarge.copy(fontFamily = fontFamily, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    baseline.bodyMedium.copy(fontFamily = fontFamily, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    baseline.bodySmall.copy(fontFamily = fontFamily, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
)

/**
 * Creates label text styles with custom font family.
 */
private fun createLabelStyles(fontFamily: FontFamily) = Triple(
    baseline.labelLarge.copy(
        fontFamily = fontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium,
    ),
    baseline.labelMedium.copy(
        fontFamily = fontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Medium,
    ),
    baseline.labelSmall.copy(
        fontFamily = fontFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Medium,
    ),
)

/**
 * Creates Material 3 typography with custom font families.
 *
 * @param displayFontFamily Font family for display, headline, and title text
 * @param bodyFontFamily Font family for body and label text
 * @return Configured Typography instance
 */
fun createTypography(
    displayFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
): Typography {
    val (displayLarge, displayMedium, displaySmall) = createDisplayStyles(displayFontFamily)
    val (headlineLarge, headlineMedium, headlineSmall) = createHeadlineStyles(displayFontFamily)
    val (titleLarge, titleMedium, titleSmall) = createTitleStyles(displayFontFamily)
    val (bodyLarge, bodyMedium, bodySmall) = createBodyStyles(bodyFontFamily)
    val (labelLarge, labelMedium, labelSmall) = createLabelStyles(bodyFontFamily)

    return Typography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall,
    )
}

/**
 * Default typography using system fonts.
 */
val DefaultTypography = createTypography()
