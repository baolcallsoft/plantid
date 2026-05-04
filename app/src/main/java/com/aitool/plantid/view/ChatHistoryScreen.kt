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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R

// 1. Data Class đại diện cho 1 đoạn chat
data class ChatSession(val id: String, val title: String)

@Composable
fun ChatHistoryScreen() {
    // 2. Tạo dữ liệu giả lập (Sau này bạn sẽ lấy từ ViewModel / Room Database)
    val chatHistory = remember {
        listOf(
            ChatSession("1", "Yellow Leaves on My Plant"),
            ChatSession("2", "Brown Spots on Rose Leaves"),
            ChatSession("3", "Help Me Choose a Balcony Plant"),
            ChatSession("4", "Wilting Monstera"),
            ChatSession("5", "Can I Propagate This?"),
            ChatSession("6", "What's This Mushroom?"),
            ChatSession("7", "Indoor Plants for Low Light")
        )
        // 💡 MẸO TEST: Hãy comment block listOf(...) ở trên lại và mở comment dòng dưới
        // để xem màn hình Trống (Empty State) hoạt động ra sao nhé!
        // emptyList<ChatSession>()
    }

    // 3. Khung chứa chính (Thay đổi UI dựa vào việc list có trống hay không)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (chatHistory.isEmpty()) {
            EmptyHistoryState()
        } else {
            ChatHistoryList(chatHistory = chatHistory)
        }
    }
}

// ==========================================
// THÀNH PHẦN 1: DANH SÁCH LỊCH SỬ (Khi có data)
// ==========================================
@Composable
fun ChatHistoryList(chatHistory: List<ChatSession>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(chatHistory) { chat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // TODO: Click vào thì mở lại đoạn chat cũ (Truyền ID sang màn hình chat)
                    }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f) // Chiếm chỗ đẩy icon dấu 3 chấm sang phải
                )

                IconButton(
                    onClick = { /* TODO: Mở menu Xóa / Đổi tên đoạn chat */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Gray
                    )
                }
            }

            // Đường kẻ mờ ngăn cách giữa các item
            HorizontalDivider(
                color = Color(0xFFF9F9F9),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// ==========================================
// THÀNH PHẦN 2: MÀN HÌNH TRỐNG (Khi chưa có data)
// ==========================================
@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Căn giữa màn hình
    ) {
        // Cần chuẩn bị 1 file ảnh đuôi .png (hoặc vector) cho icon trống này
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