package com.darach.openlibrarybooks.core.domain.repository

import com.darach.openlibrarybooks.core.domain.model.Book
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing favourite books.
 *
 * Handles marking books as favourites, removing favourites, and querying
 * favourite status. All operations are local-only and use RxJava for
 * reactive operations and Flow for observing data changes.
 */
interface FavouritesRepository {

    /**
     * Mark a book as a favourite.
     *
     * Adds the specified book to the user's favourites. If the book is already
     * a favourite, this operation has no effect.
     *
     * @param bookId Unique identifier for the book
     * @return Completable that completes when the favourite is added
     */
    fun addFavourite(bookId: String): Completable

    /**
     * Remove a book from favourites.
     *
     * Removes the specified book from the user's favourites. If the book is not
     * a favourite, this operation has no effect.
     *
     * @param bookId Unique identifier for the book
     * @return Completable that completes when the favourite is removed
     */
    fun removeFavourite(bookId: String): Completable

    /**
     * Toggle a book's favourite status.
     *
     * If the book is currently a favourite, it will be removed. If it's not
     * a favourite, it will be added. Convenient for UI toggle operations.
     *
     * @param bookId Unique identifier for the book
     * @return Completable that completes when the toggle is finished
     */
    fun toggleFavourite(bookId: String): Completable

    /**
     * Observe all favourite books.
     *
     * Returns a Flow that emits the complete list of favourite books whenever
     * the underlying data changes.
     *
     * @return Flow emitting the list of all favourite books
     */
    fun getFavourites(): Flow<List<Book>>

    /**
     * Check if a specific book is marked as a favourite.
     *
     * @param bookId Unique identifier for the book
     * @return Single emitting true if the book is a favourite, false otherwise
     */
    fun isFavourite(bookId: String): Single<Boolean>

    /**
     * Observe the total count of favourite books.
     *
     * Returns a Flow that emits the current count of favourites whenever
     * the count changes. Useful for displaying badges or statistics.
     *
     * @return Flow emitting the current favourite count
     */
    fun getFavouriteCount(): Flow<Int>

    /**
     * Clear all favourite books.
     *
     * Removes all books from the favourites list. This operation cannot be undone.
     *
     * @return Completable that completes when all favourites are cleared
     */
    fun clearAllFavourites(): Completable
}
