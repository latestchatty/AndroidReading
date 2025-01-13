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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.MaterialRichText
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    mainViewModel: MainViewModel
) {
    val messageViewModel = mainViewModel.messageViewModel.collectAsState().value
    val channelName = mainViewModel.channelName.value
    val channelId = mainViewModel.channelId.value
    val channelOp = mainViewModel.channelOp.value
    var messageId by remember { mutableStateOf("0") }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    val messageContentScrollState = rememberScrollState()
    val messageListScrollState = rememberScrollState()

    LaunchedEffect(channelOp) {
        messageId = channelId
        selectedMessage = channelOp as? Message
    }

    fun formatDate(timestamp: String): String {
        return try {
            // Parse the ISO 8601 timestamp
            val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US)
            inputSdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputSdf.parse(timestamp)

            // Format the date in desired format
            val outputSdf = SimpleDateFormat("MM-dd-yyyy HH:mm a", Locale.US)
            outputSdf.timeZone = TimeZone.getDefault() // Or keep UTC if needed: TimeZone.getTimeZone("UTC")

            return outputSdf.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "$channelName ",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.bodyMedium)},
            navigationIcon = {
                IconButton(
                    onClick = {
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
                        .verticalScroll(messageContentScrollState)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column() {
                        Spacer(modifier = Modifier.height(60.dp))
                        Row( modifier = Modifier.padding(bottom = 8.dp)) {
                            selectedMessage?.author?.let {
                                Text(
                                    text = it.globalName ?: it.username,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.orange),
                                    textAlign = TextAlign.Left,
                                    maxLines = 1
                                )
                            }
                            if (selectedMessage?.author?.globalName?.isBlank() == true || selectedMessage?.author?.username?.isBlank() == true) {
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
                            Spacer(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth())
                            Text(
                                text = "${selectedMessage?.timestamp?.let { formatDate(it) }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray.copy(0.75f),
                                textAlign = TextAlign.Right,
                                maxLines = 1
                            )
                        }

                        Row( modifier = Modifier.padding(bottom = 8.dp)) {
                            SimpleMarkdownText(markdown = channelName, modifier = Modifier.fillMaxWidth())
                        }

                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            EnhancedMarkdownText(
                                markdown = selectedMessage?.content ?: "",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.25f))
                        .height(44.dp)
                        .padding(8.dp)
                ) {
                    Column() {
                        Row( modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                            IconButton(onClick = {

                            }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            /*
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
                            */
                            Spacer(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth())
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
                    contentAlignment = Alignment.TopStart
                ){
                    Column(modifier = Modifier.verticalScroll(messageListScrollState)) {
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

// Basic implementation using compose-richtext library
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    MaterialRichText(modifier = modifier) {
        Markdown(content = markdown)
    }
}

// Example usage
@Composable
fun MarkdownPreview() {
    val markdownContent = """
        # Hello Markdown
        
        This is a **bold** text and *italic* text.
        
        ## List Example
        - Item 1
        - Item 2
        
        [Click here](https://example.com)
    """.trimIndent()

    MarkdownText(
        markdown = markdownContent,
        modifier = Modifier.padding(16.dp)
    )
}

// Simple custom implementation without external dependencies
@Composable
fun SimpleMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val text = buildAnnotatedString {
        // Bold text
        val boldPattern = """(\*\*|__)(.*?)\1""".toRegex()
        var lastIndex = 0

        boldPattern.findAll(markdown).forEach { match ->
            // Add text before the bold pattern
            append(markdown.substring(lastIndex, match.range.first))

            // Add the bold text
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[2])
            }

            lastIndex = match.range.last + 1
        }

        // Add remaining text
        if (lastIndex < markdown.length) {
            append(markdown.substring(lastIndex))
        }
    }

    Text(
        text = text,
        modifier = modifier
    )
}

@Composable
fun ClickableLinksText(
    text: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    // Regular expression to match URLs
    val urlRegex = """(?:https?://)[^\s]+""".toRegex()

    // Find all URLs in the text
    val urlRanges = urlRegex.findAll(text)

    // Build annotated string with clickable links
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0

        append(text)

        // Add URL annotations and styling
        urlRanges.forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val url = matchResult.value

            // Add URL annotation
            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end
            )

            // Add URL styling
            addStyle(
                style = SpanStyle(
                    color = Color(0xFFA459D6),
                    //color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
        }
    }

    // Create clickable text
    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier,
        onClick = { offset ->
            // Find which URL was clicked (if any)
            annotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                // Open URL in browser
                uriHandler.openUri(annotation.item)
            }
        }
    )
}

// You can replace the SimpleMarkdownText in your MessagesScreen with this:
@Composable
fun EnhancedMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    // First handle markdown
    val text = buildAnnotatedString {
        // Bold text
        val boldPattern = """(\*\*|__)(.*?)\1""".toRegex()
        var lastIndex = 0

        boldPattern.findAll(markdown).forEach { match ->
            // Add text before the bold pattern
            append(markdown.substring(lastIndex, match.range.first))

            // Add the bold text
            withStyle(SpanStyle( fontWeight = FontWeight.Bold)) {
                append(match.groupValues[2])
            }

            lastIndex = match.range.last + 1
        }

        // Add remaining text
        if (lastIndex < markdown.length) {
            append(markdown.substring(lastIndex))
        }
    }

    // Then handle clickable links
    ClickableLinksText(
        text = text.toString(),
        modifier = modifier
    )
}