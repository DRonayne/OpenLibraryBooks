package com.darach.openlibrarybooks.feature.widget

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * Trampoline activity that launches MainActivity with favourites deeplink.
 * Used by the FavouritesWidget to ensure proper navigation to favourites screen.
 */
class WidgetLauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch MainActivity with favourites deeplink
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("openlibrarybooks://favourites")
            setClassName(packageName, "com.darach.openlibrarybooks.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        startActivity(intent)
        finish()
    }
}
