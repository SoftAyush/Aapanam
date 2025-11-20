package org.example.aapanam.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.aapanam.repository.Repository


sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}

class SettingsViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState = _syncState.asStateFlow()

    fun syncAllData() {
        coroutineScope.launch {
            _syncState.value = SyncState.Loading
            try {
                repository.syncAllUnsynced()
                _syncState.value = SyncState.Success
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}
