package com.chrishodge.afternoonreading

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
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

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000  // 30 seconds for the entire request
            connectTimeoutMillis = 15_000  // 15 seconds to establish a connection
            socketTimeoutMillis = 15_000   // 15 seconds between data packets
        }

        engine {
            requestTimeout = 30_000 // 30 seconds
            endpoint {
                connectTimeout = 15_000
                socketTimeout = 15_000
                keepAliveTime = 15_000
            }
        }
    }

    suspend fun getMessage(url: String): Message {
        return try {
            val response = client.get(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bot $authToken")
                }
            }

            // Print raw response for debugging
            //val responseText = response.body<String>()
            //println("Raw response: $responseText")

            // Then parse it
            client.get(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bot $authToken")
                }
            }.body()
        } catch (e: Exception) {
            println("Error occurred during getMessage: ${e.message}")
            throw RuntimeException("Failed to fetch message: ${e.message}", e)
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
            throw RuntimeException("Failed to fetch threads: ${e.message} at $url", e)
        }
    }

    fun close() {
        client.close()
    }
}
