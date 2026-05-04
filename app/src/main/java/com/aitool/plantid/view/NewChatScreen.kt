package com.aitool.plantid.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aitool.plantid.R
import com.aitool.plantid.ui.Green100
import com.aitool.plantid.ui.Green300
import com.aitool.plantid.ui.Green500
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral100
import com.aitool.plantid.ui.Neutral200
import com.aitool.plantid.ui.Neutral50
import com.aitool.plantid.ui.Neutral500
import com.aitool.plantid.viewmodel.PlantChatViewModel

// 1. Data Class lưu trữ nội dung tin nhắn
data class ChatMessage(val text: String, val isFromUser: Boolean, val isLoading: Boolean = false)

@Composable
fun NewChatScreen(viewModel: PlantChatViewModel = viewModel()) {
    var chatText by remember { mutableStateOf("") }

    // 2. Khởi tạo AI, danh sách tin nhắn và Coroutine để chạy ngầm
    val chatMessages by viewModel.chatMessages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        // 3. PHẦN GIỮA: Hiển thị Bot hoặc Danh sách chat
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatMessages.isEmpty()) {
                // Trạng thái trống: Hiện linh vật Robot
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bot), // Icon đã sửa của bạn
                        contentDescription = "Bot Mascot",
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Hi there! Got questions about your plant?\nI've got answers!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            } else {
                // Trạng thái có chat: Hiện danh sách tin nhắn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatBubble(message = message)
                    }
                }
            }
        }

        // 4. PHẦN ĐÁY: Thanh nhập liệu (Chat Input Bar)
        Row(
            modifier = Modifier
                .padding(bottom = 20.dp, top = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Khung bo tròn chứa Icon Gallery và Ô nhập liệu
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    // Nút chọn ảnh
                    IconButton(onClick = { /* Mở gallery */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_image), // Icon đã sửa của bạn
                            contentDescription = "Gallery",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Ô nhập liệu text
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (chatText.isEmpty()) {
                            Text("Ask about your plant...", color = Color.Gray, fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = chatText,
                            onValueChange = { chatText = it },
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nút Gửi
            Surface(
                modifier = Modifier.size(35.dp),
                color = if (chatText.isNotBlank()) Color(0xFF00C853) else Color(0xFFF0F0F0),
                shape = RoundedCornerShape(24.dp)
            ) {
                IconButton(
                    onClick = {
                        if (chatText.isNotBlank()) {
                            val userMsg = chatText
                            chatText = "" // Xóa ô text

                            // 🔥 Gọi ViewModel xử lý mọi thứ!
                            viewModel.sendMessage(userMsg)
                        }
                    },
                    enabled = chatText.isNotBlank()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_up),
                        contentDescription = "Send",
                        tint = if (chatText.isNotBlank()) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// 5. Component vẽ bong bóng chat (Đã tích hợp Copy)
@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isFromUser) Green900 else Neutral50

    // Khởi tạo trình quản lý bộ nhớ tạm và context
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                // Bo góc nhọn chỉ tay về phía người gửi (phải cho User, trái cho Bot)
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp) // Giới hạn chiều rộng
                .pointerInput(Unit) { // 🔥 Lắng nghe thao tác chạm
                    detectTapGestures(
                        onLongPress = {
                            // Chỉ copy khi không phải là dòng chữ "Typing..."
                            if (!message.isLoading) {
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp,
                // Đổi sang chữ trắng nếu là nền tối của User, và chữ đen cho Bot
                color = if (message.isLoading) Color.Gray
                else if (message.isFromUser) Color.White
                else Color.Black
            )
        }
    }
}