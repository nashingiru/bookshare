package com.bookshare.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bookshare.app.data.local.entities.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByUid(uid: String): LiveData<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUidSync(uid: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUserByUid(uid: String)
}
