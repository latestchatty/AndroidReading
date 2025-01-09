package com.chrishodge.afternoonreading.ui

//noinspection UsingMaterialAndMaterial3Libraries
import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.chrishodge.afternoonreading.BuildConfig
import com.chrishodge.afternoonreading.MainViewModel
import com.chrishodge.afternoonreading.MessageScreen
import com.chrishodge.afternoonreading.SharedForm
import com.chrishodge.afternoonreading.ThreadsClient
import com.chrishodge.afternoonreading.ThreadsScreen
import com.chrishodge.afternoonreading.ThreadsViewModel

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val apiKey = BuildConfig.API_KEY
    println("API Key: $apiKey")

    val threadsClient = ThreadsClient("$apiKey")
    val guildId = viewModel.guildId.value
    val threadViewModel = rememberThreadsViewModel(threadsClient, guildId)
    val messageViewModel = viewModel.messageViewModel.collectAsState().value
    val showMessageScreen = viewModel.showMessageScreen.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.createMessageViewModel()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Keep ThreadsScreen always mounted but control visibility
        ThreadsScreen(
            viewModel = threadViewModel,
            mainViewModel = viewModel,
            modifier = Modifier.alpha(if (showMessageScreen) 0f else 1f)
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = viewModel.channelId.value != "0",
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })
        ) {
            MessageScreen(mainViewModel = viewModel)
        }
    }
}

@Composable
fun AccountScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Account",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .wrapContentSize(Alignment.Center)
    ) {
        SharedForm { formState ->
            // Handle form submission
            Log.d("Form", "Guild ID: ${formState.guildId}, Forum ID: ${formState.forumId}")
        }
    }
}

// Create a remember helper for ThreadsViewModel
@Composable
private fun rememberThreadsViewModel(
    threadsClient: ThreadsClient,
    guildId: String
): ThreadsViewModel {
    return remember(guildId) {
        ThreadsViewModel(
            threadsClient,
            "https://canary.discord.com/api/v9/guilds/$guildId/threads/active"
        )
    }
}