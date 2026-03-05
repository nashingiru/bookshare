package com.bookshare.app.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.bookshare.app.data.local.dao.BookDao
import com.bookshare.app.data.remote.api.OpenLibraryApi
import com.bookshare.app.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val openLibraryApi: OpenLibraryApi,
    private val firestore: FirebaseFirestore
) {

    // Local
    fun getAllLocalBooks(): LiveData<List<Book>> =
        bookDao.getAllBooks().map { list -> list.map { it.toDomain() } }

    fun getMyBooks(uid: String): LiveData<List<Book>> =
        bookDao.getBooksByOwner(uid).map { list -> list.map { it.toDomain() } }

    fun searchLocalBooks(query: String): LiveData<List<Book>> =
        bookDao.searchBooks(query).map { list -> list.map { it.toDomain() } }

    suspend fun getBookById(bookId: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookById(bookId)?.toDomain()
    }

    // Remote - Firestore
    suspend fun fetchAllBooksFromFirestore(): Resource<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("books").get().await()
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            books.forEach { bookDao.insertBook(it.toEntity()) }
            Resource.Success(books)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener libros")
        }
    }

    suspend fun addBook(book: Book): Resource<Book> = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection("books").add(book).await()
            val bookWithId = book.copy(id = docRef.id)
            firestore.collection("books").document(docRef.id).set(bookWithId).await()
            bookDao.insertBook(bookWithId.toEntity())
            Resource.Success(bookWithId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al agregar libro")
        }
    }

    suspend fun updateBook(book: Book): Resource<Book> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("books").document(book.id).set(book).await()
            bookDao.updateBook(book.toEntity())
            Resource.Success(book)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar libro")
        }
    }

    suspend fun deleteBook(bookId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("books").document(bookId).delete().await()
            bookDao.deleteBookById(bookId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar libro")
        }
    }

    // Open Library API
    suspend fun searchBooksFromApi(query: String): Resource<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val response = openLibraryApi.searchBooks(query)
            if (response.isSuccessful) {
                val books = response.body()?.docs?.map { it.toDomain() } ?: emptyList()
                Resource.Success(books)
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de red")
        }
    }
}
