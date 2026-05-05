package com.aitool.plantid.components

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aitool.plantid.view.ChatDetailScreen
import com.aitool.plantid.view.ChatHistoryScreen
import com.aitool.plantid.view.LanguageScreen
import com.aitool.plantid.view.NewChatScreen
import com.aitool.plantid.view.PlantBotScreen
import com.aitool.plantid.view.SettingScreen

@Composable
fun AppNavigation(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = "dashboard", // 🔥 Bắt đầu bằng chuỗi thuần túy như cũ
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        composable("dashboard") {
            MainDashboardScreen(
                rootNavController,
                onNavigateToSetting = { rootNavController.navigate("setting") },
                // Khi bấm nút mở Chatbot, đi đến trang NewChat
                onNavigateToChatbot = { rootNavController.navigate("chatbot") }
            )
        }

        composable("setting") {
            SettingScreen(onBack = { rootNavController.popBackStack() }, onNavigateToLanguage = { rootNavController.navigate("language") })
        }

        composable("language") {
            LanguageScreen(onConfirmClick = { rootNavController.popBackStack() })
        }

        // 1. Màn hình Cò mồi
        composable("new_chat") {
            NewChatScreen(navController = rootNavController)
        }

        // 2. Màn hình Lịch sử
        composable("chat_history") {
            ChatHistoryScreen(navController = rootNavController)
        }

        composable("chatbot") {
            PlantBotScreen(
                navController = rootNavController, // Truyền vào đây
                onBackClick = { rootNavController.popBackStack() }
            )
        }

        // 3. Màn hình Chat Chi tiết (Có gắn biến trên link)
        composable(
            route = "chat_detail?chatId={chatId}&initialMsg={initialMsg}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType; defaultValue = "-1" },
                navArgument("initialMsg") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "-1"
            val initialMsg = backStackEntry.arguments?.getString("initialMsg")

            ChatDetailScreen(
                navController = rootNavController,
                chatId = chatId,
                initialMsg = initialMsg
            )
        }
    }
}