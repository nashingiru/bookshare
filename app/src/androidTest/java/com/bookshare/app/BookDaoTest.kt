package com.bookshare.app

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bookshare.app.data.local.BookShareDatabase
import com.bookshare.app.data.local.dao.BookDao
import com.bookshare.app.data.local.entities.BookEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: BookShareDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BookShareDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetBook() = runTest {
        val book = BookEntity(
            id = "1", title = "El Quijote", author = "Cervantes",
            description = "Clásico español", genre = "Ficción",
            coverUrl = "", ownerUid = "uid1", ownerName = "Juan"
        )
        bookDao.insertBook(book)
        val retrieved = bookDao.getBookById("1")
        assertNotNull(retrieved)
        assertEquals("El Quijote", retrieved?.title)
        assertEquals("Cervantes", retrieved?.author)
    }

    @Test
    fun deleteBook_removesFromDB() = runTest {
        val book = BookEntity(
            id = "del1", title = "Para Borrar", author = "Autor",
            description = "", genre = "Otro", coverUrl = "",
            ownerUid = "uid1", ownerName = "Test"
        )
        bookDao.insertBook(book)
        bookDao.deleteBookById("del1")
        val result = bookDao.getBookById("del1")
        assertNull(result)
    }

    @Test
    fun getBooksCount_returnsCorrectCount() = runTest {
        repeat(3) { i ->
            bookDao.insertBook(
                BookEntity(
                    id = "count$i", title = "Book $i", author = "Author",
                    description = "", genre = "Ficción", coverUrl = "",
                    ownerUid = "uid1", ownerName = "User"
                )
            )
        }
        assertEquals(3, bookDao.getBooksCount())
    }

    @Test
    fun updateAvailability_updatesCorrectly() = runTest {
        val book = BookEntity(
            id = "avail1", title = "Test Book", author = "Author",
            description = "", genre = "Ficción", coverUrl = "",
            ownerUid = "uid1", ownerName = "User", isAvailable = true
        )
        bookDao.insertBook(book)
        bookDao.updateAvailability("avail1", false)
        val updated = bookDao.getBookById("avail1")
        assertFalse(updated?.isAvailable ?: true)
    }
}
