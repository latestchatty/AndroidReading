package com.chrishodge.afternoonreading

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun ThreadsScreen(viewModel: ThreadsViewModel, mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    fun refresh() = refreshScope.launch {
        refreshing = true
        viewModel.refresh()
        refreshing = false
    }
    val refreshingState = rememberPullRefreshState(refreshing, ::refresh)
    val userToken by mainViewModel.userToken.collectAsState(initial = "")

    LaunchedEffect(userToken) {
        println("Current user token: '$userToken'")
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                actions = {
                    if (userToken.isNotEmpty()) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "New Thread",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = {

                        }) {
                            Icon(
                                Icons.Default.Create,
                                contentDescription = "New Thread",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    ThreadsList(threads = state.threads, mainViewModel, viewModel = viewModel)
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
fun ThreadsList(
    threads: List<Thread>,
    mainViewModel: MainViewModel,
    viewModel: ThreadsViewModel // Add ViewModel parameter
) {
    val hiddenIds by mainViewModel.hiddenIds.collectAsState(initial = emptySet())

    LaunchedEffect(Unit) {
        mainViewModel.refreshHiddenIds()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(threads.filter { thread -> !hiddenIds.contains(thread.id) }) { thread ->
            ThreadCard(
                thread = thread,
                mainViewModel = mainViewModel,
                viewModel = viewModel
            )
        }
    }
}



@Composable
fun ThreadCard(
    thread: Thread,
    mainViewModel: MainViewModel,
    viewModel: ThreadsViewModel
) {
    val dark = isSystemInDarkTheme()
    val forceDarkMode by mainViewModel.forceDarkMode.collectAsState()
    val nickname by mainViewModel.nickname.collectAsState("")

    // Load thread data when card becomes visible
    LaunchedEffect(thread.id) {
        // Queue this thread for loading when it appears
        viewModel.queueThreadLoad(thread.id)
    }

    Card(
        onClick = {
            mainViewModel.setChannel(thread = thread)
            println("Thread ${thread.id} clicked")
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
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                thread.username?.let { username ->
                    thread.author?.let { author ->
                        Text(
                            text = author,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (nickname.isNotBlank() && nickname.lowercase() == username.lowercase())
                                colorResource(id = R.color.blue)
                            else colorResource(id = R.color.orange),
                            textAlign = TextAlign.Left,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (thread.author?.isBlank() == true) {
                    Box(modifier = Modifier
                        .background(
                            if (dark || forceDarkMode) colorResource(id = R.color.redacted_orange_dark)
                            else colorResource(id = R.color.redacted_orange_light)
                        )
                    ) {
                        Text(
                            text = "RedactedAuthor",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (dark || forceDarkMode) colorResource(id = R.color.redacted_orange_dark)
                            else colorResource(id = R.color.redacted_orange_light),
                            textAlign = TextAlign.Left,
                            maxLines = 1
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${thread.threadMetadata.createTimestamp?.let { formatDate(it) }} (${thread.messageCount})",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                thread.firstPost?.let {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = it.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (thread.firstPost == null) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (dark || forceDarkMode) colorResource(id = R.color.redacted_gray_dark)
                                else colorResource(id = R.color.redacted_gray_light)
                            ),
                        text = "Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts and visual mockups.",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (dark || forceDarkMode) colorResource(id = R.color.redacted_gray_dark)
                        else colorResource(id = R.color.redacted_gray_light)
                    )
                }
                MinimalDropdownMenu(
                    thread = thread,
                    mainViewModel = mainViewModel
                )
            }
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
fun MinimalDropdownMenu(thread: Thread, mainViewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showHideDialog) {
        AlertDialog(
            onDismissRequest = { showHideDialog = false },
            title = { Text("Hide Thread") },
            text = { Text("Are you sure you want to hide this thread?") },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.addHiddenId(thread.id)
                    showHideDialog = false
                    expanded = false
                }) {
                    Text("Hide")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report") },
            text = { Text("Are you sure you want to report this content?") },
            confirmButton = {
                TextButton(onClick = {
                    // mainViewModel.reportThread(thread.id)
                    Toast.makeText(context, "Content Reported!", Toast.LENGTH_SHORT).show()
                    showReportDialog = false
                    expanded = false
                }) {
                    Text("Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    IconButton(onClick = { expanded = !expanded }) {
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
            onClick = {
                showHideDialog = true
//                mainViewModel.addHiddenId(id = thread.id)
//                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("Report", color = MaterialTheme.colorScheme.primary) },
            onClick = {
                showReportDialog = true
            }
        )
    }
}