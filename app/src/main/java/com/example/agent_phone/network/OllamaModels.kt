package com.example.agent_phone.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

@Serializable
data class OllamaOptions(
    @SerialName("num_predict") val numPredict: Int = 250,
    val temperature: Double = 0.5
)

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaChatMessage>,
    val options: OllamaOptions? = OllamaOptions(),
    val stream: Boolean = true
)

@Serializable
data class OllamaChatResponse(
    val model: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val message: OllamaChatMessage? = null,
    val done: Boolean = false
)
