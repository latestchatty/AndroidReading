package com.chrishodge.afternoonreading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import android.util.Log
import androidx.activity.ComponentActivity

// FormState.kt
data class FormState(
    val guildId: String = "",
    val forumId: String = "",
    val isValid: Boolean = false
)

// FormViewModel.kt
class FormViewModel : ViewModel() {
    private val _formState = MutableStateFlow(FormState())
    val formState = _formState.asStateFlow()

    fun updateGuildId(guildId: String) {
        _formState.update { currentState ->
            currentState.copy(
                guildId = guildId,
                isValid = validateForm(guildId, currentState.forumId)
            )
        }
    }

    fun updateForumId(forumId: String) {
        _formState.update { currentState ->
            currentState.copy(
                forumId = forumId,
                isValid = validateForm(currentState.guildId, forumId)
            )
        }
    }

    private fun validateForm(guildId: String, forumId: String): Boolean {
        return guildId.isNotBlank() && forumId.isNotBlank()
    }

    companion object {
        private var instance: FormViewModel? = null

        fun getInstance(owner: ViewModelStoreOwner): FormViewModel {
            return instance ?: ViewModelProvider(owner)[FormViewModel::class.java].also {
                instance = it
            }
        }
    }
}

// SharedForm.kt
@Composable
fun SharedForm(
    modifier: Modifier = Modifier,
    onSubmit: (FormState) -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val viewModel = remember { FormViewModel.getInstance(activity) }
    val formState by viewModel.formState.collectAsState()

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

        Button(
            onClick = { onSubmit(formState) },
            enabled = formState.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
    }
}
