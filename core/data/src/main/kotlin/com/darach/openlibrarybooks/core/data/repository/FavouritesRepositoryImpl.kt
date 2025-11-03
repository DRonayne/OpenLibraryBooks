package com.darach.openlibrarybooks.core.data.repository

import android.util.Log
import com.darach.openlibrarybooks.core.data.mapper.toBook
import com.darach.openlibrarybooks.core.database.dao.FavouritesDao
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
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
 * @property favouritesDao The local database DAO for favourites
 */
@Singleton
class FavouritesRepositoryImpl @Inject constructor(private val favouritesDao: FavouritesDao) : FavouritesRepository {

    companion object {
        private const val TAG = "FavouritesRepositoryImpl"
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
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to clear favourites", error)
        }
}
