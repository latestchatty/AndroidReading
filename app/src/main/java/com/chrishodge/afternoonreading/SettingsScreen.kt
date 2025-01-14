package com.chrishodge.afternoonreading.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.chrishodge.afternoonreading.MainViewModel
import com.chrishodge.afternoonreading.SharedForm

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .wrapContentSize(Alignment.Center)
    ) {
        SharedForm(
            mainViewModel = viewModel,
            onSubmit = { formState ->
                Log.d("Form", "Guild ID: ${formState.guildId}, Forum ID: ${formState.forumId}")

            }
        )
    }
}

