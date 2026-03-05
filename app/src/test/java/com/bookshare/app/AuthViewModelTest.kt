package com.bookshare.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bookshare.app.model.Resource
import com.bookshare.app.model.User
import com.bookshare.app.repository.AuthRepository
import com.bookshare.app.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials calls repository and updates state`() = runTest {
        // Given
        val mockUser = User(uid = "uid123", name = "Test User", email = "test@email.com")
        whenever(authRepository.login("test@email.com", "password123"))
            .thenReturn(Resource.Success(mockUser))

        // When
        viewModel.login("test@email.com", "password123")

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is Resource.Success)
        assertEquals("uid123", (state as Resource.Success).data.uid)
    }

    @Test
    fun `login with empty email shows error`() {
        // When
        viewModel.login("", "password123")

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("El correo no puede estar vacío", (state as Resource.Error).message)
    }

    @Test
    fun `login with invalid email shows error`() {
        viewModel.login("notanemail", "password123")
        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("Ingresa un correo válido", (state as Resource.Error).message)
    }

    @Test
    fun `login with short password shows error`() {
        viewModel.login("valid@email.com", "12345")
        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("La contraseña debe tener al menos 6 caracteres", (state as Resource.Error).message)
    }

    @Test
    fun `register with empty name shows error`() {
        viewModel.register("", "email@test.com", "password123")
        val state = viewModel.registerState.value
        assertTrue(state is Resource.Error)
        assertEquals("El nombre no puede estar vacío", (state as Resource.Error).message)
    }

    @Test
    fun `register with valid data calls repository`() = runTest {
        // Given
        val mockUser = User(uid = "uid1", name = "Juan", email = "juan@test.com")
        whenever(authRepository.register("Juan", "juan@test.com", "password123"))
            .thenReturn(Resource.Success(mockUser))

        // When
        viewModel.register("Juan", "juan@test.com", "password123")

        // Then
        val state = viewModel.registerState.value
        assertTrue(state is Resource.Success)
    }

    @Test
    fun `isLoggedIn delegates to repository`() {
        whenever(authRepository.isLoggedIn()).thenReturn(true)
        assertTrue(viewModel.isLoggedIn())
        whenever(authRepository.isLoggedIn()).thenReturn(false)
        assertFalse(viewModel.isLoggedIn())
    }
}
