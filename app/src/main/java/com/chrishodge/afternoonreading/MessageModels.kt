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
    val id: String,
    val type: Int,
    val content: String,
    val channel_id: String,
    val author: Author,
    val timestamp: String,
    val edited_timestamp: String?,
    val mentions: List<Author>,
    val mention_roles: List<String>,
    val attachments: List<Attachment>,
    val embeds: List<Embed>,
    val reactions: List<Reaction>? = null,
    val pinned: Boolean,
    val mention_everyone: Boolean,
    val tts: Boolean,
    val flags: Int,
    // val components: List<Any>,
    val position: Int? = 1,
    val message_reference: MessageReference? = null,
    val referenced_message: Message? = null
)

@Serializable
data class Author(
    val id: String,
    val username: String,
    val avatar: String?,
    val discriminator: String,
    val public_flags: Int,
    val flags: Int,
    val banner: String?,
    val accent_color: Int?,
    @SerialName("global_name")
    val globalName: String?,
    @SerialName("avatar_decoration_data")
    val avatarDecorationData: AvatarDecorationData?,
    @SerialName("banner_color")
    val bannerColor: String?,
    val clan: String?,
    @SerialName("primary_guild")
    val primaryGuild: String?
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
    val width: Int,
    val height: Int,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("content_scan_version")
    val contentScanVersion: Int,
    val placeholder: String,
    @SerialName("placeholder_version")
    val placeholderVersion: Int
)

@Serializable
data class Embed(
    val type: String,
    val url: String,
    val title: String,
    val description: String,
    val color: Int,
    @SerialName("reference_id")
    val referenceId: String? = null,
    val provider: Provider? = null,
    val thumbnail: Thumbnail? = null,
    @SerialName("content_scan_version")
    val contentScanVersion: Int
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