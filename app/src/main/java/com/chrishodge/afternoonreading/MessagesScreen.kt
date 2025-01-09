package com.chrishodge.afternoonreading

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    mainViewModel: MainViewModel
) {
    var channelId by remember { mutableStateOf("") }
    var messageId by remember { mutableStateOf("") }

    val messageViewModel = mainViewModel.messageViewModel.collectAsState().value
    val channelName = mainViewModel.channelName.value

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "$channelName",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium)},
            navigationIcon = {
                IconButton(onClick = {
                    mainViewModel.setChannel("0", "")
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    }, content = {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = channelId,
                onValueChange = { channelId = it },
                label = { Text("Channel ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = messageId,
                onValueChange = { messageId = it },
                label = { Text("Message ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    messageViewModel?.fetchMessage(channelId, messageId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Message")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (messageViewModel?.isLoading?.value == true) {
                CircularProgressIndicator()
            }

            // Handle error state
            messageViewModel?.error?.value?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Handle message state
            messageViewModel?.message?.value?.let { message ->
                MessageCard(message)
            }
        }
    })
}
@Composable
fun MessageCard(message: Message) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message.author.username,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )

            if (message.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AttachmentsList(message.attachments)
            }
        }
    }
}

@Composable
fun AttachmentsList(attachments: List<Attachment>) {
    Column {
        attachments.forEach { attachment ->
            if (attachment.contentType.startsWith("image/")) {
                AsyncImage(
                    model = attachment.url,
                    contentDescription = attachment.filename,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Text(
                    text = attachment.filename,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}