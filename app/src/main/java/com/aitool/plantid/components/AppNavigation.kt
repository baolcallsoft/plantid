package com.aitool.plantid.components

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aitool.plantid.view.*
import com.aitool.plantid.viewmodel.ScannerUiState
import com.aitool.plantid.viewmodel.SharedScannerViewModel

// --- BỘ ĐIỀU HƯỚNG TẬP TRUNG ---
class PlantIDActions(navController: NavHostController) {
    // Chuyển sang Chat Detail mới (có đính kèm dữ liệu)
    val navigateToChatDetail: (String?, String?) -> Unit = { msg, uri ->
        val encodedMsg = Uri.encode(msg ?: "")
        val encodedUri = Uri.encode(uri ?: "")
        navController.navigate("chat_detail?initialMsg=$encodedMsg&initialImageUri=$encodedUri")
    }

    // Mở lại một lịch sử Chat đã có
    val navigateToExistingChat: (String) -> Unit = { chatId ->
        navController.navigate("chat_detail?chatId=$chatId")
    }

    // Mở Camera với Mode cụ thể
    val navigateToCamera: (CameraEntryMode) -> Unit = { mode ->
        navController.navigate("camera_screen?entryMode=${mode.name}")
    }

    val navigateToSetting: () -> Unit = { navController.navigate("setting") }
    val navigateToLanguage: () -> Unit = { navController.navigate("language") }
    val popBackStack: () -> Unit = { navController.popBackStack() }
    val navigateToHistory: () -> Unit = { navController.navigate("history")}
}

@Composable
fun AppNavigation(rootNavController: NavHostController) {
    val actions = remember(rootNavController) { PlantIDActions(rootNavController) }
    val sharedScannerViewModel: SharedScannerViewModel = hiltViewModel()

    NavHost(
        navController = rootNavController,
        startDestination = "dashboard",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        composable("dashboard") {
            MainDashboardScreen(
                rootNavController,
                onNavigateToSetting = { actions.navigateToSetting() },
                onNavigateToChatbot = { rootNavController.navigate("chatbot") },
                onOpenCamera = { mode -> actions.navigateToCamera(mode) },
                onNavigateToHistory = { actions.navigateToHistory() }
            )
        }

        composable("setting") {
            SettingScreen(onBack = { actions.popBackStack() }, onNavigateToLanguage = { actions.navigateToLanguage() })
        }

        composable("language") {
            LanguageScreen(onConfirmClick = { actions.popBackStack() })
        }

        composable("new_chat") {
            NewChatScreen(
                navController = rootNavController, // Vẫn truyền để hứng ảnh
                onNavigateToChatDetail = actions.navigateToChatDetail,
                onOpenCamera = { actions.navigateToCamera(CameraEntryMode.CHAT_ATTACHMENT) }
            )
        }

        composable("chatbot") {
            PlantBotScreen(
                navController = rootNavController,
                onBackClick = { actions.popBackStack() },
                onNavigateToChatDetail = actions.navigateToChatDetail,
                onOpenCamera = { actions.navigateToCamera(CameraEntryMode.CHAT_ATTACHMENT) },
                onNavigateToChat = actions.navigateToExistingChat
            )
        }

        composable(
            route = "camera_screen?entryMode={entryMode}",
            arguments = listOf(navArgument("entryMode") { type = NavType.StringType; defaultValue = CameraEntryMode.IDENTIFY_PLANT.name })
        ) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("entryMode") ?: CameraEntryMode.IDENTIFY_PLANT.name
            val mode = try { CameraEntryMode.valueOf(modeStr) } catch (e: Exception) { CameraEntryMode.IDENTIFY_PLANT }

            CameraScreen(
                navController = rootNavController,
                entryMode = mode
            )
        }

        composable("history") {
            HistoryScreen(
                onBackClick = { actions.popBackStack() },
                onIdentifyClick = {
                    actions.navigateToCamera(CameraEntryMode.IDENTIFY_PLANT)
                },
                onNavigateToResult = { uri ->
                    val encodedUri = Uri.encode(uri)
                    rootNavController.navigate("result_screen?imageUri=$encodedUri")
                }
            )
        }

        composable(
            route = "scanner_screen?imageUri={imageUri}&mode={mode}",
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = CameraEntryMode.IDENTIFY_PLANT.name }
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: CameraEntryMode.IDENTIFY_PLANT.name

            ScannerScreen(
                viewModel = sharedScannerViewModel,
                imageUri = uri,
                mode = mode,
                onCancel = { rootNavController.popBackStack() },
                onScanComplete = {
                    rootNavController.navigate("result_screen?imageUri=$uri")
                }
            )
        }

        composable("result_screen?imageUri={imageUri}") { backStackEntry ->
            val uiState = sharedScannerViewModel.uiState.collectAsState().value
            val uri = backStackEntry.arguments?.getString("imageUri") ?: ""

            if (uiState is ScannerUiState.Success) {
                ResultScreen(
                    type = uiState.resultType, // PLANT hay DIAGNOSE
                    jsonString = uiState.jsonResult,
                    imageUri = uiState.imageUri ?: uri, // Ưu tiên imageUri từ Success state
                    onClose = {
                        sharedScannerViewModel.resetState()
                        rootNavController.popBackStack("camera_screen?entryMode={entryMode}", inclusive = false)
                    }
                )
            }
        }

        composable(
            route = "chat_detail?chatId={chatId}&initialMsg={initialMsg}&initialImageUri={initialImageUri}",
            arguments = listOf(
                navArgument("chatId") { defaultValue = "-1" },
                navArgument("initialMsg") { nullable = true; defaultValue = null },
                navArgument("initialImageUri") { nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "-1"
            val initialMsg = backStackEntry.arguments?.getString("initialMsg")
            val initialImageUri = backStackEntry.arguments?.getString("initialImageUri")

            ChatDetailScreen(
                onBack = { actions.popBackStack() },
                onOpenCamera = { actions.navigateToCamera(CameraEntryMode.CHAT_ATTACHMENT) },
                chatId = chatId,
                initialMsg = initialMsg,
                initialImageUri = initialImageUri,
                navController = rootNavController
            )
        }
    }
}