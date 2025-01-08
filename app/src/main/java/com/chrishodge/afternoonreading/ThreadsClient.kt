package com.chrishodge.afternoonreading

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ThreadsClient(private val authToken: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getThreads(url: String): ThreadsResponse {
        return try {
            client.get(url) {
                // Add authorization header
                headers {
                    append(HttpHeaders.Authorization, "Bot $authToken")
                    // For Discord specifically, you might also want:
                    // append(HttpHeaders.Authorization, "Bot $authToken")
                }
            }.body()
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch threads: ${e.message}", e)
        }
    }

    fun close() {
        client.close()
    }
}