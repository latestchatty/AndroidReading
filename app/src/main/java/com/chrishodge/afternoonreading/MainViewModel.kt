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
    init {
        // Load hidden IDs when ViewModel is created
        // refreshHiddenIds()
    }

    private val _threadsData = MutableStateFlow<List<Thread>>(emptyList())
    val threadsData = _threadsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _guildId = mutableStateOf(preferencesManager.getString("guild_id"))
    val guildId: State<String> = _guildId

    private val _forumId = mutableStateOf(preferencesManager.getString("forum_id"))
    val forumId: State<String> = _forumId

    private val _hiddenIds: MutableStateFlow<Set<String>> = MutableStateFlow(preferencesManager.getStringSet("hidden_ids"))
    val hiddenIds = _hiddenIds.asStateFlow()

    //private val _hiddenIds = MutableStateFlow(preferencesManager.getStringSet("hidden_ids"))
    //val hiddenIds = _hiddenIds.asStateFlow()

    //private val _hiddenIds = MutableStateFlow<Set<String>>(emptySet())
    //val hiddenIds = _hiddenIds.asStateFlow()

    // private val _hiddenIds = mutableStateOf(preferencesManager.getStringSet("hidden_ids"))
    // val hiddenIds: State<Set<String>> = _hiddenIds

    private val _channelId = mutableStateOf("0")
    val channelId: State<String> = _channelId

    private val _channelName = mutableStateOf("")
    val channelName: State<String> = _channelName

    private val _channelOP = mutableStateOf<Message?>(null)
    val channelOp: State<Message?> = _channelOP

    fun clearHiddenIds() {
        _hiddenIds.value = emptySet()
        preferencesManager.saveStringSet("hidden_ids", emptySet())
    }

    fun addHiddenId(id: String) {
        // Get current set and add new ID
        val currentIds = _hiddenIds.value.toMutableSet()
        currentIds.add(id)

        // Save to preferences
        preferencesManager.saveStringSet("hidden_ids", currentIds)

        // Update state
        _hiddenIds.value = currentIds
    }

    fun refreshHiddenIds() {
        _hiddenIds.value = preferencesManager.getStringSet("hidden_ids")
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

    fun setChannel(thread: Thread?) {
        _channelId.value = thread?.id ?: "0"
        _channelName.value = thread?.name ?: ""
        _channelOP.value = thread?.firstPost
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
