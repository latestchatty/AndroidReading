package com.chrishodge.afternoonreading

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
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
                        .verticalScroll(messageContentScrollState)
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

                    /*
                    // Display standard emoji names
                    selectedMessage?.reactions?.let { reactions ->
                        val standardEmojis = reactions.filter { it.emoji.id == null }
                        if (standardEmojis.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                standardEmojis.forEach { reaction ->
                                    Text(
                                        text = reaction.emoji.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    */

                    // Display supported reactions
                    selectedMessage?.reactions?.let { reactions ->
                        GroupedReactions(
                            reactions = reactions,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (selectedMessage?.id == channelId) {
                        SimpleMarkdownText(
                            markdown = channelName,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EnhancedMarkdownText(
                        markdown = selectedMessage?.content ?: "",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Display attachments in detail view
                    selectedMessage?.attachments?.let { attachments ->
                        if (attachments.isNotEmpty()) {
                            MessageAttachments(
                                attachments = attachments,
                                modifier = Modifier.fillMaxWidth(),
                                maxHeight = 300.dp  // Larger for detail view
                            )
                        }
                    }

                    // Display article embeds
                    selectedMessage?.embeds?.forEach { embed ->
                        if (embed.type == "article") {
                            ArticleEmbed(
                                embed = embed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
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
                modifier = Modifier.weight(0.6f),
                selectedMessage = selectedMessage,
                onMessageSelected = { message ->
                    selectedMessage = message
                }
            )
        }
    }
}

// ThreadMessageList
@Composable
private fun ThreadedMessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier,
    selectedMessage: Message?,
    onMessageSelected: (Message) -> Unit
) {
    var selectedMessageId by remember { mutableStateOf<String?>(null) }

    // Get the 5 most recent message IDs as an ordered list
    val recentMessageIds = remember(messages) {
        messages.sortedByDescending { it.timestamp }
            .take(5)
            .mapIndexed { index, message -> message.id to index }
            .toMap()
    }

    LaunchedEffect(messages) {
        if (selectedMessageId == null && messages.isNotEmpty()) {
            selectedMessageId = messages.first().id
            onMessageSelected(messages.first())
        }
    }

    val threadMap = remember(messages) {
        messages.groupBy { it.messageReference?.messageId }
    }

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
        verticalArrangement = Arrangement.Top
    ) {
        items(
            items = threadedMessages,
            key = { (message, _) -> message.id }
        ) { (message, indent) ->
            ThreadedMessage(
                message = message,
                indent = indent,
                isSelected = message.id == selectedMessageId,
                recentIndex = recentMessageIds[message.id],
                onMessageClick = {
                    selectedMessageId = it.id
                    onMessageSelected(it)
                }
            )
        }
    }
}

val postTagIds = mapOf(
    "ShackLOL" to "981756323848917012",
    "ShackINF" to "981756323525967973",
    "ShackUNF" to "981756323819565129",
    "ShackWTF" to "981756323932823592",
    "ShackWOW" to "981756323827965963",
    "ShackAWW" to "981756323874078740"
)

val postTagColors = mapOf(
    "981756323848917012" to Color(0xFFFF9800),
    "981756323525967973" to Color.Blue,
    "981756323819565129" to Color.Red,
    "981756323932823592" to Color(0xFF9C27B0),
    "981756323827965963" to Color.White,
    "981756323874078740" to Color(0xFF00BCD4)
)


// Messages (including OP)
@Composable
fun ThreadedMessage(
    message: Message,
    indent: Int = 0,
    isSelected: Boolean = false,
    recentIndex: Int? = null,
    onMessageClick: (Message) -> Unit = {}
) {
    val backgroundColor = when {
        isSelected -> Color(0xFFA459D6).copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val textOpacity = when (recentIndex) {
        0 -> 1f    // Most recent
        1 -> 0.95f
        2 -> 0.9f
        3 -> 0.85f
        4 -> 0.8f
        else -> 0.75f  // Not recent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (indent * 16).dp)
            .background(backgroundColor)
            .clickable { onMessageClick(message) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (indent > 0) {
                Text(
                    "C",
                    fontFamily = replylines,
                    fontSize = 16.sp,
                    color = Color.LightGray
                )
            }
            if (message.content.isBlank() && message.attachments.isNotEmpty()){
                Text(
                    text = "${message.attachments?.size} attachment(s)...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (recentIndex != null) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Left,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(textOpacity)
                )
            }
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (recentIndex != null) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Left,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .alpha(textOpacity)
            )
            Text(
                text = message.author.globalName ?: message.author.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.orange),
                textAlign = TextAlign.Right,
                maxLines = 1
            )

            // Display reactions and colored tags
            message.reactions?.forEach { reaction ->
                reaction.emoji.id?.let { emojiId ->
                    // If this emoji ID is in postTagColors, display the colored "A"
                    postTagColors[emojiId]?.let { color ->
                        Text(
                            "A",
                            fontFamily = tags,
                            fontSize = 10.sp,
                            color = color,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
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

@Composable
fun ArticleEmbed(
    embed: Embed,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            /*
            embed.thumbnail?.let { thumbnail ->
                Image(
                    painter = painterResource(id = R.drawable.ic_placeholder_white_24pd),
                    contentDescription = "Article thumbnail",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            */

            // Thumbnail image using Coil
            embed.thumbnail?.let { thumbnail ->
                AsyncImage(
                    model = thumbnail.url,
                    contentDescription = "Article thumbnail",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Article content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                embed.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                embed.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                embed.provider?.name?.let { providerName ->
                    Text(
                        text = providerName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageAttachments(
    attachments: List<Attachment>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 200.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        attachments.forEach { attachment ->
            when {
                attachment.contentType?.startsWith("image/") == true -> {
                    val isGif = attachment.contentType == "image/gif"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(attachment.url)
                            .decoderFactory(if (isGif) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    ImageDecoderDecoder.Factory()
                                } else {
                                    GifDecoder.Factory()
                                }
                            } else GifDecoder.Factory() )
                            .size(Size.ORIGINAL) // Preserve original dimensions for GIFs
                            .build(),
                        contentDescription = attachment.filename,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxHeight)
                            .padding(vertical = 4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    // For non-image attachments, show filename and size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${attachment.filename} (${formatFileSize(attachment.size)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format file size
private fun formatFileSize(bytes: Int): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

@Composable
private fun GroupedReactions(
    reactions: List<Reaction>,
    modifier: Modifier = Modifier
) {
    // Group reactions by type (standard vs custom)
    val standardEmojis = reactions.filter { it.emoji.id == null }
    val customEmojis = reactions.filter { it.emoji.id != null }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Standard emojis
        if (standardEmojis.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                standardEmojis.forEach { reaction ->
                    ReactionChip(
                        emoji = reaction.emoji.name,
                        count = reaction.count,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        // Custom tag emojis
        if (customEmojis.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                customEmojis.forEach { reaction ->
                    reaction.emoji.id?.let { emojiId ->
                        postTagColors[emojiId]?.let { color ->
                            TagReactionChip(
                                color = color,
                                count = reaction.count,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReactionChip(
    emoji: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " $count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun TagReactionChip(
    color: Color,
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "A",
                fontFamily = tags,
                fontSize = 10.sp,
                color = color
            )
            Text(
                text = " $count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}