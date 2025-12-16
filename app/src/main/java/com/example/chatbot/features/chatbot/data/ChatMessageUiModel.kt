package com.example.chatbot.features.chatbot.data

// 채팅 메시지 하나를 나타내는 데이터 구조
data class ChatMessageUiModel(
    val message: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = 0
)
