package org.example.aapanam.presentation


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.aapanam.data.local.Database
import org.example.aapanam.data.remote.FirebaseService
import org.example.aapanam.repository.Repository

class AuthViewModel(private val firebaseService: FirebaseService, private val database: Database, private val repository: Repository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        coroutineScope.launch {
            val currentUser = firebaseService.getCurrentUser()
            if (currentUser != null) {
                _authState.update { AuthState.Authenticated(currentUser.email ?: "") }
                sync()
            } else {
                _authState.update { AuthState.Unauthenticated }
            }
        }
    }

    fun signIn(email: String, password: String) {
        coroutineScope.launch {
            _authState.update { AuthState.Loading }
            try {
                val user = firebaseService.signIn(email, password)
                _authState.update { AuthState.Authenticated(user.user?.email ?: "") }
                sync()
            } catch (e: Exception) {
                _authState.update { AuthState.Error(e.message ?: "An error occurred") }
            }
        }
    }

    fun signOut() {
        coroutineScope.launch {
            firebaseService.signOut()
            database.clearAllData()
            _authState.update { AuthState.Unauthenticated }
        }
    }

    private fun sync() {
        coroutineScope.launch {
            repository.syncAllUnsynced()
            repository.pull()
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val email: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
