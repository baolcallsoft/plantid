package com.aitool.plantid.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R // Đảm bảo import đúng R của project bạn

// 1. Data Class lưu trữ thông tin ngôn ngữ
data class LanguageData(val name: String, val flagIconRes: Int)

@Composable
fun LanguageScreen(
    onConfirmClick: (String) -> Unit
) {
    // 2. Danh sách ngôn ngữ đúng thứ tự và cú pháp tên nước tiếng Anh như bạn yêu cầu
    val languages = remember {
        listOf(
            LanguageData("Chinese", R.drawable.ic_china),
            LanguageData("Dutch", R.drawable.ic_netherlands),
            LanguageData("English", R.drawable.ic_united_kingdom),
            LanguageData("French", R.drawable.ic_france),
            LanguageData("German", R.drawable.ic_germany),
            LanguageData("Indonesian", R.drawable.ic_indonesia),
            LanguageData("Japanese", R.drawable.ic_japan),
            LanguageData("Korean", R.drawable.ic_south_korea),
            LanguageData("Malay", R.drawable.ic_malaysia),
            LanguageData("Polish", R.drawable.ic_poland),
            LanguageData("Portuguese", R.drawable.ic_portugal),
            LanguageData("Russian", R.drawable.ic_russia),
            LanguageData("Spanish", R.drawable.ic_spain),
            LanguageData("Thai", R.drawable.ic_thailand),
            LanguageData("Vietnamese", R.drawable.ic_vietnam)
        )
    }

    var selectedLanguage by remember { mutableStateOf("English") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Thanh TopBar tuỳ chỉnh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select your language",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                IconButton(
                    onClick = { onConfirmClick(selectedLanguage) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->

        // 3. Danh sách cuộn LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách giữa các ô
        ) {
            items(languages) { language ->
                val isSelected = selectedLanguage == language.name

                // Thẻ (Card) màu trắng bo góc cho mỗi ngôn ngữ
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLanguage = language.name }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Cờ
                        Image(
                            painter = painterResource(id = language.flagIconRes),
                            contentDescription = language.name,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Tên ngôn ngữ
                        Text(
                            text = language.name,
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Icon Circle chọn/không chọn
                        Image(
                            painter = painterResource(
                                id = if (isSelected) R.drawable.ic_circle_green else R.drawable.ic_circle
                            ),
                            contentDescription = if (isSelected) "Selected" else "Unselected",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
