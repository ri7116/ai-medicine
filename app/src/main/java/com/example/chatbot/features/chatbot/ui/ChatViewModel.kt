package com.example.chatbot.features.chatbot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.features.chatbot.data.ChatMessageUiModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val generativeModel = Firebase.vertexAI.generativeModel(
        modelName = "gemini-1.5-pro-latest",
        systemInstruction = content {
            text(
                """
                너는 동네 약사야. 
                사용자의 증상을 듣고 친절하게 상담해줘.
                
                1. 바로 약을 추천하지 말고 질문을 먼저 해서 상태를 파악해.
                2. 추천이 끝나면 마지막에 [추천약: 약이름 / 복용법: ... ] 형식으로 요약해줘.
                3. 답변할 때 절대 마크다운(**, *, # 등)을 사용하지 마.
                """.trimIndent()
            )
        }
    )
    private val chat = generativeModel.startChat()

    private val _messages = MutableStateFlow<List<ChatMessageUiModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageUiModel>> = _messages.asStateFlow()

    private var currentChatId: String? = null

    fun setChatId(chatId: String?) {
        currentChatId = chatId
        if (chatId != null) {
            loadMessages(chatId)
        } else {
            _messages.value = listOf(
                ChatMessageUiModel(
                    message = "안녕하세요! 무엇을 도와드릴까요?",
                    isUser = false
                )
            )
        }
    }

    private fun loadMessages(chatId: String) {
        firestore.collection("histories").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // ... (error handling) ...
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _messages.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessageUiModel::class.java)
                    }
                }
            }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            if (currentChatId == null) {
                val newDoc = firestore.collection("histories").document()
                currentChatId = newDoc.id
                loadMessages(currentChatId!!)
            }
            val chatId = currentChatId!!

            val userMsgMap = hashMapOf(
                "message" to userMessage,
                "isUser" to true,
                "timestamp" to timestamp
            )
            saveMessageToFirestore(chatId, userMsgMap)

            updateChatRoomSummary(
                chatId,
                userMessage,
                timestamp,
                isNewChat = messages.value.size <= 1
            )

            try {
                val response = chat.sendMessage(userMessage)
                val aiReply = response.text ?: "답변을 생성하지 못했습니다."
                val aiTimestamp = System.currentTimeMillis()

                val aiMsgMap = hashMapOf(
                    "message" to aiReply,
                    "isUser" to false,
                    "timestamp" to aiTimestamp
                )
                saveMessageToFirestore(chatId, aiMsgMap)

                updateChatRoomSummary(chatId, aiReply, aiTimestamp)
            } catch (e: Exception) {
                // ... (error handling) ...
            }
        }
    }

    private suspend fun saveMessageToFirestore(chatId: String, messageData: Map<String, Any>) {
        firestore.collection("histories").document(chatId)
            .collection("messages").add(messageData).await()
    }

    private suspend fun updateChatRoomSummary(
        chatId: String,
        lastMessage: String,
        timestamp: Long,
        isNewChat: Boolean = false,
    ) {
        val roomData = mutableMapOf<String, Any>(
            "lastMessage" to lastMessage,
            "timestamp" to timestamp
        )
        if (isNewChat) {
            roomData["title"] = lastMessage
        }
        firestore.collection("histories").document(chatId)
            .set(roomData, SetOptions.merge()).await()
    }
}


