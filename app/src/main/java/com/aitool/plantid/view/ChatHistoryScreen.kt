package com.aitool.plantid.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aitool.plantid.R
import com.aitool.plantid.data.ChatSessionEntity
import com.aitool.plantid.viewmodel.PlantChatViewModel

data class ChatSession(val id: String, val title: String)

@Composable
fun ChatHistoryScreen(
    navController: NavController,
    viewModel: PlantChatViewModel = hiltViewModel()
) {
    val chatHistory by viewModel.chatHistorySessions.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (chatHistory.isEmpty()) {
            EmptyHistoryState() // Hiển thị màn hình trống
        } else {
            // Truyền danh sách thật xuống
            ChatHistoryList(chatHistory = chatHistory, navController = navController)
        }
    }
}

@Composable
fun ChatHistoryList(chatHistory: List<ChatSessionEntity>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(chatHistory) { chat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("chat_detail?chatId=${chat.sessionId}")
                    }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { /* TODO: Mở menu Xóa / Đổi tên */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Gray
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFF9F9F9),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Căn giữa màn hình
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_no_chat),
            contentDescription = "Empty History",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Empty History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You haven't had any conversation yet",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}