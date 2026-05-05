package com.aitool.plantid.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aitool.plantid.R

@Composable
fun NewChatScreen(navController: NavController) {
    var chatText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        // PHẦN GIỮA: Chỉ có hình con Robot ngồi chờ
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bot),
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

        // PHẦN ĐÁY: Thanh nhập liệu
        Row(
            modifier = Modifier
                .padding(bottom = 20.dp, top = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(26.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
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
                            val msg = chatText
                            chatText = "" // Xóa ô text
                            val encodedMsg = android.net.Uri.encode(msg)
                            navController.navigate("chat_detail?initialMsg=$encodedMsg")
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