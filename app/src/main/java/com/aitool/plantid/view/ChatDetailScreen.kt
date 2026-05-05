package com.aitool.plantid.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aitool.plantid.R
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral50
import com.aitool.plantid.viewmodel.PlantChatViewModel

data class ChatMessage(val text: String, val isFromUser: Boolean, val isLoading: Boolean = false)

@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String,
    initialMsg: String?,
    viewModel: PlantChatViewModel = hiltViewModel()
) {
    var chatText by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Tự động xử lý khi vừa mở màn hình
    LaunchedEffect(chatId) { 
        if (!initialMsg.isNullOrEmpty()) {
            viewModel.sendMessage(initialMsg)
        } else if (chatId != "-1" && chatId.isNotBlank()) {
            viewModel.loadChatSession(chatId.toInt())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()) {

        // 1. THANH HEADER: Có nút quay lại
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Text("PlantBot", fontSize = 18.sp, color = Color.Black)
        }

        // 2. DANH SÁCH TIN NHẮN
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }
        }

        // 3. THANH NHẬP LIỆU (Giống hệt ở NewChatScreen)
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.weight(1f).height(52.dp), color = Color(0xFFF7F7F7), shape = RoundedCornerShape(26.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                    IconButton(onClick = { /* Mở gallery */ }) {
                        Icon(painterResource(id = R.drawable.ic_add_image), "Gallery", tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                    }
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                        if (chatText.isEmpty()) Text("Ask about your plant...", color = Color.Gray, fontSize = 15.sp)
                        BasicTextField(value = chatText, onValueChange = { chatText = it }, textStyle = TextStyle(fontSize = 15.sp, color = Color.Black), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(48.dp),
                color = if (chatText.isNotBlank()) Color(0xFF00C853) else Color(0xFFF0F0F0),
                shape = RoundedCornerShape(24.dp)
            ) {
                IconButton(
                    onClick = {
                        if (chatText.isNotBlank()) {
                            viewModel.sendMessage(chatText)
                            chatText = ""
                        }
                    },
                    enabled = chatText.isNotBlank()
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_up), "Send", tint = if (chatText.isNotBlank()) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isFromUser) Green900 else Neutral50
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp).pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (!message.isLoading) {
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        ) {
            Text(
                text = message.text, modifier = Modifier.padding(12.dp), fontSize = 15.sp,
                color = if (message.isLoading) Color.Gray else if (message.isFromUser) Color.White else Color.Black
            )
        }
    }
}