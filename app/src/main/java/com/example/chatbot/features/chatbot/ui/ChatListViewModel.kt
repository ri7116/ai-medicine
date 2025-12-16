package com.example.chatbot.features.chatbot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.features.chatbot.data.ChatRoom
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChatListViewModel : ViewModel() {

    // Firestore의 'histories' 컬렉션을 시간순으로 정렬하여 실시간으로 관찰합니다.
    val chatRooms: StateFlow<List<ChatRoom>> = FirebaseFirestore.getInstance()
        .collection("histories")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                // Firestore 문서를 ChatRoom 객체로 변환합니다.
                doc.toObject<ChatRoom>()?.copy(id = doc.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}



