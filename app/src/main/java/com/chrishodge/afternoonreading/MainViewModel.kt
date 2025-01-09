package com.chrishodge.afternoonreading

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _threadsData = MutableStateFlow<List<Thread>>(emptyList())
    val threadsData = _threadsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _guildId = mutableStateOf(preferencesManager.getString("guild_id"))
    val guildId: State<String> = _guildId

    private val _channelId = mutableStateOf("0")
    val channelId: State<String> = _channelId

    fun loadThreads(guildId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Your thread loading logic here
                // _threadsData.value = result
                println("NOW FUCKING WHAT")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setChannelId(channelId: String) {
        _channelId.value = channelId
    }

    fun saveGuildId(id: String) {
        preferencesManager.saveString("guild_id", id)
        _guildId.value = id
    }

    private val _showMessageScreen = MutableStateFlow(false)
    val showMessageScreen = _showMessageScreen.asStateFlow()

    fun toggleMessageScreen() {
        _showMessageScreen.value = !_showMessageScreen.value
        if (_showMessageScreen.value && _messageViewModel.value == null) {
            createMessageViewModel()
        }
    }

    private val _messageViewModel = MutableStateFlow<MessageViewModel?>(null)
    val messageViewModel = _messageViewModel.asStateFlow()


    fun createMessageViewModel() {
        _messageViewModel.value = MessageViewModel()
    }

    fun navigateToThreadsScreen() {
        println("navigateToThreadsScreen")
        _channelId.value = "0"
    }
}

// Create a ViewModelFactory
class MainViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
