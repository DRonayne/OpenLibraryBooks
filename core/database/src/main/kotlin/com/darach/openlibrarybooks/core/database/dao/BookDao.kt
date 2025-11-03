package com.darach.openlibrarybooks.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.darach.openlibrarybooks.core.database.entity.BookEntity
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Data Access Object for BookEntity.
 *
 * Provides reactive queries using RxJava3 for observing data changes and operations.
 * Also includes suspend functions for write operations to support coroutines.
 * Uses REPLACE strategy for inserts to handle updates automatically.
 */
@Dao
interface BookDao {

    /**
     * Insert a book into the database.
     * If a book with the same composite key exists, it will be replaced.
     *
     * @param book The book to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity)

    /**
     * Insert multiple books into the database.
     * If books with the same composite keys exist, they will be replaced.
     *
     * @param books The books to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<BookEntity>)

    /**
     * Insert multiple books reactively.
     * If books with the same composite keys exist, they will be replaced.
     *
     * @param books The books to insert
     * @return Completable that completes when insertion is finished
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRx(books: List<BookEntity>): Completable

    /**
     * Update an existing book in the database.
     *
     * @param book The book to update
     */
    @Update
    suspend fun update(book: BookEntity)

    /**
     * Get all books from the database, ordered by date added (newest first).
     * Emits a new list whenever the data changes.
     *
     * @return Flowable emitting the list of all books
     */
    @Query("SELECT * FROM books ORDER BY date_added DESC")
    fun getAllBooks(): Flowable<List<BookEntity>>

    /**
     * Get books filtered by reading status, ordered by date added (newest first).
     * Emits a new list whenever the data changes.
     *
     * @param status The reading status to filter by
     * @return Flowable emitting the filtered list of books
     */
    @Query("SELECT * FROM books WHERE reading_status = :status ORDER BY date_added DESC")
    fun getBooksByStatus(status: ReadingStatus): Flowable<List<BookEntity>>

    /**
     * Get a single book by its composite key reactively.
     * Emits a new value whenever the book data changes, or empty if not found.
     *
     * @param compositeKey The book's composite key
     * @return Flowable emitting the book or empty
     */
    @Query("SELECT * FROM books WHERE composite_key = :compositeKey")
    fun getBookByKey(compositeKey: String): Flowable<BookEntity>

    /**
     * Get a single book by its work key reactively.
     * Emits a new value whenever the book data changes, or empty if not found.
     *
     * @param workKey The Open Library work key
     * @return Flowable emitting the book or empty
     */
    @Query("SELECT * FROM books WHERE work_key = :workKey LIMIT 1")
    fun getBookByWorkKey(workKey: String): Flowable<BookEntity>

    /**
     * Search books by title or author name.
     * Case-insensitive search that matches partial strings.
     *
     * @param searchQuery The search query to match against title or authors
     * @return Flowable emitting the list of matching books
     */
    @Query(
        """
        SELECT * FROM books
        WHERE title LIKE '%' || :searchQuery || '%'
        OR authors LIKE '%' || :searchQuery || '%'
        ORDER BY date_added DESC
        """,
    )
    fun searchBooks(searchQuery: String): Flowable<List<BookEntity>>

    /**
     * Get the total count of books in the database as a one-time operation.
     *
     * @return Single emitting the total count
     */
    @Query("SELECT COUNT(*) FROM books")
    fun getBooksCount(): Single<Int>

    /**
     * Get the count of books by reading status as a one-time operation.
     *
     * @param status The reading status to filter by
     * @return Single emitting the count
     */
    @Query("SELECT COUNT(*) FROM books WHERE reading_status = :status")
    fun getBooksCountByStatus(status: ReadingStatus): Single<Int>

    /**
     * Delete a book by its composite key.
     *
     * @param compositeKey The book's composite key
     */
    @Query("DELETE FROM books WHERE composite_key = :compositeKey")
    suspend fun deleteByKey(compositeKey: String)

    /**
     * Delete all books from the database.
     *
     * @return Completable that completes when deletion is finished
     */
    @Query("DELETE FROM books")
    fun deleteAll(): Completable

    /**
     * Update the reading status of a book reactively.
     *
     * @param compositeKey The book's composite key
     * @param status The new reading status
     * @param lastUpdated The timestamp of the update
     * @return Completable that completes when update is finished
     */
    @Query(
        """
        UPDATE books
        SET reading_status = :status, last_updated = :lastUpdated
        WHERE composite_key = :compositeKey
        """,
    )
    fun updateReadingStatus(compositeKey: String, status: ReadingStatus, lastUpdated: Long): Completable
}
