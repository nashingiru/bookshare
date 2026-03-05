package com.bookshare.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bookshare.app.data.local.entities.LoanRequestEntity

@Dao
interface LoanRequestDao {

    // Solicitudes que YO envié (como solicitante)
    @Query("SELECT * FROM loan_requests WHERE requesterUid = :uid ORDER BY createdAt DESC")
    fun getMyRequests(uid: String): LiveData<List<LoanRequestEntity>>

    // Solicitudes que recibí (como dueño del libro)
    @Query("SELECT * FROM loan_requests WHERE ownerUid = :uid ORDER BY createdAt DESC")
    fun getReceivedRequests(uid: String): LiveData<List<LoanRequestEntity>>

    // Solicitudes pendientes recibidas (para badge/notificación)
    @Query("SELECT COUNT(*) FROM loan_requests WHERE ownerUid = :uid AND status = 'pending'")
    fun getPendingReceivedCount(uid: String): LiveData<Int>

    @Query("SELECT * FROM loan_requests WHERE id = :id")
    suspend fun getById(id: String): LoanRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: LoanRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<LoanRequestEntity>)

    @Query("UPDATE loan_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM loan_requests WHERE id = :id")
    suspend fun deleteById(id: String)
}
