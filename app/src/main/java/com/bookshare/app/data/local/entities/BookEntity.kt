package com.bookshare.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val genre: String,
    val coverUrl: String,
    val ownerUid: String,
    val ownerName: String,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val isbn: String = "",
    val pageCount: Int = 0,
    val language: String = "es"
)
