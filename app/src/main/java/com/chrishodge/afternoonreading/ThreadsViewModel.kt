package com.chrishodge.afternoonreading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
    private val apiUrl: String,
    private val forumId: String
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
                println("Attempting to fetch from URL: $apiUrl")

                // First get threads and show them immediately
                val response = threadsClient.getThreads(apiUrl)
                val initialThreads = response.threads.filter { it.parentId == forumId }.sortedByDescending { it.lastMessageId?.toLongOrNull() ?: 0L }
                _uiState.value = ThreadsUiState.Success(initialThreads)
                // Then update authors one by one
                val processedThreads = initialThreads.toMutableList()
                initialThreads.forEachIndexed { index, thread ->
                    try {
                        if (index < 30) {
                            delay(125) // 125ms delay between requests
                            val messageResponse = threadsClient.getMessage(
                                url = "https://canary.discord.com/api/v9/channels/${thread.id}/messages/${thread.id}"
                            )
                            // Update the thread with author info
                            processedThreads[index] = thread.copy(
                                author = (messageResponse.author?.globalName
                                    ?: messageResponse.author?.username).toString(),
                                username = messageResponse.author?.username,
                                firstPost = messageResponse
                            )
                            // Emit updated list after each thread is processed
                            _uiState.value = ThreadsUiState.Success(processedThreads.toList())
                        } else {
                            // Update the thread with author info (nothing in this case)
                            processedThreads[index] = thread.copy()
                            // Emit updated list after each thread is processed
                            _uiState.value = ThreadsUiState.Success(processedThreads.toList())
                        }
                    } catch (e: Exception) {
                        println("Error fetching message for thread ${thread.id}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("Error details: ${e.message}")
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
