package com.bookshare.app.ui.loans

import androidx.lifecycle.*
import com.bookshare.app.model.Book
import com.bookshare.app.model.LoanRequest
import com.bookshare.app.model.Resource
import com.bookshare.app.repository.AuthRepository
import com.bookshare.app.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid get() = authRepository.currentUser?.uid ?: ""

    val myRequests: LiveData<List<LoanRequest>> = loanRepository.getMyRequests(uid)
    val receivedRequests: LiveData<List<LoanRequest>> = loanRepository.getReceivedRequests(uid)
    val pendingCount: LiveData<Int> = loanRepository.getPendingReceivedCount(uid)

    private val _sendState = MutableLiveData<Resource<LoanRequest>?>()
    val sendState: LiveData<Resource<LoanRequest>?> = _sendState

    private val _actionState = MutableLiveData<Resource<Unit>?>()
    val actionState: LiveData<Resource<Unit>?> = _actionState

    init {
        syncRequests()
    }

    fun syncRequests() {
        viewModelScope.launch {
            loanRepository.syncRequests(uid)
        }
    }

    fun sendRequest(book: Book, message: String) {
        val requesterName = authRepository.currentUser?.displayName
            ?: authRepository.currentUser?.email
            ?: "Usuario"
        viewModelScope.launch {
            _sendState.value = Resource.Loading()
            val result = loanRepository.sendRequest(
                book = book,
                requesterUid = uid,
                requesterName = requesterName,
                message = message
            )
            _sendState.value = result
        }
    }

    fun resetSendState() {
        _sendState.value = null
    }

    fun resetActionState() {
        _actionState.value = null
    }

    fun acceptRequest(request: LoanRequest) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            _actionState.value = loanRepository.acceptRequest(request.id, request.bookId)
        }
    }

    fun rejectRequest(request: LoanRequest) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            _actionState.value = loanRepository.rejectRequest(request.id)
        }
    }

    fun markAsReturned(request: LoanRequest) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            _actionState.value = loanRepository.markAsReturned(request.id, request.bookId)
        }
    }

    fun getCurrentUserId(): String = uid
}
