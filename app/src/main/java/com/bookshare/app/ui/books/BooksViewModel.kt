package com.bookshare.app.ui.books

import androidx.lifecycle.*
import com.bookshare.app.model.Book
import com.bookshare.app.model.Resource
import com.bookshare.app.repository.AuthRepository
import com.bookshare.app.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")
    val allBooks: LiveData<List<Book>> = bookRepository.getAllLocalBooks()

    val myBooks: LiveData<List<Book>> = authRepository.currentUser?.uid?.let {
        bookRepository.getMyBooks(it)
    } ?: MutableLiveData(emptyList())

    val searchResults: LiveData<List<Book>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) bookRepository.getAllLocalBooks()
        else bookRepository.searchLocalBooks(query)
    }

    private val _addBookState = MutableLiveData<Resource<Book>>()
    val addBookState: LiveData<Resource<Book>> = _addBookState

    private val _deleteBookState = MutableLiveData<Resource<Unit>>()
    val deleteBookState: LiveData<Resource<Unit>> = _deleteBookState

    private val _apiSearchState = MutableLiveData<Resource<List<Book>>>()
    val apiSearchState: LiveData<Resource<List<Book>>> = _apiSearchState

    private val _selectedBook = MutableLiveData<Book?>()
    val selectedBook: LiveData<Book?> = _selectedBook

    private val _refreshState = MutableLiveData<Resource<List<Book>>>()
    val refreshState: LiveData<Resource<List<Book>>> = _refreshState

    init {
        refreshBooks()
    }

    fun refreshBooks() {
        viewModelScope.launch {
            _refreshState.value = Resource.Loading()
            _refreshState.value = bookRepository.fetchAllBooksFromFirestore()
        }
    }

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            val book = bookRepository.getBookById(bookId)
            _selectedBook.value = book
        }
    }

    fun addBook(book: Book) {
        val uid = authRepository.currentUser?.uid ?: return
        val userName = authRepository.currentUser?.displayName ?: "Usuario"
        val bookWithOwner = book.copy(ownerUid = uid, ownerName = userName)

        viewModelScope.launch {
            _addBookState.value = Resource.Loading()
            _addBookState.value = bookRepository.addBook(bookWithOwner)
        }
    }

    fun addBookWithCover(book: Book) {
        val uid = authRepository.currentUser?.uid ?: return
        val userName = authRepository.currentUser?.displayName ?: "Usuario"

        viewModelScope.launch {
            _addBookState.value = Resource.Loading()

            val resolvedCoverUrl = if (book.coverUrl.isNotBlank()) {
                book.coverUrl
            } else {
                bookRepository.resolveCoverUrl(book.isbn, book.title, book.author)
            }

            val bookWithOwner = book.copy(
                ownerUid = uid,
                ownerName = userName,
                coverUrl = resolvedCoverUrl
            )
            _addBookState.value = bookRepository.addBook(bookWithOwner)
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            _deleteBookState.value = Resource.Loading()
            _deleteBookState.value = bookRepository.deleteBook(bookId)
        }
    }

    fun selectBook(book: Book) {
        _selectedBook.value = book
    }

    fun searchBooksFromApi(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _apiSearchState.value = Resource.Loading()
            _apiSearchState.value = bookRepository.searchBooksFromApi(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getCurrentUserId(): String? = authRepository.currentUser?.uid
}
