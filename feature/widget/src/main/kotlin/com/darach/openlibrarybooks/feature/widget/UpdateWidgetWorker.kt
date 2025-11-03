package com.darach.openlibrarybooks.feature.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to update the favourites widget when favourites change.
 * Triggered by FavouritesRepository when the user toggles a favourite.
 */
@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(@Assisted context: Context, @Assisted params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "UpdateWidgetWorker"
        const val WORK_NAME = "update_favourites_widget"
    }

    override suspend fun doWork(): Result = try {
        Log.d(TAG, "Starting widget update")
        WidgetUpdater.updateFavouritesWidget(applicationContext)
        Log.d(TAG, "Widget update completed successfully")
        Result.success()
    } catch (e: IllegalStateException) {
        Log.e(TAG, "Widget update failed due to invalid state", e)
        Result.failure()
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "Widget update failed due to invalid arguments", e)
        Result.failure()
    } catch (e: SecurityException) {
        Log.e(TAG, "Widget update failed due to security restrictions", e)
        Result.failure()
    }
}
