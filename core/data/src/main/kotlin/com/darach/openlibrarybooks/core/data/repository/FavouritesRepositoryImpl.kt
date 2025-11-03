package com.darach.openlibrarybooks.core.data.repository

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.darach.openlibrarybooks.core.data.mapper.toBook
import com.darach.openlibrarybooks.core.database.dao.FavouritesDao
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.asFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FavouritesRepository for managing favourite books.
 *
 * Uses RxJava for reactive operations and Room's Flowable converted to Kotlin Flow
 * for observing favourites data. Delegates favourite management to FavouritesDao
 * which maintains a separate favourites table with JOIN queries for book details.
 *
 * Also triggers widget updates when favourites change by enqueueing a background
 * worker to cache book cover images for the widget.
 *
 * @property favouritesDao The local database DAO for favourites
 * @property context Application context for WorkManager
 */
@Singleton
class FavouritesRepositoryImpl @Inject constructor(
    private val favouritesDao: FavouritesDao,
    @ApplicationContext private val context: Context,
) : FavouritesRepository {

    companion object {
        private const val TAG = "FavouritesRepositoryImpl"
        private const val WIDGET_CACHE_WORKER_NAME = "cache_widget_images"
    }

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    /**
     * Add a book to favourites.
     *
     * @param bookId The composite key of the book to favourite
     * @return Completable that completes when the operation finishes
     */
    override fun addFavourite(bookId: String): Completable = Completable.fromAction {
        kotlinx.coroutines.runBlocking {
            favouritesDao.insertFavourite(
                com.darach.openlibrarybooks.core.database.entity.FavouritesEntity(
                    bookCompositeKey = bookId,
                    dateAdded = System.currentTimeMillis(),
                ),
            )
            Log.i(TAG, "Added book to favourites: $bookId")
            triggerWidgetUpdate()
        }
    }.subscribeOn(Schedulers.io())

    /**
     * Remove a book from favourites.
     *
     * @param bookId The composite key of the book to unfavourite
     * @return Completable that completes when the operation finishes
     */
    override fun removeFavourite(bookId: String): Completable = Completable.fromAction {
        kotlinx.coroutines.runBlocking {
            favouritesDao.deleteFavourite(bookId)
            Log.i(TAG, "Removed book from favourites: $bookId")
            triggerWidgetUpdate()
        }
    }.subscribeOn(Schedulers.io())

    /**
     * Toggle favourite status for a book.
     * If the book is favourited, it will be unfavourited, and vice versa.
     *
     * @param bookId The composite key of the book to toggle
     * @return Completable that completes when the operation finishes
     */
    override fun toggleFavourite(bookId: String): Completable = Completable.fromAction {
        kotlinx.coroutines.runBlocking {
            favouritesDao.toggleFavourite(bookId, System.currentTimeMillis())
            Log.i(TAG, "Toggled favourite status for book: $bookId")
            triggerWidgetUpdate()
        }
    }.subscribeOn(Schedulers.io())

    /**
     * Observe all favourite books from the local database.
     *
     * Returns a Flow that emits the complete list of favourites whenever the
     * underlying data changes, ordered by date added (most recent first).
     *
     * @return Flow emitting the list of favourite books
     */
    override fun getFavourites(): Flow<List<Book>> = favouritesDao.getAllFavouriteBooks()
        .map { entities ->
            entities.map { entity ->
                entity.toBook().copy(isFavorite = true)
            }
        }
        .subscribeOn(Schedulers.io())
        .toObservable()
        .asFlow()

    /**
     * Check if a book is marked as favourite reactively.
     *
     * @param bookId The composite key of the book to check
     * @return Single emitting true if the book is favourited, false otherwise
     */
    override fun isFavourite(bookId: String): Single<Boolean> = favouritesDao.isFavourite(bookId)
        .firstOrError()
        .subscribeOn(Schedulers.io())
        .doOnSuccess { isFav ->
            Log.d(TAG, "Favourite status for $bookId: $isFav")
        }

    /**
     * Get the total count of favourite books reactively.
     *
     * @return Flow emitting the current count of favourites
     */
    override fun getFavouriteCount(): Flow<Int> = favouritesDao.getAllFavouriteBooks()
        .map { it.size }
        .subscribeOn(Schedulers.io())
        .toObservable()
        .asFlow()

    /**
     * Clear all favourites from the local database.
     *
     * @return Completable that completes when deletion is finished
     */
    override fun clearAllFavourites(): Completable = favouritesDao.deleteAllFavourites()
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Cleared all favourites")
            triggerWidgetUpdate()
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to clear favourites", error)
        }

    /**
     * Triggers a background worker to cache book cover images for the favourites widget.
     * This ensures the widget displays the most recent favourites with their cover images.
     */
    @Suppress("UNCHECKED_CAST")
    private fun triggerWidgetUpdate() {
        try {
            // We need to use reflection to get the worker class since we can't directly
            // reference feature module classes from core module
            val workerClass = Class.forName(
                "com.darach.openlibrarybooks.feature.widget.CacheWidgetImagesWorker",
            ) as Class<out androidx.work.ListenableWorker>

            val workRequest = androidx.work.OneTimeWorkRequest.Builder(workerClass).build()

            workManager.enqueueUniqueWork(
                WIDGET_CACHE_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )

            Log.d(TAG, "Triggered widget image caching worker")
        } catch (e: ClassNotFoundException) {
            // Widget module might not be included in build configuration - this is expected
            Log.d(TAG, "Widget worker not found, skipping widget update: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid worker class: ${e.message}")
        }
    }
}
