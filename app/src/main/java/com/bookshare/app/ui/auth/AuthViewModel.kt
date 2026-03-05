package com.bookshare.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookshare.app.model.Resource
import com.bookshare.app.model.User
import com.bookshare.app.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    private val _registerState = MutableLiveData<Resource<User>>()
    val registerState: LiveData<Resource<User>> = _registerState

    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = authRepository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        if (!validateRegisterInput(name, email, password)) return
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = authRepository.register(name, email, password)
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun logout() = authRepository.logout()

    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _loginState.value = Resource.Error("El correo no puede estar vacío")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = Resource.Error("Ingresa un correo válido")
            return false
        }
        if (password.length < 6) {
            _loginState.value = Resource.Error("La contraseña debe tener al menos 6 caracteres")
            return false
        }
        return true
    }

    private fun validateRegisterInput(name: String, email: String, password: String): Boolean {
        if (name.isBlank()) {
            _registerState.value = Resource.Error("El nombre no puede estar vacío")
            return false
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = Resource.Error("Ingresa un correo válido")
            return false
        }
        if (password.length < 6) {
            _registerState.value = Resource.Error("La contraseña debe tener al menos 6 caracteres")
            return false
        }
        return true
    }
}
