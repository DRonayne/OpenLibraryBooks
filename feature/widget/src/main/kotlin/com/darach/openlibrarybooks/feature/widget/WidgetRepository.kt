package com.darach.openlibrarybooks.feature.widget

import android.util.Log
import com.darach.openlibrarybooks.core.database.dao.FavouritesDao
import com.darach.openlibrarybooks.core.domain.model.Book
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching data needed by the favourites widget.
 * Queries Room directly to avoid repository complexity and ensure fast widget updates.
 */
@Singleton
class WidgetRepository @Inject constructor(private val favouritesDao: FavouritesDao) {

    companion object {
        private const val TAG = "WidgetRepository"
        private const val MAX_WIDGET_BOOKS = 3
    }

    /**
     * Get the 3 most recent favourite books for the widget.
     * Returns a Flowable that emits a new list whenever favourites change.
     *
     * @return Flowable emitting list of up to 3 recent favourite books
     */
    fun getRecentFavourites(): Flowable<List<Book>> = favouritesDao.getRecentFavouriteBooks(limit = MAX_WIDGET_BOOKS)
        .map { bookEntities ->
            bookEntities.map { entity ->
                Book(
                    id = entity.compositeKey,
                    title = entity.title,
                    authors = entity.authors,
                    coverUrl = entity.coverUrl,
                    publishYear = entity.firstPublishYear,
                    description = entity.description,
                    subjects = entity.subjects,
                    readingStatus = entity.readingStatus,
                    isFavorite = true, // All books from favourites table are favourites
                    workKey = entity.workKey,
                    editionKey = entity.editionKey,
                    dateAdded = entity.dateAdded,
                )
            }
        }
        .doOnNext { books ->
            Log.d(TAG, "Widget fetched ${books.size} recent favourites")
        }
        .doOnError { error ->
            Log.e(TAG, "Error fetching recent favourites for widget", error)
        }
}
