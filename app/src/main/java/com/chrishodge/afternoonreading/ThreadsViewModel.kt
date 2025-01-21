package com.chrishodge.afternoonreading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections

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

    // Queue for thread loading requests
    private val threadLoadQueue = Channel<String>(Channel.UNLIMITED)

    // Set to track threads that have been processed or are queued
    private val processedOrQueuedThreads = Collections.synchronizedSet(mutableSetOf<String>())

    // Flag to track if processor is running
    private var isProcessing = false

    init {
        startQueueProcessor()
        loadThreads()
    }

    fun loadThread(threadId: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is ThreadsUiState.Success) {
                    val threads = currentState.threads.toMutableList()
                    val threadIndex = threads.indexOfFirst { it.id == threadId }

                    if (threadIndex != -1) {
                        try {
                            // Fetch the message for this thread
                            val messageResponse = threadsClient.getMessage(
                                url = "https://canary.discord.com/api/v9/channels/${threadId}/messages/${threadId}"
                            )

                            // Update the thread with author info
                            val updatedThread = threads[threadIndex].copy(
                                author = (messageResponse.author?.globalName
                                    ?: messageResponse.author?.username).toString(),
                                username = messageResponse.author?.username,
                                firstPost = messageResponse
                            )

                            threads[threadIndex] = updatedThread
                            _uiState.value = ThreadsUiState.Success(threads)
                        } catch (e: Exception) {
                            println("Error fetching message for thread $threadId: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error loading thread $threadId: ${e.message}")
            }
        }
    }

    private fun startQueueProcessor() {
        viewModelScope.launch {
            println("Starting queue processor")
            while (true) {
                try {
                    if (!isProcessing) {
                        // Get next thread ID from queue
                        val threadId = threadLoadQueue.receive()
                        isProcessing = true

                        try {
                            println("Processing single thread from queue: $threadId")
                            loadThreadInternal(threadId)
                            delay(500) // 500ms delay between requests
                        } finally {
                            isProcessing = false
                        }
                    } else {
                        delay(100) // Wait before checking again
                    }
                } catch (e: Exception) {
                    println("Error processing thread queue: ${e.message}")
                    isProcessing = false
                }
            }
        }
    }

    fun queueThreadLoad(threadId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ThreadsUiState.Success) {
                val thread = currentState.threads.find { it.id == threadId }

                // Only queue if thread exists and hasn't been processed
                if (!processedOrQueuedThreads.contains(threadId)) {
                    println("Queueing thread $threadId for loading")
                    processedOrQueuedThreads.add(threadId)
                    threadLoadQueue.send(threadId)
                }
            }
        }
    }

    private suspend fun loadThreadInternal(threadId: String) {
        try {
            val currentState = _uiState.value
            if (currentState is ThreadsUiState.Success) {
                val existingThreads = currentState.threads
                val threadIndex = existingThreads.indexOfFirst { it.id == threadId }

                if (threadIndex != -1) {
                    try {
                        println("Fetching message for thread $threadId")
                        val messageResponse = threadsClient.getMessage(
                            url = "https://canary.discord.com/api/v9/channels/${threadId}/messages/${threadId}"
                        )

                        // Create a new list with the updated thread
                        val updatedThreads = existingThreads.toMutableList()
                        val author = (messageResponse.author?.globalName
                            ?: messageResponse.author?.username).toString()

                        // Update the thread
                        updatedThreads[threadIndex] = existingThreads[threadIndex].copy(
                            author = author,
                            username = messageResponse.author?.username,
                            firstPost = messageResponse
                        )

                        // Update the state
                        _uiState.value = ThreadsUiState.Success(updatedThreads)
                        println("Successfully updated thread $threadId with author $author")
                    } catch (e: Exception) {
                        println("Error fetching message for thread $threadId: ${e.message}")
                        processedOrQueuedThreads.remove(threadId) // Allow retry on failure
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading thread $threadId: ${e.message}")
            processedOrQueuedThreads.remove(threadId) // Allow retry on failure
        }
    }

    fun loadThreads() {
        viewModelScope.launch {
            try {
                _uiState.value = ThreadsUiState.Loading
                processedOrQueuedThreads.clear() // Clear tracking set on reload

                val response = threadsClient.getThreads(apiUrl)
                println("Got ${response.threads.size} threads from API")

                val initialThreads = response.threads
                    .filter { it.parentId == forumId }
                    .sortedByDescending { it.lastMessageId?.toLongOrNull() ?: 0L }
                println("Filtered to ${initialThreads.size} threads for forum $forumId")

                _uiState.value = ThreadsUiState.Success(initialThreads)
            } catch (e: Exception) {
                println("Error loading threads: ${e.message}")
                e.printStackTrace()
                _uiState.value = ThreadsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun refresh() {
        loadThreads()
    }

    override fun onCleared() {
        super.onCleared()
        // threadsClient.close()
        threadLoadQueue.close()
        isProcessing = false
    }
}
