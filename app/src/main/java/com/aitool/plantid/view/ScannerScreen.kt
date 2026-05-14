package com.aitool.plantid.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.viewmodel.ScannerUiState
import com.aitool.plantid.viewmodel.SharedScannerViewModel
import kotlinx.coroutines.delay

@Composable
fun ScannerScreen(
    viewModel: SharedScannerViewModel,
    imageUri: String,
    mode: String,
    onCancel: () -> Unit,
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var progress by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.resetState() // Reset lại state cũ (nếu có) trước khi bắt đầu quét mới
        viewModel.processImage(context, imageUri, mode)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ScannerUiState.Loading -> {
                // Cho % chạy ảo lên 90% rồi ngâm ở đó chờ AI
                while (progress < 90) {
                    delay(40)
                    progress += 1
                }
            }
            is ScannerUiState.Success -> {
                // AI trả lời xong, phi thẳng lên 100%
                progress = 100
                delay(400) // Khựng lại nửa giây cho người dùng nhìn kịp số 100%
                onScanComplete()
            }
            is ScannerUiState.Error -> {
                // Xử lý khi lỗi (Bạn có thể show Toast hoặc Dialog ở đây)
                progress = 0
            }
            else -> {}
        }
    }

    // 🔥 CẬP NHẬT: THUẬT TOÁN QUÉT MỚI (TỪ 0 ĐẾN 2)
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing), // 2s xuống + 2s lên = 4s
            repeatMode = RepeatMode.Restart // Chạy vòng tròn liên tục
        ),
        label = "scan_phase"
    )

    // Tính toán hướng và tọa độ thực tế dựa trên phase
    val isGoingDown = scanPhase <= 1f
    val scanAnim = if (isGoingDown) scanPhase else 2f - scanPhase

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 25.dp)
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(3f / 4f)
            ) {
                // Khoảng cách giữa viền xanh và ảnh (GAP)
                val dynamicGapPadding = 10.dp

                // Ảnh gốc sắc nét
                Box(modifier = Modifier.fillMaxSize().padding(dynamicGapPadding)) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Scanned Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                }

                // Khung viền xanh 4 góc
                ScannerCorners()

                // Tia Laser và vệt sáng xanh
                Box(modifier = Modifier.fillMaxSize().padding(dynamicGapPadding)) {
                    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) {
                        val canvasHeight = size.height
                        val currentY = scanAnim * canvasHeight

                        // 🔥 ĐÃ FIX: VẼ VỆT SÁNG ĐẢO CHIỀU HOÀN HẢO THEO ISGOINGDOWN
                        val trailHeight = 100.dp.toPx()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Green900.copy(alpha = 0.4f)),
                                // startY là điểm bắt đầu (Mờ), endY là điểm dính vào Laser (Đậm)
                                startY = if (isGoingDown) currentY - trailHeight else currentY + trailHeight,
                                endY = currentY
                            ),
                            // topLeft quyết định góc trên cùng bên trái của vùng vẽ
                            topLeft = Offset(0f, if (isGoingDown) currentY - trailHeight else currentY),
                            size = Size(size.width, trailHeight)
                        )

                        // Vẽ vạch Laser ngang
                        drawLine(
                            color = Green900,
                            start = Offset(0f, currentY),
                            end = Offset(size.width, currentY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Scanning ${progress}%...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please hold on while we analyze your photo.\nThis may take a few seconds.",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(160.dp).height(50.dp)
            ) {
                Text("Cancel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ScannerCorners() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cornerLength = 50.dp.toPx()
        val strokeWidth = 2.dp.toPx()
        val cornerRadius = 16.dp.toPx()
        val left = 0f; val top = 0f; val right = size.width; val bottom = size.height
        val path = Path()

        path.moveTo(left, top + cornerLength); path.lineTo(left, top + cornerRadius); path.quadraticBezierTo(left, top, left + cornerRadius, top); path.lineTo(left + cornerLength, top)
        path.moveTo(right - cornerLength, top); path.lineTo(right - cornerRadius, top); path.quadraticBezierTo(right, top, right, top + cornerRadius); path.lineTo(right, top + cornerLength)
        path.moveTo(right, bottom - cornerLength); path.lineTo(right, bottom - cornerRadius); path.quadraticBezierTo(right, bottom, right - cornerRadius, bottom); path.lineTo(right - cornerLength, bottom)
        path.moveTo(left + cornerLength, bottom); path.lineTo(left + cornerRadius, bottom); path.quadraticBezierTo(left, bottom, left, bottom - cornerRadius); path.lineTo(left, bottom - cornerLength)

        drawPath(path = path, color = Green900, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}