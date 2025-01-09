package com.chrishodge.afternoonreading

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ThreadsScreen(viewModel: ThreadsViewModel, mainViewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    fun refresh() = refreshScope.launch {
        refreshing = true
        viewModel.refresh()
        refreshing = false
    }
    val refreshingState = rememberPullRefreshState(refreshing, ::refresh)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .pullRefresh(refreshingState)) {
            when (val state = uiState) {
                is ThreadsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ThreadsUiState.Success -> {
                    ThreadsList(threads = state.threads, mainViewModel)
                    PullRefreshIndicator(refreshing, refreshingState, Modifier.align(Alignment.TopCenter))
                }
                is ThreadsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThreadsList(threads: List<Thread>, mainViewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(threads) { thread ->
            ThreadCard(thread = thread, mainViewModel)
        }
    }
}

@Composable
fun ThreadCard(thread: Thread, mainViewModel: MainViewModel) {
    Card(
        onClick = {
            mainViewModel.setChannelId(thread.id)
            println("${thread.id}")
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.25f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row( modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Author",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.orange),
                    textAlign = TextAlign.Left,
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f).fillMaxHeight())
                Text(
                    text = "${thread.threadMetadata.createTimestamp?.let { formatDate(it) }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray.copy(0.75f),
                    textAlign = TextAlign.Right,
                    maxLines = 1
                )
            }

            Text(
                text = thread.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Column {
                    Text(
                        text = "${thread.messageCount} Replies",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${thread.memberCount} Contributors",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.weight(1f).fillMaxHeight())
                Row {
                    Box {
                        MinimalDropdownMenu()
                    }
                }
            }
            /*
            if (!thread.appliedTags.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    thread.appliedTags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag) }
                        )
                    }
                }
            }
            */
        }
    }
}

private fun formatDate(timestamp: String): String {
    return try {
        // Parse the ISO 8601 timestamp
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)
        val now = System.currentTimeMillis()
        val ago = DateUtils.getRelativeTimeSpanString(date?.getTime() ?: now, now, DateUtils.MINUTE_IN_MILLIS)
        return ago.toString()
    } catch (e: Exception) {
        timestamp
    }
}

@Composable
fun MinimalDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material.IconButton(onClick = { expanded = !expanded }) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.primary
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        DropdownMenuItem(
            text = { Text("Hide Thread", color = MaterialTheme.colorScheme.primary) },
            onClick = { /* Do something... */ }
        )
        DropdownMenuItem(
            text = { Text("Report", color = MaterialTheme.colorScheme.primary) },
            onClick = { /* Do something... */ }
        )
    }
}