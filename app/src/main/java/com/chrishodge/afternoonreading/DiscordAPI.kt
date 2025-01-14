package com.chrishodge.afternoonreading

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// DiscordApi.kt
interface DiscordApi {
    @GET("channels/{channelId}/messages")
    suspend fun getMessages(
        @Path("channelId") channelId: String,
        @Query("limit") limit: Int
    ): List<Message>

    @GET("channels/{channelId}/messages/{messageId}")
    suspend fun getMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String
    ): Message

    @GET("channels/{channelId}/messages")
    suspend fun getMessagesAfter(
        @Path("channelId") channelId: String,
        @Query("limit") limit: Int,
        @Query("after") after: String
    ): List<Message>

    @POST("channels/{channelId}/messages")
    suspend fun createMessage(
        @Path("channelId") channelId: String,
        @Body message: NewMessage
    ): Message

    companion object {
        fun create(): DiscordApi {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val apiKey = BuildConfig.API_KEY
            return Retrofit.Builder()
                .baseUrl("https://canary.discord.com/api/v9/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(
                    OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bot $apiKey")
                            .build()
                        chain.proceed(request)
                    }
                    .build())
                .build()
                .create(DiscordApi::class.java)
        }
    }
}