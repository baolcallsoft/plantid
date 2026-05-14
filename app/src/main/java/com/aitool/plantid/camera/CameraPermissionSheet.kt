package com.aitool.plantid.camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R // Nhớ đổi package name cho đúng
import com.aitool.plantid.components.AppBottomSheet
import com.aitool.plantid.ui.Green900

@Composable
fun CameraPermissionSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onAllow: () -> Unit
) {
    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = onDismiss,
        showDragHandle = false // Ẩn drag handle giống hình
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding(), // Đẩy lên để không bị khuất bởi thanh điều hướng
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Icon Camera (Bỏ vòng tròn xám, tăng kích thước để giống ảnh)
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_request),
                contentDescription = "Camera Request",
                tint = Color.Unspecified, // Giữ nguyên màu gốc của file thiết kế
                modifier = Modifier.size(130.dp) // Phóng to icon lên
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Tiêu đề
            Text(
                text = "Allow camera access",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Đoạn mô tả
            Text(
                text = "We need access to your camera to capture plant photos. Give permission from settings",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Hàng chứa 2 nút (Cancel bên trái, Allow bên phải)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Khoảng cách giữa 2 nút
            ) {
                // Nút Cancel (OutlinedButton: Viền xám, nền trắng)
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D1D1)), // Viền màu xám nhạt
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF4A4A4A), // Chữ màu xám đậm
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Nút Allow (Button: Nền xanh, chữ trắng)
                Button(
                    onClick = onAllow,
                    colors = ButtonDefaults.buttonColors(containerColor = Green900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Allow",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}