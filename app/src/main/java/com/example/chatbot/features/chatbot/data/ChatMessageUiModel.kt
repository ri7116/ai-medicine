package com.example.chatbot.features.chatbot.data

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// 채팅 메시지 하나를 나타내는 데이터 구조
@IgnoreExtraProperties
data class ChatMessageUiModel(
    val message: String = "",
    @get:PropertyName("isUser")
    @set:PropertyName("isUser")
    var isUser: Boolean = false,
    val timestamp: Long = 0
)
