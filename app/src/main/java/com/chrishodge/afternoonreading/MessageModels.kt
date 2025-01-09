package com.chrishodge.afternoonreading

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    // Required fields
    val id: String,
    val type: Int,
    val content: String,
    val channel_id: String,
    val author: Author,
    val timestamp: String,
    val pinned: Boolean,
    val mention_everyone: Boolean,
    val tts: Boolean,

    // Optional fields with default values
    val edited_timestamp: String? = null,
    val mentions: List<Author> = emptyList(),
    val mention_roles: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val embeds: List<Embed> = emptyList(),
    val reactions: List<Reaction>? = null,
    val flags: Int = 0,
    val position: Int? = null,
    val message_reference: MessageReference? = null,
    val referenced_message: Message? = null
)

@Serializable
data class Author(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val discriminator: String,
    val public_flags: Int = 0,
    val flags: Int = 0,
    val banner: String? = null,
    val accent_color: Int? = null,
    @SerialName("global_name")
    val globalName: String? = null,
    // @SerialName("avatar_decoration_data")
    // val avatarDecorationData: AvatarDecorationData? = null,
    @SerialName("banner_color")
    val bannerColor: String? = null,
    val clan: String? = null,
    @SerialName("primary_guild")
    val primaryGuild: String? = null
)

@Serializable
data class AvatarDecorationData(
    val asset: String,
    @SerialName("sku_id")
    val skuId: String,
    @SerialName("expires_at")
    val expiresAt: String?
)

@Serializable
data class Attachment(
    val id: String,
    val filename: String,
    val size: Int,
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String,
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("content_type")
    val contentType: String? = null,
    @SerialName("content_scan_version")
    val contentScanVersion: Int? = null,
    val placeholder: String? = null,
    @SerialName("placeholder_version")
    val placeholderVersion: Int? = null
)

@Serializable
data class Embed(
    val type: String,
    val url: String? = null,
    val title: String? = null,
    val description: String? = null,
    val color: Int? = null,
    @SerialName("reference_id")
    val referenceId: String? = null,
    val provider: Provider? = null,
    val thumbnail: Thumbnail? = null,
    @SerialName("content_scan_version")
    val contentScanVersion: Int? = null
)

@Serializable
data class Provider(
    val name: String
)

@Serializable
data class Thumbnail(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String,
    val width: Int,
    val height: Int,
    val placeholder: String,
    @SerialName("placeholder_version")
    val placeholderVersion: Int,
    val flags: Int
)

@Serializable
data class Reaction(
    val emoji: Emoji,
    val count: Int,
    @SerialName("count_details")
    val countDetails: CountDetails,
    @SerialName("burst_colors")
    val burstColors: List<String>,
    @SerialName("me_burst")
    val meBurst: Boolean,
    @SerialName("burst_me")
    val burstMe: Boolean,
    val me: Boolean,
    @SerialName("burst_count")
    val burstCount: Int
)

@Serializable
data class Emoji(
    val id: String?,
    val name: String
)

@Serializable
data class CountDetails(
    val burst: Int,
    val normal: Int
)

@Serializable
data class MessageReference(
    val type: Int,
    @SerialName("channel_id")
    val channelId: String,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("guild_id")
    val guildId: String
)

class MessageViewModel : ViewModel() {
    private val discordApi = DiscordApi.create()
    private var _message = mutableStateOf<Message?>(null)
    val message: State<Message?> = _message

    private var _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private var _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun fetchMessage(channelId: String, messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = discordApi.getMessage(channelId, messageId)
                _message.value = result
                println("Message received: $result")
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                println("Error occurred: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}