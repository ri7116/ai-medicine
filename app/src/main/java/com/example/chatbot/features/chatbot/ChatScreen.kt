package com.example.chatbot.features.chatbot // ★ 패키지 이름은 본인 프로젝트에 맞게 수정하세요

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatbot.ui.theme.ChatBotTheme // ★ 테마 이름이 다르면 빨간줄 뜰 수 있음 (지워도 됨)
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 데이터 모델
data class ChatMessageUiModel(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// 시간 포맷 확장 함수
fun Long.formatTime(): String {
    val sdf = SimpleDateFormat("a h:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

// 색상 및 스타일 정의
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray500 = Color(0xFF9E9E9E)
val Gray900 = Color(0xFF212121)

val TextSRegular = TextStyle(fontSize = 16.sp, color = Gray900)
val InfoS = TextStyle(fontSize = 12.sp, color = Gray500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier) {

    // 1. AI 모델 준비 (최신 2.0 모델 사용)
    // remember를 사용하여 리컴포지션 시에도 객체가 유지되도록 함
    val generativeModel = remember {
        Firebase.vertexAI.generativeModel("gemini-2.0-flash")
    }
    val chat = remember { generativeModel.startChat() }

    val scope = rememberCoroutineScope()
    var textInput by remember { mutableStateOf("") }
    // 화면에 보여줄 메시지 리스트
    val messages = remember { mutableStateListOf<ChatMessageUiModel>() }

    // 2. 최초 실행 시 환영 메시지 (한 번만 실행)
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(ChatMessageUiModel("안녕하세요! 무엇을 도와드릴까요?", isUser = false))
        }
    }

    // 스크롤 상태 제어
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // 하단 입력창 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .navigationBarsPadding(), // 네비게이션 바 겹침 방지
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("메시지 입력...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (textInput.isNotBlank()) {
                        val userMessage = textInput
                        // 사용자 메시지 추가
                        messages.add(ChatMessageUiModel(userMessage, isUser = true))
                        textInput = "" // 입력창 초기화

                        scope.launch {
                            try {
                                // AI에게 전송
                                val response = chat.sendMessage(userMessage)
                                // AI 응답 추가
                                messages.add(
                                    ChatMessageUiModel(
                                        response.text ?: "답변이 없습니다.",
                                        isUser = false
                                    )
                                )
                            } catch (e: Exception) {
                                messages.add(
                                    ChatMessageUiModel(
                                        "오류 발생: ${e.localizedMessage}",
                                        isUser = false
                                    )
                                )
                            }
                        }
                    }
                }) {
                    Text("전송")
                }
            }
        }
    ) { innerPadding ->
        // 채팅 리스트 영역
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(chatMessage = message)
            }
        }

        // 메시지가 추가될 때마다 맨 아래로 스크롤
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
}

@Composable
fun ChatBubble(
    modifier: Modifier = Modifier,
    chatMessage: ChatMessageUiModel,
) {
    val messageArrangement = if (chatMessage.isUser) Arrangement.End else Arrangement.Start
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        horizontalArrangement = messageArrangement,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (chatMessage.isUser) {
            // 사용자 메시지 (오른쪽)
            TimeText(time = chatMessage.timestamp.formatTime())
            Spacer(modifier = Modifier.width(8.dp))
            MessageBox(
                message = chatMessage.message,
                isUser = true,
            )
        } else {
            // AI 메시지 (왼쪽)
            ProfileImage(
                modifier = Modifier
                    .align(Alignment.Top)
                    .size(40.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            MessageBox(
                message = chatMessage.message,
                isUser = false,
            )
            Spacer(modifier = Modifier.width(8.dp))
            TimeText(time = chatMessage.timestamp.formatTime())
        }
    }
}

@Composable
fun MessageBox(
    modifier: Modifier = Modifier,
    message: String,
    isUser: Boolean,
) {
    val maxWidthDp = LocalConfiguration.current.screenWidthDp.dp * 2 / 3
    Box(
        modifier = modifier
            .widthIn(max = if (isUser) maxWidthDp else maxWidthDp - 56.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp
                )
            )
            .background(if (isUser) Gray200 else Gray300)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = message,
            style = TextSRegular,
        )
    }
}

@Composable
fun TimeText(time: String) {
    Text(
        text = time,
        style = InfoS,
    )
}

@Composable
fun ProfileImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Gray500),
        contentAlignment = Alignment.Center
    ) {
        Text("AI", color = Color.White, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatBotTheme {
        ChatScreen()
    }
}