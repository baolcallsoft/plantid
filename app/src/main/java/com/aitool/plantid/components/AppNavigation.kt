package com.aitool.plantid.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aitool.plantid.view.LanguageScreen
import com.aitool.plantid.view.PlantBotScreen
import com.aitool.plantid.view.SettingScreen

@Composable
fun AppNavigation(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = "dashboard",

        // 1. Hiệu ứng khi MỞ trang mới (Trang mới trượt từ PHẢI sang TRÁI)
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300) // 300ms là tốc độ chuẩn cực kỳ mượt mà
            )
        },

        // 2. Hiệu ứng của trang cũ khi bị trang mới đè lên (Cũng trượt dạt sang TRÁI)
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },

        // 3. Hiệu ứng khi QUAY LẠI trang cũ bằng nút Back (Trang cũ trượt từ TRÁI sang PHẢI)
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },

        // 4. Hiệu ứng của trang hiện tại khi bị đóng đi (Trang hiện tại trượt sang PHẢI biến mất)
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable("dashboard") {
            MainDashboardScreen(rootNavController, onNavigateToSetting = { rootNavController.navigate("setting") }, onNavigateToChatbot = { rootNavController.navigate("chatbot") })
        }

        composable("setting") {
            SettingScreen(
                onBack = { rootNavController.popBackStack() }, onNavigateToLanguage = { rootNavController.navigate("language") }
            )
        }

        composable("language") {
            LanguageScreen(
                onConfirmClick = { selectedLanguage ->
                    rootNavController.popBackStack()
                }
            )
        }

        composable("chatbot") {
            PlantBotScreen { rootNavController.popBackStack() }
        }
    }
}