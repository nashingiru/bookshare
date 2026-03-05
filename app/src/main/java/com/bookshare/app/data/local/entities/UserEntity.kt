package com.bookshare.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String = "",
    val bio: String = "",
    val booksShared: Int = 0,
    val booksExchanged: Int = 0
)
