package com.chrishodge.afternoonreading

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

// DiscordApi.kt
interface DiscordApi {
    @GET("channels/{channelId}/messages/{messageId}")
    suspend fun getMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String
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