package com.chrishodge.afternoonreading

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// FormState.kt
data class FormState(
    val guildId: String = "1250110786676981872",
    val forumId: String = "1321902595287285832",
    var nickname: String = "",
    var hiddenIds: Set<String> = emptySet(),
    var userToken: String = "",
    val isValid: Boolean = false
)

// FormViewModel.kt
class FormViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    private val _hiddenIds = MutableStateFlow(preferencesManager.getStringSet("hidden_ids"))
    val hiddenIds = _hiddenIds.asStateFlow()
    private val _formState = MutableStateFlow(FormState(
        guildId = preferencesManager.getString("guild_id"),
        forumId = preferencesManager.getString("forum_id"),
        nickname = preferencesManager.getString("nickname"),
        hiddenIds = _hiddenIds.value,
        userToken = preferencesManager.getString("user_token"),
    ))
    val formState = _formState.asStateFlow()

    fun updateGuildId(guildId: String) {
        _formState.update { currentState ->
            currentState.copy(
                guildId = guildId,
                isValid = validateForm(guildId, currentState.forumId)
            )
        }
        preferencesManager.saveString("guild_id", guildId)
    }

    fun updateForumId(forumId: String) {
        _formState.update { currentState ->
            currentState.copy(
                forumId = forumId,
                isValid = validateForm(currentState.guildId, forumId)
            )
        }
        preferencesManager.saveString("forum_id", forumId)
    }

    fun updateNickname(nickname: String) {
        _formState.update { currentState ->
            currentState.copy(
                nickname = nickname,
                isValid = validateForm(currentState.nickname, nickname)
            )
        }
        preferencesManager.saveString("nickname", nickname)
    }

    fun updateUserToken(token: String) {
        _formState.update { currentState ->
            currentState.copy(
                userToken = token,
                isValid = validateForm(currentState.userToken, token)
            )
        }
        preferencesManager.saveString("user_token", token)
    }

    fun clearHidden() {
        _formState.update { currentState ->
            currentState.copy(
                hiddenIds = emptySet()
            )
        }
        preferencesManager.saveStringSet("hidden_ids", emptySet())
    }

    fun updateHiddenIds() {
        val newHiddenIds = preferencesManager.getStringSet("hidden_ids")
        _hiddenIds.value = newHiddenIds
        _formState.update { it.copy(hiddenIds = newHiddenIds) }
    }

    // Optional: Add a function to be called when the form becomes active
    fun refresh() {
        updateHiddenIds()
    }

    private fun validateForm(guildId: String, forumId: String): Boolean {
        return guildId.isNotBlank() && forumId.isNotBlank()
    }

    companion object {
        private var instance: FormViewModel? = null

        fun getInstance(owner: ViewModelStoreOwner, preferencesManager: PreferencesManager): FormViewModel {
            return instance ?: ViewModelProvider(
                owner,
                FormViewModelFactory(preferencesManager)
            )[FormViewModel::class.java].also {
                instance = it
            }
        }
    }
}

// SharedForm.kt
@Composable
fun SharedForm(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    onSubmit: (FormState) -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val preferencesManager = remember { PreferencesManager(context) }
    val viewModel = remember { FormViewModel.getInstance(activity, preferencesManager) }
    val formState by viewModel.formState.collectAsState()
    val hiddenIds by viewModel.hiddenIds.collectAsState()

    val hiddenCount = remember(hiddenIds) {
        hiddenIds.count()
    }

    LaunchedEffect(Unit) {
        viewModel.updateHiddenIds()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = formState.guildId,
            onValueChange = { viewModel.updateGuildId(it) },
            label = { Text("Guild ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = formState.forumId,
            onValueChange = { viewModel.updateForumId(it) },
            label = { Text("Forum ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = formState.nickname,
            onValueChange = { viewModel.updateNickname(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = formState.userToken,
            onValueChange = { viewModel.updateUserToken(it) },
            label = { Text("User Token") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                onSubmit(formState)
                mainViewModel.updateUserToken(formState.userToken)
                Toast.makeText(context, "Settings Saved", Toast.LENGTH_SHORT).show()
                      },
            enabled = true, // formState.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Button(
            onClick = {
                viewModel.clearHidden()
                Toast.makeText(context, "Hidden Cleared", Toast.LENGTH_SHORT).show()
            },
            enabled = hiddenCount > 0,  // Only enable if there are hidden items
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear $hiddenCount Hidden")
        }
    }
}

// Add a Factory for FormViewModel
class FormViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FormViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}