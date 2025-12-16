package com.example.chatbot.features.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // [수정] 마크다운 사용 금지 규칙 추가 및 모델 이름 수정
    private val generativeModel = Firebase.vertexAI.generativeModel(
        modelName = "gemini-2.0-flash", // 2.0-flash는 아직 존재하지 않으므로, 안정적인 최신 프로 모델로 수정
        systemInstruction = content {
            text("""
                너는 동네 약사야. 
                사용자의 증상을 듣고 친절하게 상담해줘.
                
                1. 바로 약을 추천하지 말고 질문을 먼저 해서 상태를 파악해.
                2. 추천이 끝나면 마지막에 [추천약: 약이름 / 복용법: ... ] 형식으로 요약해줘.
                3. 답변할 때 절대 마크다운(**, *, # 등)을 사용하지 마.
            """.trimIndent())
        }
    )
    private val chat = generativeModel.startChat()

    private val _messages = MutableStateFlow<List<ChatMessageUiModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageUiModel>> = _messages.asStateFlow()

    private var currentChatId: String? = null

    // 채팅방 ID 설정 및 기존 메시지 불러오기
    fun setChatId(chatId: String?) {
        currentChatId = chatId
        if (chatId != null) {
            loadMessages(chatId)
        } else {
            // 새 채팅일 경우 환영 메시지 (로컬에만 표시)
            _messages.value = listOf(ChatMessageUiModel("안녕하세요! 무엇을 도와드릴까요?", isUser = false))
        }
    }

    // Firestore에서 메시지 목록 실시간 감지
    private fun loadMessages(chatId: String) {
        firestore.collection("histories").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    val errorMessage = ChatMessageUiModel(
                        message = "🔥 데이터 읽기 실패: ${e.message}",
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + errorMessage
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _messages.value = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            ChatMessageUiModel(
                                message = data["message"] as? String ?: "(내용 없음)",
                                isUser = data["isUser"] as? Boolean ?: false,
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                            )
                        } catch (exception: Exception) {
                            ChatMessageUiModel(
                                message = "⚠️ 데이터 변환 오류: ${exception.message}",
                                isUser = false
                            )
                        }
                    }
                }
            }
    }

    // 메시지 전송 및 저장 로직
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

            updateChatRoomSummary(chatId, userMessage, timestamp, isNewChat = messages.value.size <= 2)

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
                val errorMsg = "오류: ${e.localizedMessage}"
                val errorMap = hashMapOf(
                    "message" to errorMsg,
                    "isUser" to false,
                    "timestamp" to System.currentTimeMillis()
                )
                saveMessageToFirestore(chatId, errorMap)
            }
        }
    }

    private suspend fun saveMessageToFirestore(chatId: String, messageData: Map<String, Any>) {
        firestore.collection("histories").document(chatId)
            .collection("messages").add(messageData).await()
    }

    private suspend fun updateChatRoomSummary(chatId: String, lastMessage: String, timestamp: Long, isNewChat: Boolean = false) {
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
