package com.aitool.plantid.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R
import com.aitool.plantid.ui.* // Đảm bảo import Green400, Green900, v.v.
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantBannerSlider() {
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Tự động chạy slider
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp) // Tăng nhẹ chiều cao tổng thể để thoáng hơn
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            // LAYER 1: ẢNH NỀN
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Image(
                    painter = painterResource(
                        id = if (page == 0) R.drawable.slide1 else R.drawable.slide2
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // LAYER 2: CHỮ & NÚT
            val offset0 = (pagerState.currentPage - 0) + pagerState.currentPageOffsetFraction
            val alpha0 = 1f - offset0.absoluteValue.coerceIn(0f, 1f)

            val offset1 = (pagerState.currentPage - 1) + pagerState.currentPageOffsetFraction
            val alpha1 = 1f - offset1.absoluteValue.coerceIn(0f, 1f)

            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {

                // --- SLIDE 1: SNAP IT NOW ---
                if (alpha0 > 0.01f) {
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
                            fontSize = 20.sp, // 👉 Đúng 20sp
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Take a photo to instantly\nrecognize any plant",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp, // 👉 Đúng 10sp
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // NÚT GRADIENT (BRUSH)
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Green400, Green900)
                                    )
                                )
                                .clickable { /* Action */ }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Snap it now!",
                                color = Color.White,
                                fontSize = 12.sp, // 👉 Đúng 12sp
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- SLIDE 2: DIAGNOSE ---
                if (alpha1 > 0.01f) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .align(Alignment.CenterEnd)
                            .graphicsLayer { alpha = alpha1 },
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Diagnose plant\nproblems",
                            color = TextBrownLarge,
                            fontSize = 20.sp, // 👉 Đúng 20sp
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan damaged leaves to identify\nissues and find solutions",
                            color = TextBrownSmall,
                            fontSize = 10.sp, // 👉 Đúng 10sp
                            textAlign = TextAlign.End,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { /* Action */ },
                            modifier = Modifier.height(40.dp), // 👉 Chiều cao đồng bộ
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB50000)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            Text(
                                text = "Scan my plant",
                                color = Color.White,
                                fontSize = 12.sp, // 👉 Đúng 12sp
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ĐIỂM INDICATOR
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val width by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 7.dp,
                    animationSpec = tween(300),
                    label = ""
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Green900 else Neutral100)
                        .height(7.dp)
                        .width(width)
                )
            }
        }
    }
}