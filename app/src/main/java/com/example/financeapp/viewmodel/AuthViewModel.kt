package com.example.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.local.SessionManager
import com.example.financeapp.data.local.dao.UserDao
import com.example.financeapp.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val loggedInUserId: Long = SessionManager.NO_USER,
    val error: AuthError? = null
)

enum class AuthError {
    EMPTY_FIELDS,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_EXISTS,
    UNKNOWN
}

class AuthViewModel(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (sessionManager.isLoggedIn()) {
            _uiState.update {
                it.copy(isAuthenticated = true, loggedInUserId = sessionManager.getUserId())
            }
        }
    }

    fun login(email: String, password: String) {
        val error = validateLoginInputs(email, password)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val user = userDao.getByEmail(email.trim().lowercase())
            if (user == null || user.password != password) {
                _uiState.update { it.copy(isLoading = false, error = AuthError.INVALID_CREDENTIALS) }
                return@launch
            }

            sessionManager.saveUserId(user.id)
            _uiState.update {
                it.copy(isLoading = false, isAuthenticated = true, loggedInUserId = user.id)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        val error = validateRegisterInputs(name, email, password)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val normalizedEmail = email.trim().lowercase()
            if (userDao.getByEmail(normalizedEmail) != null) {
                _uiState.update { it.copy(isLoading = false, error = AuthError.EMAIL_ALREADY_EXISTS) }
                return@launch
            }

            val user = User(name = name.trim(), email = normalizedEmail, password = password)
            val id = userDao.insert(user)
            sessionManager.saveUserId(id)
            _uiState.update {
                it.copy(isLoading = false, isAuthenticated = true, loggedInUserId = id)
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.update { AuthUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateLoginInputs(email: String, password: String): AuthError? {
        if (email.isBlank() || password.isBlank()) return AuthError.EMPTY_FIELDS
        if (!isValidEmail(email)) return AuthError.INVALID_EMAIL
        return null
    }

    private fun validateRegisterInputs(name: String, email: String, password: String): AuthError? {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return AuthError.EMPTY_FIELDS
        if (!isValidEmail(email)) return AuthError.INVALID_EMAIL
        if (password.length < 6) return AuthError.WEAK_PASSWORD
        return null
    }

    private fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun factory(userDao: UserDao, sessionManager: SessionManager): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AuthViewModel(userDao, sessionManager) }
            }
    }
}
