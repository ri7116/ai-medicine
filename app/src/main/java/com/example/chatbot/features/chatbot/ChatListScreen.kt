package com.example.chatbot.features.chatbot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.chatbot.ui.theme.ChatBotTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    chatListViewModel: ChatListViewModel = viewModel()
) {
    val chatRooms by chatListViewModel.chatRooms.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("대화 목록") },
                windowInsets = WindowInsets(0.dp), // 상태 표시줄 영역 무시
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController?.navigate("chat_screen") }) {
                Icon(Icons.Default.Add, contentDescription = "새 대화 시작")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatRooms) { room ->
                MessageCard(
                    title = room.title,
                    content = room.lastMessage,
                    date = room.timestamp.toFormattedDate(),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { navController?.navigate("chat_screen/${room.id}") } // 클릭 시 해당 채팅방으로 이동
                )
            }
        }
    }
}

// Long 타임스탬프를 "MM/dd" 형식의 문자열로 변환하는 확장 함수
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    return sdf.format(Date(this))
}

@Composable
fun MessageCard(title: String, content: String, date: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
    ){
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = content)
            Text(text = date)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    ChatBotTheme {
        ChatListScreen()
    }
}
