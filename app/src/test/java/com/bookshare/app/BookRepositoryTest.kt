package com.bookshare.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bookshare.app.data.local.dao.BookDao
import com.bookshare.app.data.remote.api.OpenLibraryApi
import com.bookshare.app.data.remote.dto.BookSearchDoc
import com.bookshare.app.data.remote.dto.OpenLibrarySearchResponse
import com.bookshare.app.model.Book
import com.bookshare.app.model.Resource
import com.bookshare.app.repository.BookRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BookRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var bookDao: BookDao

    @Mock
    private lateinit var openLibraryApi: OpenLibraryApi

    @Mock
    private lateinit var firestore: FirebaseFirestore

    private lateinit var repository: BookRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = BookRepository(bookDao, openLibraryApi, firestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchBooksFromApi returns success when response is successful`() = runTest {
        // Given
        val mockDocs = listOf(
            BookSearchDoc(key = "/works/OL123W", title = "El Quijote", authorName = listOf("Cervantes"))
        )
        val mockResponse = OpenLibrarySearchResponse(numFound = 1, docs = mockDocs)
        whenever(openLibraryApi.searchBooks("Quijote")).thenReturn(Response.success(mockResponse))

        // When
        val result = repository.searchBooksFromApi("Quijote")

        // Then
        assertTrue(result is Resource.Success)
        val books = (result as Resource.Success).data
        assertEquals(1, books.size)
        assertEquals("El Quijote", books.first().title)
    }

    @Test
    fun `searchBooksFromApi returns error on exception`() = runTest {
        // Given
        whenever(openLibraryApi.searchBooks("Quijote")).thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.searchBooksFromApi("Quijote")

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `getBookById returns null when not found`() = runTest {
        // Given
        whenever(bookDao.getBookById("nonexistent")).thenReturn(null)

        // When
        val result = repository.getBookById("nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `Book model default values are correct`() {
        val book = Book(id = "1", title = "Test", author = "Author")
        assertTrue(book.isAvailable)
        assertEquals("", book.isbn)
        assertEquals(0, book.pageCount)
    }

    @Test
    fun `BookSearchDoc getCoverUrl returns correct url when coverId exists`() {
        val doc = BookSearchDoc(key = "/works/OL1W", title = "Test", coverId = 12345)
        assertEquals("https://covers.openlibrary.org/b/id/12345-M.jpg", doc.getCoverUrl())
    }

    @Test
    fun `BookSearchDoc getCoverUrl returns empty string when no coverId`() {
        val doc = BookSearchDoc(key = "/works/OL1W", title = "Test", coverId = null)
        assertEquals("", doc.getCoverUrl())
    }

    @Test
    fun `BookSearchDoc getAuthor returns unknown when no authors`() {
        val doc = BookSearchDoc(key = "/works/OL1W", title = "Test", authorName = null)
        assertEquals("Autor desconocido", doc.getAuthor())
    }
}
