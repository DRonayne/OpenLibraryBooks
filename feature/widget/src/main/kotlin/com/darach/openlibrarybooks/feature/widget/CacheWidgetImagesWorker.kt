package com.darach.openlibrarybooks.feature.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker to cache book cover images for the favourites widget.
 * Fetches the most recent favourite books and downloads their cover images
 * to internal storage for use in Glance widgets.
 */
@HiltWorker
class CacheWidgetImagesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val widgetRepository: WidgetRepository,
    private val imageCache: WidgetImageCache,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CacheWidgetImagesWorker"
        const val WORK_NAME = "cache_widget_images"
    }

    override suspend fun doWork(): Result = runCatching {
        Log.d(TAG, "Starting widget image caching work")

        val favourites = widgetRepository.getRecentFavourites()
            .blockingFirst(emptyList())

        if (favourites.isEmpty()) {
            Log.d(TAG, "No favourites to cache")
            return Result.success()
        }

        Log.d(TAG, "Caching images for ${favourites.size} favourite books")
        val cacheResult = cacheBookImages(favourites)
        Log.d(TAG, "Image caching completed: ${cacheResult.first} succeeded, ${cacheResult.second} failed")

        updateWidget()
        Log.d(TAG, "Widget updated with cached images")

        Result.success()
    }.getOrElse { throwable ->
        Log.e(TAG, "Error caching widget images: ${throwable.message}", throwable)
        Result.retry()
    }

    /**
     * Caches cover images for the given list of books.
     *
     * @param books List of books to cache images for
     * @return Pair of (successCount, failureCount)
     */
    private suspend fun cacheBookImages(
        books: List<com.darach.openlibrarybooks.core.domain.model.Book>,
    ): Pair<Int, Int> {
        var successCount = 0
        var failureCount = 0

        books.forEach { book ->
            val coverUrl = book.coverUrl
            if (coverUrl != null) {
                val cached = imageCache.cacheImage(bookId = book.id, coverUrl = coverUrl)
                if (cached) {
                    successCount++
                } else {
                    failureCount++
                }
            } else {
                Log.d(TAG, "Book ${book.id} has no cover URL, skipping")
            }
        }

        return successCount to failureCount
    }

    /**
     * Updates the favourites widget to display the newly cached images.
     */
    private suspend fun updateWidget() {
        FavouritesWidget().updateAll(applicationContext)
    }
}
