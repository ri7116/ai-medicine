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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatbot.features.chatbot.ChatListScreen
import com.example.chatbot.features.chatbot.ChatScreen
import com.example.chatbot.features.pharmacy.PharmacyDetailScreen
import com.example.chatbot.features.pharmacy.PharmacyScreen
import com.example.chatbot.features.pharmacy.PharmacyViewModel
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
    var selectedDestination by rememberSaveable { mutableStateOf(Destination.CHATBOT.route) }
    val pharmacyViewModel: PharmacyViewModel = viewModel()

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
        AppNavHost(
            navController = navController, 
            startDestination = Destination.CHATBOT.route,
            pharmacyViewModel = pharmacyViewModel,
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    pharmacyViewModel: PharmacyViewModel,
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
        composable(Destination.CHATBOT.route) {
            ChatListScreen(navController = navController)
        }
        composable(Destination.PHARMACY.route) {
            PharmacyScreen(navController = navController, viewModel = pharmacyViewModel)
        }
        // ##### 수정된 부분: ID를 전달받는 새로운 상세 화면 라우트 #####
        composable(
            route = "pharmacy_detail/{pharmacyId}",
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pharmacyId = backStackEntry.arguments?.getString("pharmacyId")
            // ViewModel에서 ID를 사용해 약국 정보 찾기
            val pharmacy = pharmacyViewModel.pharmacies.find { it.id == pharmacyId }
            if (pharmacy != null) {
                PharmacyDetailScreen(navController = navController, pharmacy = pharmacy)
            } else {
                // 오류 처리: 이전 화면으로 돌아감
                navController.popBackStack()
            }
        }
        // 새 채팅
        composable("chat_screen") { 
            ChatScreen(navController = navController, chatId = null)
        }
        // 기존 채팅
        composable(
            route = "chat_screen/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            ChatScreen(navController = navController, chatId = chatId)
        }
    }
}
