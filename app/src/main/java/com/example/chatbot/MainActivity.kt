package com.example.chatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatbot.features.chatbot.ChatListScreen
import com.example.chatbot.features.chatbot.ChatScreen
import com.example.chatbot.features.pharmacy.PharmacyScreen
import com.example.chatbot.features.search.SearchScreen
import com.example.chatbot.ui.theme.ChatBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatBotTheme {
                MainScreen()
            }
        }
    }
}

enum class Destination(
    val route: String,
    val icon: ImageVector,
    val contentDescription: String,
    val label: String
) {
    SEARCH("search", Icons.Default.Search, "검색", "검색"),
    CHATBOT("chatbot", Icons.Default.SmartToy, "챗봇", "챗봇"),
    PHARMACY("pharmacy", Icons.Default.LocalPharmacy, "약국", "약국"),
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // 앱의 전체 네비게이션을 관리하는 NavController
    // 이 NavController는 하단 탭 이동과 화면 간 이동을 모두 제어합니다.
    var selectedDestination by rememberSaveable { mutableStateOf(Destination.CHATBOT.route) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination.route,
                        onClick = {
                            selectedDestination = destination.route
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        // AppNavHost에 NavController와 시작 지점을 전달합니다.
        AppNavHost(
            navController = navController, 
            startDestination = Destination.CHATBOT.route, // 시작 화면을 챗봇 목록으로 변경
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destination.SEARCH.route) {
            SearchScreen()
        }
        // '챗봇' 탭의 시작 화면을 ChatListScreen으로 변경합니다.
        composable(Destination.CHATBOT.route) {
            ChatListScreen(navController = navController)
        }
        composable(Destination.PHARMACY.route) {
            PharmacyScreen()
        }
        // 1:1 대화 화면을 위한 새로운 경로를 만듭니다.
        composable("chat_screen") { 
            ChatScreen(navController = navController)
        }
    }
}
