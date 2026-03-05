package com.bookshare.app.model

import com.bookshare.app.data.local.entities.BookEntity
import com.bookshare.app.data.local.entities.LoanRequestEntity
import com.bookshare.app.data.local.entities.UserEntity
import com.bookshare.app.data.remote.dto.BookSearchDoc

fun BookEntity.toDomain(): Book = Book(
    id = id, title = title, author = author, description = description,
    genre = genre, coverUrl = coverUrl, ownerUid = ownerUid,
    ownerName = ownerName, isAvailable = isAvailable, createdAt = createdAt,
    isbn = isbn, pageCount = pageCount, language = language
)

fun Book.toEntity(): BookEntity = BookEntity(
    id = id, title = title, author = author, description = description,
    genre = genre, coverUrl = coverUrl, ownerUid = ownerUid,
    ownerName = ownerName, isAvailable = isAvailable, createdAt = createdAt,
    isbn = isbn, pageCount = pageCount, language = language
)

fun UserEntity.toDomain(): User = User(
    uid = uid, name = name, email = email, photoUrl = photoUrl,
    bio = bio, booksShared = booksShared, booksExchanged = booksExchanged
)

fun User.toEntity(): UserEntity = UserEntity(
    uid = uid, name = name, email = email, photoUrl = photoUrl,
    bio = bio, booksShared = booksShared, booksExchanged = booksExchanged
)

fun LoanRequestEntity.toDomain(): LoanRequest = LoanRequest(
    id = id, bookId = bookId, bookTitle = bookTitle, bookCoverUrl = bookCoverUrl,
    requesterUid = requesterUid, requesterName = requesterName,
    ownerUid = ownerUid, ownerName = ownerName, message = message,
    status = status, createdAt = createdAt
)

fun LoanRequest.toEntity(): LoanRequestEntity = LoanRequestEntity(
    id = id, bookId = bookId, bookTitle = bookTitle, bookCoverUrl = bookCoverUrl,
    requesterUid = requesterUid, requesterName = requesterName,
    ownerUid = ownerUid, ownerName = ownerName, message = message,
    status = status, createdAt = createdAt
)

fun BookSearchDoc.toDomain(ownerUid: String = "", ownerName: String = ""): Book = Book(
    id = key.replace("/works/", ""),
    title = title,
    author = getAuthor(),
    description = "",
    genre = BookGenre.OTHER.displayName,
    coverUrl = getCoverUrl(),
    ownerUid = ownerUid,
    ownerName = ownerName,
    isbn = getIsbn(),
    pageCount = pageCount ?: 0
)
