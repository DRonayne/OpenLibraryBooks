package com.darach.openlibrarybooks.feature.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget receiver for the favourites widget.
 * Handles widget lifecycle events (onUpdate, onDeleted, onDisabled).
 */
class FavouritesWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "FavouritesWidgetReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget = FavouritesWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "Widget updated for ${appWidgetIds.size} instance(s)")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "Widget deleted: ${appWidgetIds.size} instance(s)")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Last widget instance removed")
    }
}
