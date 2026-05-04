package com.aitool.plantid.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R
import com.aitool.plantid.ui.Neutral100
import com.aitool.plantid.ui.Neutral400
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantBannerSlider() {
    val pagerState = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % 2
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // KHUNG CHÍNH BAO GỒM ẢNH (TRƯỢT) VÀ CHỮ (ĐỨNG IM)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp)) // Bo góc toàn bộ khung
        ) {
            // ==========================================
            // LAYER 1: LỚP ẢNH NỀN (CHỈ TRƯỢT NGANG)
            // ==========================================
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Image(
                    painter = painterResource(
                        id = if (page == 0) R.drawable.slide1 else R.drawable.slide2
                    ),
                    contentDescription = "Banner Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ==========================================
            // LAYER 2: LỚP CHỮ & NÚT (ĐỨNG CỐ ĐỊNH + MỜ DẦN)
            // ==========================================

            // 1. Tính toán phần trăm trượt để ra độ mờ cho từng slide
            val offset0 = (pagerState.currentPage - 0) + pagerState.currentPageOffsetFraction
            val alpha0 = 1f - offset0.absoluteValue.coerceIn(0f, 1f)

            val offset1 = (pagerState.currentPage - 1) + pagerState.currentPageOffsetFraction
            val alpha1 = 1f - offset1.absoluteValue.coerceIn(0f, 1f)

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                // --- CHỮ CỦA SLIDE 1 ---
                if (alpha0 > 0f) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .align(Alignment.CenterStart)
                            .graphicsLayer { alpha = alpha0 },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Don't know\nthis plant?",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,

                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Take a photo to instantly recognize any plant",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { /* Action Snap */ },
                            modifier = Modifier.height(31.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Snap it now!", color = Color.White)
                        }
                    }
                }

                // --- CHỮ CỦA SLIDE 2 ---
                if (alpha1 > 0f) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .align(Alignment.CenterEnd)
                            .graphicsLayer { alpha = alpha1 },
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Diagnose plant\nproblems",
                            color = Neutral100,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Scan damaged leaves to identify issues and find solutions",
                            color = Color(0xFF6B4226),
                            fontSize = 10.sp,
                            textAlign = TextAlign.End,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { /* Action Scan */ },
                            modifier = Modifier.height(31.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB50000)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = "Scan my plant", color = Color.White)
                        }
                    }
                }
            }
        }

        // --- CHẤM ĐIỀU HƯỚNG ---
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val color = if (isSelected) MaterialTheme.colorScheme.primary else Neutral100

                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "dot_width"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(8.dp)
                        .width(width)
                )
            }
        }
    }
}