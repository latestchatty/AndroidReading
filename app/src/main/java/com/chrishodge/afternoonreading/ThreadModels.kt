package com.chrishodge.afternoonreading

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ThreadsResponse(
    val threads: List<Thread>,
    val members: List<JsonElement>,
    @SerialName("has_more")
    val hasMore: Boolean
)

@Serializable
data class Thread(
    val id: String,
    val type: Int,
    @SerialName("last_message_id")
    val lastMessageId: String,
    val flags: Int,
    @SerialName("guild_id")
    val guildId: String,
    var name: String,
    @SerialName("parent_id")
    val parentId: String,
    @SerialName("rate_limit_per_user")
    val rateLimitPerUser: Int,
    val bitrate: Int? = null,
    @SerialName("user_limit")
    val userLimit: Int? = null,
    @SerialName("rtc_region")
    val rtcRegion: String? = null,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("thread_metadata")
    val threadMetadata: ThreadMetadata,
    @SerialName("message_count")
    val messageCount: Int,
    @SerialName("member_count")
    val memberCount: Int,
    @SerialName("total_message_sent")
    val totalMessageSent: Int,
    @SerialName("applied_tags")
    val appliedTags: List<String>? = null,
    @SerialName("last_pin_timestamp")
    val lastPinTimestamp: String? = null,

    var author: String? = "",
    var username: String? = "",
    var firstPost: Message? = null
)

@Serializable
data class ThreadMetadata(
    val archived: Boolean,
    @SerialName("archive_timestamp")
    val archiveTimestamp: String,
    @SerialName("auto_archive_duration")
    val autoArchiveDuration: Int,
    val locked: Boolean,
    @SerialName("create_timestamp")
    val createTimestamp: String? = null
)