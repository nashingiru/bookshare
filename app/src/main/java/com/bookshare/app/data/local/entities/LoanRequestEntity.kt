package com.bookshare.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loan_requests")
data class LoanRequestEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val bookCoverUrl: String,
    val requesterUid: String,
    val requesterName: String,
    val ownerUid: String,
    val ownerName: String,
    val message: String,
    val status: String,
    val createdAt: Long
)
