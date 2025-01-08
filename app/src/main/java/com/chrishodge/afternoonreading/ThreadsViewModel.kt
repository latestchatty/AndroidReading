package com.chrishodge.afternoonreading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ThreadsUiState {
    data object Loading : ThreadsUiState()
    data class Success(val threads: List<Thread>) : ThreadsUiState()
    data class Error(val message: String) : ThreadsUiState()
}

class ThreadsViewModel(
    private val threadsClient: ThreadsClient,
    private val apiUrl: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<ThreadsUiState>(ThreadsUiState.Loading)
    val uiState: StateFlow<ThreadsUiState> = _uiState

    init {
        loadThreads()
    }

    private fun loadThreads() {
        viewModelScope.launch {
            try {
                _uiState.value = ThreadsUiState.Loading
                println("Attempting to fetch from URL: $apiUrl") // Debug line
                val response = threadsClient.getThreads(apiUrl)
                _uiState.value = ThreadsUiState.Success(response.threads)
            } catch (e: Exception) {
                println("Error details: ${e.message}") // Debug line
                _uiState.value = ThreadsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadThreads()
    }

    override fun onCleared() {
        super.onCleared()
        threadsClient.close()
    }
}