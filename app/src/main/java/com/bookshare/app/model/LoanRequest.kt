package com.bookshare.app.model

data class LoanRequest(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val bookCoverUrl: String = "",
    val requesterUid: String = "",
    val requesterName: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val message: String = "",
    val status: String = LoanStatus.PENDING.value,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LoanStatus(val value: String, val displayName: String) {
    PENDING("pending", "Pendiente"),
    ACCEPTED("accepted", "Aceptada"),
    REJECTED("rejected", "Rechazada"),
    RETURNED("returned", "Devuelto")
}
