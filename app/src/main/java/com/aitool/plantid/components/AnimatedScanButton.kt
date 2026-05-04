package com.aitool.plantid.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.aitool.plantid.ui.LeftButtonBursh

@Composable
fun AnimatedScanButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_transition")

    val lineOffsetY by infiniteTransition.animateFloat(
        initialValue = -9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line_offset"
    )

    val gradientColorsButton = listOf(LeftButtonBursh, MaterialTheme.colorScheme.primary)
    val buttonGradient = Brush.horizontalGradient(colors = gradientColorsButton)

    Box(
        modifier = Modifier
            .size(57.dp) // Kích thước nút giữ nguyên 50.dp
            .clip(CircleShape)
            .background(buttonGradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val strokeW = 2.dp.toPx()
            val cornerLen = 6.dp.toPx()
            val color = Color.White

            // --- VẼ 4 GÓC TĨNH ---
            // Góc Trái - Trên
            drawLine(color, Offset(0f, cornerLen), Offset(0f, 0f), strokeW, cap = StrokeCap.Round)
            drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW, cap = StrokeCap.Round)

            // Góc Phải - Trên
            drawLine(color, Offset(size.width - cornerLen, 0f), Offset(size.width, 0f), strokeW, cap = StrokeCap.Round)
            drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeW, cap = StrokeCap.Round)

            // Góc Trái - Dưới
            drawLine(color, Offset(0f, size.height - cornerLen), Offset(0f, size.height), strokeW, cap = StrokeCap.Round)
            drawLine(color, Offset(0f, size.height), Offset(cornerLen, size.height), strokeW, cap = StrokeCap.Round)

            // Góc Phải - Dưới
            drawLine(color, Offset(size.width - cornerLen, size.height), Offset(size.width, size.height), strokeW, cap = StrokeCap.Round)
            drawLine(color, Offset(size.width, size.height - cornerLen), Offset(size.width, size.height), strokeW, cap = StrokeCap.Round)

            // --- VẼ THANH NGANG ĐỘNG ---
            val centerY = size.height / 2
            drawLine(
                color = color,
                start = Offset(0f, centerY + lineOffsetY.dp.toPx()),
                end = Offset(size.width, centerY + lineOffsetY.dp.toPx()),
                // Giảm độ dày của thanh quét ngang một chút cho tinh tế
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}