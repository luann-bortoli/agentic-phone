package com.example.agent_phone.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class OllamaService(
    var baseUrl: String = "http://10.0.2.2:11434",
    var selectedModel: String = "qwen3.8:27b"
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 300_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 300_000
        }
    }

    fun chatStream(
        systemPrompt: String,
        history: List<OllamaChatMessage>
    ): Flow<String> = flow {
        val messages = mutableListOf<OllamaChatMessage>()
        messages.add(OllamaChatMessage(role = "system", content = systemPrompt))
        messages.addAll(history)

        val request = OllamaChatRequest(
            model = selectedModel,
            messages = messages,
            options = OllamaOptions(numPredict = 250, temperature = 0.5),
            stream = true
        )

        try {
            client.preparePost("$baseUrl/api/chat") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    emit("[Ollama HTTP Error: ${response.status.value} ${response.status.description}]")
                    return@execute
                }

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.isBlank()) continue
                    try {
                        val chunk = json.decodeFromString<OllamaChatResponse>(line)
                        chunk.message?.content?.let { content ->
                            if (content.isNotEmpty()) {
                                emit(content)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("OllamaService", "Error parsing chunk line: $line", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OllamaService", "Connection error", e)
            emit("[Error connecting to Ollama at $baseUrl. Ensure Ollama is running with OLLAMA_HOST=0.0.0.0. Error: ${e.localizedMessage}]")
        }
    }
}
