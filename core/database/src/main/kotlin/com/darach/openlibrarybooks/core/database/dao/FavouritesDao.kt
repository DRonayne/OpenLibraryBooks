package com.darach.openlibrarybooks.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.database.entity.FavouritesEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Data Access Object for FavouritesEntity.
 *
 * Provides reactive queries for managing favourite books with JOIN operations
 * to fetch complete book details. Uses @Transaction for JOIN queries to ensure
 * data consistency across multiple tables. Primarily uses RxJava3 for reactivity.
 */
@Dao
interface FavouritesDao {

    /**
     * Add a book to favourites.
     * If the book is already favourited, it will be replaced with the new entry.
     *
     * @param favourite The favourite entry to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouritesEntity)

    /**
     * Remove a book from favourites by its composite key.
     *
     * @param bookCompositeKey The composite key of the book to unfavourite
     */
    @Query("DELETE FROM favourites WHERE book_composite_key = :bookCompositeKey")
    suspend fun deleteFavourite(bookCompositeKey: String)

    /**
     * Get all favourite books with complete book details using a JOIN query.
     * Emits a new list whenever favourite books change.
     * Ordered by date added to favourites (most recent first).
     *
     * @return Flowable emitting the list of favourite books
     */
    @Transaction
    @Query(
        """
        SELECT books.* FROM books
        INNER JOIN favourites ON books.composite_key = favourites.book_composite_key
        ORDER BY favourites.date_added DESC
        """,
    )
    fun getAllFavouriteBooks(): Flowable<List<BookEntity>>

    /**
     * Check if a book is marked as favourite reactively.
     * Emits a new value whenever the favourite status changes.
     *
     * @param bookCompositeKey The composite key of the book to check
     * @return Flowable emitting true if the book is favourited, false otherwise
     */
    @Query(
        """
        SELECT EXISTS(SELECT 1 FROM favourites WHERE book_composite_key = :bookCompositeKey)
        """,
    )
    fun isFavourite(bookCompositeKey: String): Flowable<Boolean>

    /**
     * Get the total count of favourite books as a one-time operation.
     *
     * @return Single emitting the total count of favourites
     */
    @Query("SELECT COUNT(*) FROM favourites")
    fun getFavouritesCount(): Single<Int>

    /**
     * Get the most recent favourite books reactively (limited count).
     * Useful for widgets showing recent favourites.
     *
     * @param limit Maximum number of books to return
     * @return Flowable emitting the list of recent favourite books
     */
    @Transaction
    @Query(
        """
        SELECT books.* FROM books
        INNER JOIN favourites ON books.composite_key = favourites.book_composite_key
        ORDER BY favourites.date_added DESC
        LIMIT :limit
        """,
    )
    fun getRecentFavouriteBooks(limit: Int): Flowable<List<BookEntity>>

    /**
     * Delete all favourites from the database.
     *
     * @return Completable that completes when deletion is finished
     */
    @Query("DELETE FROM favourites")
    fun deleteAllFavourites(): Completable

    /**
     * Toggle favourite status for a book.
     * If the book is favourited, it will be unfavourited, and vice versa.
     *
     * This is a convenience method that combines check + insert/delete operations.
     *
     * @param bookCompositeKey The composite key of the book to toggle
     * @param dateAdded The timestamp to use if adding to favourites
     */
    @Transaction
    suspend fun toggleFavourite(bookCompositeKey: String, dateAdded: Long) {
        val exists = isFavouriteSync(bookCompositeKey)
        if (exists) {
            deleteFavourite(bookCompositeKey)
        } else {
            insertFavourite(FavouritesEntity(bookCompositeKey = bookCompositeKey, dateAdded = dateAdded))
        }
    }

    /**
     * Synchronous check for favourite status (for use in transactions).
     *
     * @param bookCompositeKey The composite key of the book to check
     * @return true if the book is favourited, false otherwise
     */
    @Query(
        """
        SELECT EXISTS(SELECT 1 FROM favourites WHERE book_composite_key = :bookCompositeKey)
        """,
    )
    suspend fun isFavouriteSync(bookCompositeKey: String): Boolean
}
