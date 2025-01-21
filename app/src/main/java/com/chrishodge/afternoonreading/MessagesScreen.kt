package com.chrishodge.afternoonreading

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import kotlinx.coroutines.launch
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
    val channelOp by mainViewModel.channelOP.collectAsState()
    var messageId by remember { mutableStateOf("0") }
    val messageContentScrollState = rememberScrollState()
    val messageListScrollState = rememberScrollState()
    val messages by messageViewModel?.messages?.collectAsState(initial = emptyList()) ?: remember {
        mutableStateOf(emptyList())
    }
    // Track selected message ID separately
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    // Create derived state that updates when either messages or selectedMessageId changes
    val selectedMessage by remember(messages, selectedMessageId) {
        mutableStateOf(messages.find { it.id == selectedMessageId })
    }
    val userToken by mainViewModel.userToken.collectAsState(initial = "")
    var showReplySheet by remember { mutableStateOf(false) }
    val submitMessageScope = rememberCoroutineScope()
    var splitRatio by remember { mutableFloatStateOf(0.4f) }
    val nickname by mainViewModel.nickname.collectAsState("")


    // Update messages when channelId changes
    LaunchedEffect(channelId) {
        messageId = channelId
        if (channelId != "0") {
            if (channelOp == null) {
                val fetchedOp = messageViewModel?.fetchOp(channelId, channelId)
                mainViewModel.setChannelOp(fetchedOp)
                selectedMessageId = fetchedOp?.id
            } else {
                selectedMessageId = channelOp?.id
            }
            messageViewModel?.fetchMessages(channelId, 100, channelOp)
        }
    }


    // Update selected message when new messages come in
    LaunchedEffect(messages) {
        if (selectedMessageId == null && messages.isNotEmpty()) {
            selectedMessageId = messages.first().id
        }
    }

    DisposableEffect(Unit) {
        onDispose {
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
                        messageViewModel?.clearMessages()
                        selectedMessageId = null
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
        val layoutScope = rememberCoroutineScope()
        var layoutHeight by remember { mutableFloatStateOf(0f) }
        var splitRatio by remember { mutableFloatStateOf(0.4f) }
        var showReplySheet by remember { mutableStateOf(false) }
        val userToken by mainViewModel.userToken.collectAsState()
        val nickname by mainViewModel.nickname.collectAsState("")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .onSizeChanged {
                    layoutHeight = it.height.toFloat()
                }
        ) {
            // Top section (message content)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(splitRatio)
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

                    // Display reactions
                    selectedMessage?.reactions?.let { reactions ->
                        GroupedReactions(
                            reactions = reactions,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Markdown title or name
                    if (selectedMessage?.id == channelId) {
                        SimpleMarkdownText(
                            markdown = channelName,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Markdown content
                    EnhancedMarkdownText(
                        markdown = selectedMessage?.mentionsContent ?: "",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Display video embeds
                    selectedMessage?.embeds?.forEach { embed ->
                        if (embed.type == "video") {
                            VideoEmbed(
                                embed = embed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Display article and image embeds
                    selectedMessage?.let { message ->
                        ImageEmbeds(
                            message = message,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Display attachments
                    selectedMessage?.attachments?.let { attachments ->
                        if (attachments.isNotEmpty()) {
                            MessageAttachments(
                                attachments = attachments,
                                modifier = Modifier.fillMaxWidth(),
                                maxHeight = 300.dp  // Larger for detail view
                            )
                        }
                    }
                }
            }

            // Action buttons section with drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.25f))
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            // Get total height from LocalDensity
                            val totalHeight = layoutHeight
                            // Convert delta to ratio
                            val ratioChange = delta / totalHeight
                            val newRatio = splitRatio + ratioChange
                            // Clamp the ratio between 0.2 and 0.8 (20% - 80%)
                            splitRatio = newRatio.coerceIn(0.2f, 0.8f)
                        }
                    )
                    .padding(8.dp)
            ) {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(32.dp)
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                )
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
                        IconButton(onClick = {
                            messageViewModel?.fetchMessages(channelId, 100, channelOp)
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    selectedMessage?.let {
                        Box {
                            MoreDropdownMenu(message = it, mainViewModel = mainViewModel)
                        }
                    }

                    selectedMessage?.let {
                        if (userToken.isNotEmpty()) {
                            if (messageViewModel != null) {
                                Box {
                                    TagMenu(message = it, mainViewModel = mainViewModel, messageViewModel = messageViewModel)
                                }
                            }
                        }
                    }

                    if (userToken.isNotEmpty()) {
                        IconButton(onClick = { showReplySheet = true }) {
                            Image(
                                painterResource(R.drawable.ic_reply_white_24dp),
                                contentDescription = "Reply",
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier.fillMaxHeight(),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        }

                        if (showReplySheet) {
                            ReplyBottomSheet(
                                onDismiss = { showReplySheet = false },
                                onSubmit = { replyText ->
                                    selectedMessage?.let { message ->
                                        submitMessageScope.launch {
                                            messageViewModel?.submitReply(
                                                messageId = message.id,
                                                channelId = channelId,
                                                messageContent = replyText,
                                                guildId = mainViewModel.guildId.value,
                                                token = mainViewModel.userToken.value
                                            )
                                        }
                                    }
                                    showReplySheet = false
                                },
                                replyingToMessage = selectedMessage
                            )
                        }
                    }
                }
            }

            // Messages list section
            ThreadedMessageList(
                messages = messages,
                modifier = Modifier.weight(1f - splitRatio),
                selectedMessage = selectedMessage,
                nickname = nickname,
                onMessageSelected = { message ->
                    selectedMessageId = message.id
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
    nickname: String,
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

    // Messages list
    LazyColumn(
        contentPadding = PaddingValues(
            start = 0.dp,
            top = 2.dp,
            end = 0.dp,
            bottom = 16.dp  // Add your desired bottom padding here
        ),
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
                nickname = nickname,
                onMessageClick = {
                    selectedMessageId = it.id
                    onMessageSelected(it)
                }
            )
        }
    }
}

// TODO: Make this customizable
val postTagIds = mapOf(
    "daw" to "1331058613480853664",
    "lol" to "1331060088143745114",
    "inf" to "1331059807620169859",
    "unf" to "1331058616442032201",
    "ugh" to "1331063798198697984",
    "wow" to "1331060297124806789",
    "yw" to "1320755240349208656",
    "ty" to "1320755236851286068",
    "wtf" to "1331058619491291259"
)

// TODO: Make this customizable
val postTagColors = mapOf(
    "1331058613480853664" to Color(0xFFE692EE),
    "1331060088143745114" to Color(0xFFE9973F),
    "1331059807620169859" to Color(0xFF488CF6),
    "1331058616442032201" to Color(0xFFC62A1C),
    "1331063798198697984" to Color(0xFF64D140),
    "1331060297124806789" to Color(0xFFC89D7F),
    "1320755240349208656" to Color(0xFFE99E41),
    "1320755236851286068" to Color(0xFF5EAB32),
    "1331058619491291259" to Color(0xFFC0356B )
)

// Messages (including OP)
@Composable
fun ThreadedMessage(
    message: Message,
    indent: Int = 0,
    isSelected: Boolean = false,
    recentIndex: Int? = null,
    nickname: String,
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
                text = message.cleanContent ?: message.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (recentIndex != null) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Left,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .alpha(textOpacity)
            )
            Text(
                text = message.author.globalName ?: message.author.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (message.author.username.lowercase() == nickname.lowercase()) colorResource(id = R.color.blue) else colorResource(id = R.color.orange),
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
fun ArticleEmbed(
    embed: Embed,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(8.dp)
            .clickable {
                // Open the URL when clicked
                embed.url?.let { url ->
                    uriHandler.openUri(url)
                }
            }
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
    val uriHandler = LocalUriHandler.current
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
                            .padding(vertical = 4.dp)
                            .clickable {
                                uriHandler.openUri(attachment.url)
                            },
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    // For non-image attachments, show filename and size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                uriHandler.openUri(attachment.url)
                            },
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
                        TagReactionChip(
                            emojiId = emojiId,
                            count = reaction.count,
                            modifier = Modifier.padding(end = 8.dp)
                        )
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
                style = MaterialTheme.typography.bodyMedium,
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
    emojiId: String,
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
            // Load emoji image from Discord CDN
            AsyncImage(
                model = "https://cdn.discordapp.com/emojis/$emojiId.png?size=32",
                contentDescription = "Tag emoji",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
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
fun MoreDropdownMenu(message: Message, mainViewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showHideDialog) {
        AlertDialog(
            onDismissRequest = { showHideDialog = false },
            title = { Text("Block User") },
            text = { Text("Are you sure you want to block this user?") },
            confirmButton = {
                TextButton(onClick = {
                    // mainViewModel.addHiddenId(thread.id)
                    showHideDialog = false
                    expanded = false
                }) {
                    Text("Block")
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
            text = { Text("Block User", color = MaterialTheme.colorScheme.primary) },
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

@Composable
fun TagMenu(message: Message, mainViewModel: MainViewModel, messageViewModel: MessageViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val submitReactionScope = rememberCoroutineScope()

    IconButton(onClick = { expanded = !expanded }) {
        Image(
            painterResource(R.drawable.ic_tag_white_24dp),
            contentDescription = "Tag",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxHeight(),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        postTagIds.map { (tag, id) ->
            DropdownMenuItem(
                text = { Text(tag.removePrefix("Shack"), color = MaterialTheme.colorScheme.primary) },
                onClick = {
                    submitReactionScope.launch {
                        messageViewModel.submitReaction(
                            channelId = message.channelId,
                            messageId = message.id,
                            emojiName = tag,
                            emojiId = id,
                            token = mainViewModel.userToken.value
                        )
                    }
                    expanded = false
                    Toast.makeText(context, "Tagged!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    replyingToMessage: Message?
) {
    val sheetState = rememberModalBottomSheetState()
    var replyText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show who we're replying to
            replyingToMessage?.let { message ->
                Text(
                    text = "Replying to ${message.author.globalName ?: message.author.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Original message preview
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reply input field
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type your reply...") },
                minLines = 3,
                maxLines = 5
            )

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { scope.launch { onDismiss() } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss")
                }

                Button(
                    onClick = {
                        scope.launch {
                            onSubmit(replyText)
                            Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    enabled = replyText.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Submit")
                }
            }

            // Add some padding at the bottom to account for system navigation
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ImageEmbed(
    embed: Embed,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    embed.thumbnail?.let { thumbnail ->
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnail.url)
                .crossfade(true)
                .build(),
            contentDescription = embed.title ?: "Embedded image",
            modifier = modifier
                .clickable {
                    // Open the URL when clicked
                    embed.url?.let { url ->
                        uriHandler.openUri(url)
                    }
                }
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(vertical = 4.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun ImageEmbeds(
    message: Message,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Display message embeds
        message.embeds.forEach { embed ->
            when (embed.type) {
                "image" -> ImageEmbed(
                    embed = embed,
                    modifier = Modifier.fillMaxWidth()
                )
                "article" -> ArticleEmbed(
                    embed = embed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun VideoEmbed(
    embed: Embed,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        // Clickable title that opens the video URL
        val uriHandler = LocalUriHandler.current

        // Video thumbnail
        embed.thumbnail?.let { thumbnail ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnail.url)
                    .crossfade(true)
                    .build(),
                contentDescription = embed.title ?: "Video thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clickable {
                        embed.url?.let { url ->
                            uriHandler.openUri(url)
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Video title
        embed.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
                        embed.url?.let { url ->
                            uriHandler.openUri(url)
                        }
                    }
            )
        }

        // Channel/Author info
        embed.author?.let { author ->
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = author.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        author.url?.let { url ->
                            uriHandler.openUri(url)
                        }
                    }
                )
            }
        }

        // Provider info (YouTube, etc)
        embed.provider?.let { provider ->
            Text(
                text = provider.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ClickableLinksText(
    annotatedText: AnnotatedString,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val urlRegex = """(?:https?://)[^\s]+""".toRegex()
    val urlRanges = urlRegex.findAll(annotatedText.text)

    val finalAnnotatedString = buildAnnotatedString {
        // Append the existing annotated string with all its spans
        append(annotatedText)

        // Add URL annotations and styling
        urlRanges.forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val url = matchResult.value

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end
            )

            addStyle(
                style = SpanStyle(
                    color = Color(0xFFA459D6),
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
        }
    }

    ClickableText(
        text = finalAnnotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier,
        onClick = { offset ->
            finalAnnotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
        }
    )
}

@Composable
fun EnhancedMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    // Keep track of which spoilers have been revealed
    var revealedSpoilers by remember { mutableStateOf(mutableSetOf<String>()) }

    // Build annotated string to handle both bold and spoiler formatting
    val annotatedText = buildAnnotatedString {
        var currentIndex = 0
        val spoilerPattern = """(\|\|)(.*?)(\|\|)""".toRegex()
        val boldPattern = """(\*\*|__)(.*?)\1""".toRegex()

        // Find both spoilers and bold text
        val allMatches = (spoilerPattern.findAll(markdown) + boldPattern.findAll(markdown))
            .sortedBy { it.range.first }

        allMatches.forEach { match ->
            // Add text before the pattern
            append(markdown.substring(currentIndex, match.range.first))

            // Handle spoiler text (||text||)
            if (match.value.startsWith("||")) {
                val spoilerText = match.groupValues[2]
                val spoilerIndex = match.range.first
                // Create unique identifier using both position and content
                val spoilerId = "$spoilerIndex:$spoilerText"

                // Add a custom string annotation for spoiler
                pushStringAnnotation("spoiler", spoilerId)

                withStyle(SpanStyle(
                    background = if (revealedSpoilers.contains(spoilerId)) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    color = if (revealedSpoilers.contains(spoilerId)) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                )) {
                    append(spoilerText)
                }

                pop() // Pop the string annotation
            }
            // Handle bold text (**text** or __text__)
            else {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[2])
                }
            }

            currentIndex = match.range.last + 1
        }

        // Add remaining text
        if (currentIndex < markdown.length) {
            append(markdown.substring(currentIndex))
        }
    }

    val uriHandler = LocalUriHandler.current
    val urlRegex = """(?:https?://)[^\s]+""".toRegex()
    val urlRanges = urlRegex.findAll(annotatedText.text)

    val finalAnnotatedString = buildAnnotatedString {
        // Append the existing annotated string with all its spans
        append(annotatedText)

        // Add URL annotations and styling
        urlRanges.forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val url = matchResult.value

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end
            )

            addStyle(
                style = SpanStyle(
                    color = Color(0xFFA459D6),
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
        }
    }

    ClickableText(
        text = finalAnnotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier,
        onClick = { offset ->
            // Check for spoiler clicks
            finalAnnotatedString.getStringAnnotations(
                tag = "spoiler",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                // Reveal only the clicked spoiler
                revealedSpoilers = (revealedSpoilers + annotation.item).toMutableSet()
            }

            // Check for URL clicks
            finalAnnotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
        }
    )
}