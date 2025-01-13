package com.chrishodge.afternoonreading

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    mainViewModel: MainViewModel
) {
    val messageViewModel = mainViewModel.messageViewModel.collectAsState().value
    val channelName = mainViewModel.channelName.value
    val channelId = mainViewModel.channelId.value
    var messageId by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        messageId = channelId
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "$channelName",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium)},
            navigationIcon = {
                IconButton(onClick = {
                    mainViewModel.setChannel(thread = null)
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
                .fillMaxSize()
        ) {
            Column(modifier =Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column() {
                        Spacer(modifier = Modifier.height(80.dp))

                        /*
                        Row( modifier = Modifier.padding(bottom = 8.dp)) {
                            thread.author?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.orange),
                                    textAlign = TextAlign.Left,
                                    maxLines = 1
                                )
                            }
                            if (thread.author?.isBlank() == true) {
                                Box(modifier = Modifier.background(colorResource(id = R.color.redacted_orange))) {
                                    Text(
                                        text = "RedactedAuthor",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(id = R.color.redacted_orange),
                                        textAlign = TextAlign.Left,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f).fillMaxHeight())
                            Text(
                                text = "${thread.threadMetadata.createTimestamp?.let { formatDate(it) }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray.copy(0.75f),
                                textAlign = TextAlign.Right,
                                maxLines = 1
                            )
                        }
                        */


                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.25f))
                        .height(40.dp)
                        .padding(8.dp)
                ) {
                    Column() {
                        Row( modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                            IconButton(onClick = {

                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Previous",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {

                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Next",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.weight(1f).fillMaxWidth())
                            IconButton(onClick = {

                            }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {

                            }) {
                                Image(
                                    painterResource(R.drawable.ic_tag_white_24dp),
                                    contentDescription = "Reply",
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier.fillMaxHeight(),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }
                            IconButton(onClick = {

                            }) {
                                Image(
                                    painterResource(R.drawable.ic_reply_white_24dp),
                                    contentDescription = "Reply",
                                    contentScale = ContentScale.FillHeight,
                                    modifier = Modifier.fillMaxHeight(),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ){
                    Column {
                        Text(text = "Example", textAlign = TextAlign.End, color = DarkGray, fontSize = 12.sp)
                    }
                }
            }

            /*
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
            */
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
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = message.author.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.orange),
                textAlign = TextAlign.Left,
                maxLines = 1
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
            if (attachment.contentType?.startsWith("image/") == true) {
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