package com.chrishodge.afternoonreading.ui

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.chrishodge.afternoonreading.BuildConfig
import com.chrishodge.afternoonreading.MainViewModel
import com.chrishodge.afternoonreading.MessagesScreen
import com.chrishodge.afternoonreading.ThreadsClient
import com.chrishodge.afternoonreading.ThreadsScreen
import com.chrishodge.afternoonreading.ThreadsViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val apiKey = BuildConfig.API_KEY
    println("API Key: $apiKey")

    val threadsClient = ThreadsClient("$apiKey")
    val guildId = viewModel.guildId.value
    val channelId = viewModel.channelId.value
    val forumId = viewModel.forumId.value
    val threadViewModel = rememberThreadsViewModel(threadsClient, guildId, forumId)
    val messageViewModel = viewModel.messageViewModel.collectAsState().value
    val showMessageScreen = viewModel.showMessageScreen.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

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
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
            val dismissThreshold = 100f
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX.value > dismissThreshold) {
                                    coroutineScope.launch {
                                        offsetX.animateTo(
                                            with(density) { size.width.toFloat() },
                                            animationSpec = tween(300)
                                        )
                                        viewModel.navigateToThreadsScreen()
                                    }
                                } else {
                                    coroutineScope.launch {
                                        offsetX.animateTo(0f, spring())
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, spring())
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                coroutineScope.launch {
                                    val newValue = (offsetX.value + dragAmount).coerceAtLeast(0f)
                                    offsetX.snapTo(newValue)
                                }
                                change.consume()
                            }
                        )
                    }
            ) {
                MessagesScreen(mainViewModel = viewModel)
            }
        }
    }
}

// Create a remember helper for ThreadsViewModel
@Composable
private fun rememberThreadsViewModel(
    threadsClient: ThreadsClient,
    guildId: String,
    channelId: String
): ThreadsViewModel {
    return remember(guildId) {
        ThreadsViewModel(
            threadsClient,
            "https://canary.discord.com/api/v9/guilds/$guildId/threads/active",
            channelId
        )
    }
}