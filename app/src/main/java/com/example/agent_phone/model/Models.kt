package com.example.agent_phone.model

import java.util.UUID

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String,
    val phoneNumber: String,
    val avatarColorIndex: Int = (0..5).random()
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val contactId: String,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
