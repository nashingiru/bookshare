package com.bookshare.app.model

import com.google.firebase.firestore.PropertyName

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val genre: String = "",
    val coverUrl: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val isbn: String = "",
    val pageCount: Int = 0,
    val language: String = "es"
)

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val booksShared: Int = 0,
    val booksExchanged: Int = 0
)

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}

enum class BookGenre(val displayName: String) {
    FICTION("Ficción"),
    NON_FICTION("No Ficción"),
    SCIENCE("Ciencia"),
    HISTORY("Historia"),
    ROMANCE("Romance"),
    THRILLER("Thriller"),
    FANTASY("Fantasía"),
    BIOGRAPHY("Biografía"),
    SCIENCE_FICTION("Ciencia Ficción"),
    SELF_HELP("Autoayuda"),
    CHILDREN("Infantil"),
    OTHER("Otro")
}
