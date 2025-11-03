package com.darach.openlibrarybooks.core.designsystem.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Material 3 typography configuration.
 */
class TypographyTest {

    @Test
    fun `default typography should use default font family`() {
        // Given default typography
        val typography = DefaultTypography

        // Then all styles should use default font family
        assertEquals(FontFamily.Default, typography.displayLarge.fontFamily)
        assertEquals(FontFamily.Default, typography.bodyLarge.fontFamily)
        assertEquals(FontFamily.Default, typography.labelSmall.fontFamily)
    }

    @Test
    fun `createTypography with custom fonts should apply correct font families`() {
        // Given custom font families
        val displayFont = FontFamily.Serif
        val bodyFont = FontFamily.SansSerif

        // When creating typography with custom fonts
        val typography = createTypography(
            displayFontFamily = displayFont,
            bodyFontFamily = bodyFont,
        )

        // Then display styles should use display font
        assertEquals(displayFont, typography.displayLarge.fontFamily)
        assertEquals(displayFont, typography.displayMedium.fontFamily)
        assertEquals(displayFont, typography.displaySmall.fontFamily)
        assertEquals(displayFont, typography.headlineLarge.fontFamily)
        assertEquals(displayFont, typography.titleLarge.fontFamily)

        // And body/label styles should use body font
        assertEquals(bodyFont, typography.bodyLarge.fontFamily)
        assertEquals(bodyFont, typography.bodyMedium.fontFamily)
        assertEquals(bodyFont, typography.labelLarge.fontFamily)
        assertEquals(bodyFont, typography.labelSmall.fontFamily)
    }

    @Test
    fun `display large should have correct specifications`() {
        // Given typography
        val typography = DefaultTypography

        // Then display large should match M3 specs
        assertEquals(57.sp, typography.displayLarge.fontSize)
        assertEquals(64.sp, typography.displayLarge.lineHeight)
        assertEquals((-0.25).sp, typography.displayLarge.letterSpacing)
        assertEquals(FontWeight.Normal, typography.displayLarge.fontWeight)
    }

    @Test
    fun `body medium should have correct specifications`() {
        // Given typography
        val typography = DefaultTypography

        // Then body medium should match M3 specs
        assertEquals(14.sp, typography.bodyMedium.fontSize)
        assertEquals(20.sp, typography.bodyMedium.lineHeight)
        assertEquals(0.25.sp, typography.bodyMedium.letterSpacing)
        assertEquals(FontWeight.Normal, typography.bodyMedium.fontWeight)
    }

    @Test
    fun `label large should have correct specifications`() {
        // Given typography
        val typography = DefaultTypography

        // Then label large should match M3 specs
        assertEquals(14.sp, typography.labelLarge.fontSize)
        assertEquals(20.sp, typography.labelLarge.lineHeight)
        assertEquals(0.1.sp, typography.labelLarge.letterSpacing)
        assertEquals(FontWeight.Medium, typography.labelLarge.fontWeight)
    }

    @Test
    fun `title styles should use medium font weight`() {
        // Given typography
        val typography = DefaultTypography

        // Then all title styles should use medium weight
        assertEquals(FontWeight.Medium, typography.titleLarge.fontWeight)
        assertEquals(FontWeight.Medium, typography.titleMedium.fontWeight)
        assertEquals(FontWeight.Medium, typography.titleSmall.fontWeight)
    }

    @Test
    fun `headline small should have correct specifications`() {
        // Given typography
        val typography = DefaultTypography

        // Then headline small should match M3 specs
        assertEquals(24.sp, typography.headlineSmall.fontSize)
        assertEquals(32.sp, typography.headlineSmall.lineHeight)
        assertEquals(0.sp, typography.headlineSmall.letterSpacing)
        assertEquals(FontWeight.Normal, typography.headlineSmall.fontWeight)
    }
}
