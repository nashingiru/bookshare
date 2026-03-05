package com.bookshare.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bookshare.app.data.local.dao.BookDao
import com.bookshare.app.data.local.dao.UserDao
import com.bookshare.app.data.local.entities.BookEntity
import com.bookshare.app.data.local.entities.UserEntity

@Database(
    entities = [BookEntity::class, UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BookShareDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao
}
