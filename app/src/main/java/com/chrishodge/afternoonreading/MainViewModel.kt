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

    private val _forumId = mutableStateOf(preferencesManager.getString("forum_id"))
    val forumId: State<String> = _forumId

    private val _hiddenIds = mutableStateOf(preferencesManager.getStringSet("hidden_ids"))
    val hiddenIds: State<Set<String>> = _hiddenIds

    private val _channelId = mutableStateOf("0")
    val channelId: State<String> = _channelId

    private val _channelName = mutableStateOf("")
    val channelName: State<String> = _channelName

    fun clearHiddenIds() {
        _hiddenIds.value = emptySet()
        preferencesManager.saveStringSet("hidden_ids", emptySet())
    }

    fun loadThreads(guildId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // _threadsData.value = result
                println("todo...")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setChannel(channelId: String, name: String) {
        _channelId.value = channelId
        _channelName.value = name
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
        _channelName.value = ""
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
