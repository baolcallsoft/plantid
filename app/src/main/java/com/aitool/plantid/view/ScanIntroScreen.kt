package com.aitool.plantid.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R
import com.aitool.plantid.ui.Green200
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral100
import com.aitool.plantid.ui.Neutral200
import kotlinx.coroutines.delay

@Composable
fun ScanIntroScreen(onContinueClick: () -> Unit) {
    var isScanCompleted by remember { mutableStateOf(false) }
    var isScanningDown by remember { mutableStateOf(true) }
    val scanProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Vòng lặp vô tận
        while (true) {
            // 1. Trạng thái bắt đầu
            isScanCompleted = false
            isScanningDown = true
            scanProgress.snapTo(0f)

            // 2. Quét từ trên xuống (1.5s)
            scanProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )

            // 3. Quét từ dưới lên (1.5s)
            isScanningDown = false
            scanProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )

            // 4. Quét xong, hiện bảng thông tin
            isScanCompleted = true

            // 5. Dừng lại 2.5 giây để người dùng xem kết quả, sau đó lặp lại
            delay(2500)
        }
    }

    // Dùng Scaffold để màu nền tràn lên dải Status bar
    Scaffold(
        containerColor = Color(0xFFE8F5E9) // Màu xanh nền nhạt
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Đảm bảo content không bị che bởi status bar
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Identify Any Plant",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            // Khung chứa tổng thể (Viền dày)
            Box(
                modifier = Modifier
                    .height(450.dp)
                    .width(250.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(10.dp, Green200, RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Dùng Column để chia lô. Khi bảng Info hiện, ảnh cây sẽ tự bị đẩy lên.
                Column(modifier = Modifier.fillMaxSize()) {

                    // Lô 1: Khu vực quét ảnh (Có weight(1f) để co giãn động)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.plant_base),
                            contentDescription = "Plant",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Hiệu ứng thanh quét (Chỉ nằm đè lên ảnh cây)
                        if (!isScanCompleted) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val scanLineHeight = 100.dp
                                val maxOffsetY = maxHeight - scanLineHeight

                                // Đảo chiều Gradient dựa vào hướng chạy
                                val gradientColors = if (isScanningDown) {
                                    // Xuống: Đuôi mờ ở trên, vạch đậm ở dưới
                                    listOf(Color.Transparent, Color(0x6600C853), Color(0xFF00C853))
                                } else {
                                    // Lên: Vạch đậm ở trên, đuôi mờ ở dưới
                                    listOf(Color(0xFF00C853), Color(0x6600C853), Color.Transparent)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(scanLineHeight)
                                        .offset(y = maxOffsetY * scanProgress.value)
                                        .background(Brush.verticalGradient(colors = gradientColors))
                                )
                            }
                        }
                    }

                    // Lô 2: Bảng Info
                    AnimatedVisibility(
                        visible = isScanCompleted,
                        enter = fadeIn(tween(400)) + slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(400)
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plant_info_card),
                            contentDescription = "Plant Info",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green900
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(width = 16.dp, height = 6.dp).clip(RoundedCornerShape(50)).background(Green900))
                Box(modifier = Modifier.size(width = 6.dp, height = 6.dp).clip(RoundedCornerShape(50)).background(Neutral100))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScanIntroScreen() {
    ScanIntroScreen(onContinueClick = {})
}