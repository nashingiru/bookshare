package com.bookshare.app.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.bookshare.app.data.local.dao.LoanRequestDao
import com.bookshare.app.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val loanRequestDao: LoanRequestDao,
    private val firestore: FirebaseFirestore
) {

    // Solicitudes enviadas por mí
    fun getMyRequests(uid: String): LiveData<List<LoanRequest>> =
        loanRequestDao.getMyRequests(uid).map { list -> list.map { it.toDomain() } }

    // Solicitudes recibidas (soy dueño del libro)
    fun getReceivedRequests(uid: String): LiveData<List<LoanRequest>> =
        loanRequestDao.getReceivedRequests(uid).map { list -> list.map { it.toDomain() } }

    // Badge de pendientes
    fun getPendingReceivedCount(uid: String): LiveData<Int> =
        loanRequestDao.getPendingReceivedCount(uid)

    // Enviar solicitud
    suspend fun sendRequest(
        book: com.bookshare.app.model.Book,
        requesterUid: String,
        requesterName: String,
        message: String
    ): Resource<LoanRequest> = withContext(Dispatchers.IO) {
        try {
            val request = LoanRequest(
                id = UUID.randomUUID().toString(),
                bookId = book.id,
                bookTitle = book.title,
                bookCoverUrl = book.coverUrl,
                requesterUid = requesterUid,
                requesterName = requesterName,
                ownerUid = book.ownerUid,
                ownerName = book.ownerName,
                message = message,
                status = LoanStatus.PENDING.value
            )
            // Guardar en Firestore
            firestore.collection("loan_requests")
                .document(request.id)
                .set(request)
                .await()
            // Guardar localmente
            loanRequestDao.insert(request.toEntity())
            Resource.Success(request)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar solicitud")
        }
    }

    // Aceptar solicitud
    suspend fun acceptRequest(requestId: String, bookId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("loan_requests")
                    .document(requestId)
                    .update("status", LoanStatus.ACCEPTED.value)
                    .await()
                // Marcar libro como no disponible
                firestore.collection("books")
                    .document(bookId)
                    .update("isAvailable", false)
                    .await()
                loanRequestDao.updateStatus(requestId, LoanStatus.ACCEPTED.value)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al aceptar solicitud")
            }
        }

    // Rechazar solicitud
    suspend fun rejectRequest(requestId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("loan_requests")
                    .document(requestId)
                    .update("status", LoanStatus.REJECTED.value)
                    .await()
                loanRequestDao.updateStatus(requestId, LoanStatus.REJECTED.value)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al rechazar solicitud")
            }
        }

    // Marcar como devuelto
    suspend fun markAsReturned(requestId: String, bookId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("loan_requests")
                    .document(requestId)
                    .update("status", LoanStatus.RETURNED.value)
                    .await()
                // Volver a marcar libro como disponible
                firestore.collection("books")
                    .document(bookId)
                    .update("isAvailable", true)
                    .await()
                loanRequestDao.updateStatus(requestId, LoanStatus.RETURNED.value)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al marcar como devuelto")
            }
        }

    // Sincronizar desde Firestore
    suspend fun syncRequests(uid: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            // Solicitudes enviadas
            val sentSnapshot = firestore.collection("loan_requests")
                .whereEqualTo("requesterUid", uid).get().await()
            // Solicitudes recibidas
            val receivedSnapshot = firestore.collection("loan_requests")
                .whereEqualTo("ownerUid", uid).get().await()

            val allRequests = (sentSnapshot.documents + receivedSnapshot.documents)
                .distinctBy { it.id }
                .mapNotNull { doc ->
                    doc.toObject(LoanRequest::class.java)?.copy(id = doc.id)
                }
            loanRequestDao.insertAll(allRequests.map { it.toEntity() })
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al sincronizar")
        }
    }
}
