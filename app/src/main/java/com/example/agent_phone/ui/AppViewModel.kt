package com.example.agent_phone.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent_phone.model.ChatMessage
import com.example.agent_phone.model.Contact
import com.example.agent_phone.network.OllamaChatMessage
import com.example.agent_phone.network.OllamaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel : ViewModel() {

    val ollamaService = OllamaService()

    private val _contacts = MutableStateFlow<List<Contact>>(
        listOf(
            Contact(
                id = "dave_lawyer",
                name = "Dave",
                role = "Lawyer",
                phoneNumber = "9392532052"
            )
        )
    )
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "dave_lawyer" to listOf(
                ChatMessage(
                    contactId = "dave_lawyer",
                    isFromUser = false,
                    text = "Hello! I am Dave, an experienced lawyer. How can I assist you with legal matters today?"
                )
            )
        )
    )
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun updateOllamaConfig(baseUrl: String, model: String) {
        if (baseUrl.isNotBlank()) ollamaService.baseUrl = baseUrl.trim()
        if (model.isNotBlank()) ollamaService.selectedModel = model.trim()
    }

    fun addContact(name: String, role: String, phoneNumber: String) {
        val newContact = Contact(
            name = name.trim(),
            role = role.trim(),
            phoneNumber = phoneNumber.trim()
        )
        _contacts.update { currentList -> currentList + newContact }
        _messages.update { currentMap ->
            currentMap + (newContact.id to listOf(
                ChatMessage(
                    contactId = newContact.id,
                    isFromUser = false,
                    text = "Hello! I am ${newContact.name}, a specialist in ${newContact.role}. How can I help you today?"
                )
            ))
        }
    }

    fun sendMessage(contactId: String, text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMsg = ChatMessage(
            contactId = contactId,
            isFromUser = true,
            text = text.trim()
        )

        val contact = _contacts.value.find { it.id == contactId } ?: return

        // 1. Append user message
        val updatedHistory = (_messages.value[contactId] ?: emptyList()) + userMsg
        _messages.update { currentMap -> currentMap + (contactId to updatedHistory) }

        // 2. Prepare assistant placeholder message
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantPlaceholder = ChatMessage(
            id = assistantMsgId,
            contactId = contactId,
            isFromUser = false,
            text = ""
        )

        _messages.update { currentMap ->
            val list = currentMap[contactId] ?: emptyList()
            currentMap + (contactId to (list + assistantPlaceholder))
        }

        // 3. Build system prompt & chat history for Ollama
        val systemPrompt = """
            You are ${contact.name}, an expert ${contact.role}.

            STRICT RULES:
            1. SCOPE: You MUST ONLY answer questions directly related to your role as a ${contact.role}. If the user asks about ANY topic outside of ${contact.role}, you MUST politely refuse and state: "I am ${contact.name}, a ${contact.role}, so I can only help you with ${contact.role} matters."
            2. LENGTH LIMIT: Keep your response under 80 words (maximum 2 to 3 short sentences). Do NOT write bullet points or long explanations.
            3. PERSONA: Speak naturally like a human sending a quick WhatsApp text message.
            4. DIRECT ANSWER: Do NOT output reasoning or <think> tags. Output ONLY your direct reply.
        """.trimIndent()

        val ollamaHistory = updatedHistory.map { msg ->
            OllamaChatMessage(
                role = if (msg.isFromUser) "user" else "assistant",
                content = msg.text
            )
        }

        // 4. Stream response from Ollama
        viewModelScope.launch {
            _isGenerating.value = true
            var accumulatedResponse = ""

            ollamaService.chatStream(
                systemPrompt = systemPrompt,
                history = ollamaHistory
            ).collect { chunk ->
                accumulatedResponse += chunk
                val cleanedText = cleanResponseText(accumulatedResponse)

                _messages.update { currentMap ->
                    val currentList = currentMap[contactId] ?: emptyList()
                    val newList = currentList.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(text = cleanedText)
                        } else {
                            msg
                        }
                    }
                    currentMap + (contactId to newList)
                }
            }

            if (accumulatedResponse.isBlank()) {
                val fallbackText = "Sorry, no response received. Please ensure model '${ollamaService.selectedModel}' is available in Ollama."
                _messages.update { currentMap ->
                    val currentList = currentMap[contactId] ?: emptyList()
                    val newList = currentList.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(text = fallbackText)
                        } else {
                            msg
                        }
                    }
                    currentMap + (contactId to newList)
                }
            }

            _isGenerating.value = false
        }
    }

    private fun cleanResponseText(text: String): String {
        return text.replace(Regex("(?s)<think>.*?</think>"), "").trim()
    }
}
