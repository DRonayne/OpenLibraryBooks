package com.darach.openlibrarybooks.feature.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility object for updating all favourites widget instances.
 * Provides a convenient way to trigger widget updates from anywhere in the app.
 */
object WidgetUpdater {

    private const val TAG = "WidgetUpdater"

    /**
     * Update all instances of the favourites widget.
     * Called when favourites change or on app launch.
     *
     * @param context Android context
     */
    fun updateFavouritesWidget(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(FavouritesWidget::class.java)

                if (glanceIds.isEmpty()) {
                    Log.d(TAG, "No widget instances to update")
                    return@launch
                }

                Log.d(TAG, "Updating ${glanceIds.size} widget instance(s)")
                FavouritesWidget().updateAll(context)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Widget manager not in valid state", e)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid widget configuration", e)
            } catch (e: SecurityException) {
                Log.e(TAG, "Security error while updating widget", e)
            }
        }
    }
}
