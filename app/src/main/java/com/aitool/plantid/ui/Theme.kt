package com.aitool.plantid.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Bảng màu cho giao diện Sáng (Light Mode)
private val LightColors = lightColorScheme(
    primary = Green900,        // Màu xanh chủ đạo của app (Mapping từ GreenPrimary cũ)
    onPrimary = Color.White,   // Màu chữ/icon nằm đè lên màu primary

    background = Neutral50,    // Màu nền xám nhạt (Mapping từ BackgroundLight)
    onBackground = Neutral900, // Màu chữ/icon nằm trên nền (Đen sậm)

    surface = Color.White,     // Màu nền của các khối Card, Box, Dialog (Thường là trắng)
    onSurface = Neutral900,    // Màu chữ nằm trên Card

    error = Diagnose,          // Màu đỏ báo lỗi
    onError = Color.White
)

// 2. Bảng màu cho giao diện Tối (Dark Mode) - Dành cho tương lai
private val DarkColors = darkColorScheme(
    primary = Green500,        // Dùng màu xanh sáng hơn một chút để nổi bật trên nền đen
    onPrimary = Neutral900,

    background = Neutral900,   // Nền app màu Đen/Xám cực đậm
    onBackground = Neutral50,  // Chữ trên nền màu Xám nhạt

    surface = Neutral800,      // Nền của Card màu Xám đậm (sáng hơn background 1 chút)
    onSurface = Neutral50,

    error = Diagnose,
    onError = Neutral900
)

// 3. Hàm Theme chính để bọc toàn bộ ứng dụng
@Composable
fun PlantIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Biến này nhận diện máy đang bật Dark/Light
    content: @Composable () -> Unit
) {
    /* * HIỆN TẠI: Đang ép ứng dụng luôn chạy ở chế độ Sáng (LightColors)
     * dù máy người dùng có đang bật Dark Mode hay không.
     * * TƯƠNG LAI: Khi nào bạn muốn làm Dark Mode, hãy XÓA dòng ép cứng bên dưới,
     * và BỎ COMMENT dòng `val colorScheme = if...`
     */

    // val colorScheme = if (darkTheme) DarkColors else LightColors
    val colorScheme = LightColors // Ép cứng dùng Light Mode

    MaterialTheme(
        colorScheme = colorScheme,
        // typography = Typography, // Bỏ comment nếu bạn có file Type.kt để custom font chữ
        content = content
    )
}