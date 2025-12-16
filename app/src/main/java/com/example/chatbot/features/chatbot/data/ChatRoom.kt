package com.example.chatbot.features.chatbot.data

// Firestore에서 가져온 대화 목록 아이템의 데이터 구조
data class ChatRoom(
    val id: String = "",
    val title: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0
)
