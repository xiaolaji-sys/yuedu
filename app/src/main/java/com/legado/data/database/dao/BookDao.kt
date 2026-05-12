package com.legado.data.database.dao

import androidx.room.*
import com.legado.data.database.entities.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Book operations
 */
@Dao
interface BookDao {

    /**
     * Insert a book into the database
     * If a book with the same bookId exists, it will be replaced
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long

    /**
     * Insert multiple books at once
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<BookEntity>)

    /**
     * Update an existing book
     */
    @Update
    suspend fun update(book: BookEntity)

    /**
     * Delete a book from the database
     */
    @Delete
    suspend fun delete(book: BookEntity)

    /**
     * Delete multiple books
     */
    @Delete
    suspend fun deleteAll(books: List<BookEntity>)

    /**
     * Get all books ordered by last check time
     */
    @Query("SELECT * FROM books ORDER BY last_check_time DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    /**
     * Get a specific book by its ID
     */
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    /**
     * Get a specific book by its bookId
     */
    @Query("SELECT * FROM books WHERE book_id = :bookId")
    suspend fun getBookByBookId(bookId: String): BookEntity?

    /**
     * Search books by name or author (case-insensitive)
     */
    @Query("""
        SELECT * FROM books
        WHERE is_deleted = 0
        AND (
            LOWER(name) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(author) LIKE '%' || LOWER(:query) || '%'
        )
        ORDER BY last_check_time DESC
    """)
    fun searchBooks(query: String): Flow<List<BookEntity>>

    /**
     * Get books by category
     */
    @Query("SELECT * FROM books WHERE category = :category AND is_deleted = 0 ORDER BY last_check_time DESC")
    fun getBooksByCategory(category: String): Flow<List<BookEntity>>

    /**
     * Get recent books (last 7 days)
     */
    @Query("SELECT * FROM books WHERE last_check_time >= :timestamp AND is_deleted = 0 ORDER BY last_check_time DESC")
    fun getRecentBooks(timestamp: Long): Flow<List<BookEntity>>

    /**
     * Get dirty books (books that need to be synced)
     */
    @Query("SELECT * FROM books WHERE is_dirty = 1 AND is_deleted = 0 ORDER BY last_check_time DESC")
    fun getDirtyBooks(): Flow<List<BookEntity>>

    /**
     * Update book's reading progress
     */
    @Query("UPDATE books SET current_chapter_position = :position, is_dirty = 1 WHERE book_id = :bookId")
    suspend fun updateReadingProgress(bookId: String, position: Int)

    /**
     * Mark books as deleted
     */
    @Query("UPDATE books SET is_deleted = 1, is_dirty = 1 WHERE book_id IN (:bookIds)")
    suspend fun markBooksAsDeleted(vararg bookIds: String)

    /**
     * Count total books
     */
    @Query("SELECT COUNT(*) FROM books WHERE is_deleted = 0")
    suspend fun getTotalBookCount(): Int

    /**
     * Clear all books (for testing purposes)
     */
    @Query("DELETE FROM books")
    suspend fun clearAll()
}