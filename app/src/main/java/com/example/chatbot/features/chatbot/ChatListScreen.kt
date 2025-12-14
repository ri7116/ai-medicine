package com.example.chatbot.features.chatbot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatbot.ui.theme.ChatBotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("대화 목록") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MessageCard(
                id = 1,
                title = "이소티논의 효능",
                content = "이소티논의 효과가 어떻게 됨?",
                date = "10/31",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            MessageCard(
                id = 2,
                title = "오늘 점심 메뉴 추천",
                content = "근처 맛집 좀 알려줘",
                date = "11/01",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            MessageCard(
                id = 3,
                title = "주말 계획",
                content = "이번 주말에 뭐할지 고민 중",
                date = "11/02",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            MessageCard(
                id = 4,
                title = "코틀린 질문",
                content = "Composable 함수가 정확히 뭐야?",
                date = "11/03",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun MessageCard(id: Int, title: String, content: String, date: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
