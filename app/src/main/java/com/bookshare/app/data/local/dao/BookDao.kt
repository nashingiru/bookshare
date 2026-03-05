package com.bookshare.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bookshare.app.data.local.entities.BookEntity

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE ownerUid = :uid ORDER BY createdAt DESC")
    fun getBooksByOwner(uid: String): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE genre = :genre ORDER BY createdAt DESC")
    fun getBooksByGenre(genre: String): LiveData<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    @Query("UPDATE books SET isAvailable = :available WHERE id = :bookId")
    suspend fun updateAvailability(bookId: String, available: Boolean)
}
