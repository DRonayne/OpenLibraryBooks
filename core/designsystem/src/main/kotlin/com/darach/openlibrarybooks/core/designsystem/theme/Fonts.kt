package com.darach.openlibrarybooks.core.designsystem.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.darach.openlibrarybooks.core.designsystem.R

/**
 * Google Font provider configuration for the app.
 */
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/**
 * Font family for body text using Open Sans from Google Fonts.
 */
val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Open Sans"),
        fontProvider = provider,
    ),
)

/**
 * Font family for display text using Merriweather from Google Fonts.
 */
val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Merriweather"),
        fontProvider = provider,
    ),
)
