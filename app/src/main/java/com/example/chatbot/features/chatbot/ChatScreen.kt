package com.example.chatbot.features.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavHostController
import com.example.chatbot.ui.theme.ChatBotTheme
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessageUiModel(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

fun Long.formatTime(): String {
    val sdf = SimpleDateFormat("a h:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray500 = Color(0xFF9E9E9E)
val Gray900 = Color(0xFF212121)

val TextSRegular = TextStyle(fontSize = 16.sp, color = Gray900)
val InfoS = TextStyle(fontSize = 12.sp, color = Gray500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {

    val generativeModel = remember {
        Firebase.vertexAI.generativeModel("gemini-1.5-flash-latest")
    }
    val chat = remember { generativeModel.startChat() }

    val scope = rememberCoroutineScope()
    var textInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessageUiModel>() }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(ChatMessageUiModel("안녕하세요! 무엇을 도와드릴까요?", isUser = false))
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("AI 챗봇") },
                windowInsets = WindowInsets(0.dp), // 상태 표시줄 영역 무시
                navigationIcon = { // 뒤로가기 버튼 추가
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .navigationBarsPadding(),
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
                        messages.add(ChatMessageUiModel(userMessage, isUser = true))
                        textInput = ""

                        scope.launch {
                            try {
                                val response = chat.sendMessage(userMessage)
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            reverseLayout = true, // 아래부터 쌓이도록 설정
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(messages.reversed()) { message -> // 리스트를 뒤집어서 최신 메시지가 아래로 오도록 함
                ChatBubble(chatMessage = message)
            }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(0) // 뒤집힌 리스트의 맨 위(화면상 맨 아래)로 스크롤
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
            TimeText(time = chatMessage.timestamp.formatTime())
            Spacer(modifier = Modifier.width(8.dp))
            MessageBox(
                message = chatMessage.message,
                isUser = true,
            )
        } else {
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
