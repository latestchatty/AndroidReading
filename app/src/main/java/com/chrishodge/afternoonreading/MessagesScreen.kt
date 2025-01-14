package com.chrishodge.afternoonreading

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.chrishodge.afternoonreading.ui.theme.replylines
import com.chrishodge.afternoonreading.ui.theme.tags
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
    var channelOp = mainViewModel.channelOp.value
    var messageId by remember { mutableStateOf("0") }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    val messageContentScrollState = rememberScrollState()
    val messageListScrollState = rememberScrollState()

    var messages = messageViewModel?.messages?.value ?: emptyList()

    LaunchedEffect(channelOp) {
        messageId = channelId
        if (channelOp == null) {
            channelOp = messageViewModel?.fetchOp(messageId, messageId)
        }
        selectedMessage = channelOp as? Message
        messageViewModel?.fetchMessages(channelId, 100, channelOp)
    }

    DisposableEffect(Unit) {
        onDispose {
            messages = emptyList()
            messageViewModel?.clearMessages()
        }
    }

    fun formatDate(timestamp: String): String {
        return try {
            // Parse the ISO 8601 timestamp
            val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US)
            inputSdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputSdf.parse(timestamp)

            // Format the date in desired format
            val outputSdf = SimpleDateFormat("MM-dd-yyyy hh:mm a", Locale.US)
            outputSdf.timeZone = TimeZone.getDefault() // Or keep UTC if needed: TimeZone.getTimeZone("UTC")

            return outputSdf.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$channelName ",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        mainViewModel.setChannel(thread = null)
                        messages = emptyList()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
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
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = selectedMessage?.timestamp?.let { formatDate(it) } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray.copy(0.75f),
                            textAlign = TextAlign.Right,
                            maxLines = 1
                        )
                    }

                    SimpleMarkdownText(
                        markdown = channelName,
                        modifier = Modifier.fillMaxWidth()
                    )

                    EnhancedMarkdownText(
                        markdown = selectedMessage?.content ?: "",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Action buttons section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.25f))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (messageViewModel?.isLoading?.value == true) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(start = 6.dp)
                        )
                    } else {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { }) {
                        Image(
                            painterResource(R.drawable.ic_tag_white_24dp),
                            contentDescription = "Tag",
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier.fillMaxHeight(),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }

                    IconButton(onClick = { }) {
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

            // Messages list section
            ThreadedMessageList(
                messages = messages,
                modifier = Modifier.weight(0.6f)
            )
        }
    }
}

@Composable
private fun ThreadedMessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    // Create a map for threaded messages
    val threadMap = remember(messages) {
        messages.groupBy { it.messageReference?.messageId }
    }

    // Helper function to recursively build thread
    fun buildThreadItems(
        parentId: String?,
        indent: Int,
        processedIds: MutableSet<String>
    ): List<Pair<Message, Int>> {
        return threadMap[parentId]?.flatMap { message ->
            if (processedIds.add(message.id)) {
                listOf(message to indent) +
                        buildThreadItems(message.id, indent + 1, processedIds)
            } else emptyList()
        } ?: emptyList()
    }

    // Start with top-level messages (no references)
    val processedIds = mutableSetOf<String>()
    val threadedMessages = messages
        .filter { it.messageReference == null }
        .flatMap { message ->
            if (processedIds.add(message.id)) {
                listOf(message to 0) +
                        buildThreadItems(message.id, 1, processedIds)
            } else emptyList()
        }

    LazyColumn(
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        items(
            items = threadedMessages,
            key = { (message, _) -> message.id }
        ) { (message, indent) ->
            ThreadedMessage(
                message = message,
                indent = indent,
                onMessageClick = { /* Handle message click if needed */ }
            )
        }
    }
}

// First, let's create a composable for threaded messages
@Composable
fun ThreadedMessage(
    message: Message,
    indent: Int = 0,
    onMessageClick: (Message) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (indent * 16).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (indent > 0) {
                Text(
                    "A",
                    fontFamily = replylines,
                    fontSize = 16.sp,
                    color = Color.LightGray
                )
            }
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Left,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = message.author.globalName ?: message.author.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.orange),
                textAlign = TextAlign.Right,
                maxLines = 1
            )
            Text("A", fontFamily = tags, fontSize = 10.sp, color = Color.Red)
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