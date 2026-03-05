package com.bookshare.app.repository

import com.bookshare.app.data.local.dao.UserDao
import com.bookshare.app.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {

    val currentUser get() = firebaseAuth.currentUser

    suspend fun register(name: String, email: String, password: String): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user!!
                val user = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email
                )
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                userDao.insertUser(user.toEntity())
                Resource.Success(user)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al registrar usuario")
            }
        }

    suspend fun login(email: String, password: String): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user!!
                val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = doc.toObject(User::class.java)?.copy(uid = firebaseUser.uid)
                    ?: User(uid = firebaseUser.uid, name = "", email = email)
                userDao.insertUser(user.toEntity())
                Resource.Success(user)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al iniciar sesión")
            }
        }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    suspend fun getCurrentUserProfile(): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val uid = firebaseAuth.currentUser?.uid ?: return@withContext Resource.Error("No autenticado")
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)?.copy(uid = uid)
                ?: return@withContext Resource.Error("Usuario no encontrado")
            userDao.insertUser(user.toEntity())
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener perfil")
        }
    }

    suspend fun updateProfile(user: User): Resource<User> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
            userDao.updateUser(user.toEntity())
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar perfil")
        }
    }
}
