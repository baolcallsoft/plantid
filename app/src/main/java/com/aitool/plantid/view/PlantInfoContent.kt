package com.aitool.plantid.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R
import com.aitool.plantid.ui.Green900

@Composable
fun PlantInfoContent(data: PlantModel) { // 🔥 Nhận data
    val tabs = listOf("Basic info", "Warnings & Uses", "Growth & Lifecycle", "Care Guide")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. THANH MENU TABS CỐ ĐỊNH Ở TRÊN CÙNG
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {},
            edgePadding = 20.dp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { selectedTabIndex = index }
                        .padding(vertical = 8.dp)
                )
            }
        }

        // 2. NỘI DUNG CUỘN ĐƯỢC BÊN DƯỚI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 120.dp), // Tăng padding bottom để cuộn qua khỏi BottomBar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> BasicInfoTab(data)
                1 -> WarningsAndUsesTab(data)
                2 -> GrowthTab(data)
                3 -> CareGuideTab(data)
            }
        }
    }
}

@Composable
fun BasicInfoTab(data: PlantModel) {
    // 1. Thẻ thông tin chính (Snake plant...) - Giữ nguyên
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(data.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Row() {
                Text("Scientific Name:", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                Text("${data.scientificName}", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoGridItem(modifier = Modifier.weight(1f), title = "Family", value = data.family)
                InfoGridItem(modifier = Modifier.weight(1f), title = "Plant Type", value = data.plantType)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoGridItem(modifier = Modifier.weight(1f), title = "Origin", value = data.origin)
                InfoGridItem(modifier = Modifier.weight(1f), title = "Toxicity", value = data.toxicity)
            }
        }
    }

    // 2. Thẻ Description - Thay icon generic bằng icon drawable để giống ảnh mẫu
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Thay Icons.Default.Info bằng ic_info (drawable) và dùng Unspecified tint
                Icon(
                    painter = painterResource(id = R.drawable.ic_information), // Cần file drawable ic_info.png màu xanh
                    contentDescription = null,
                    tint = Color.Unspecified, // 🔥 SỬA YÊU CẦU 3: Giữ màu xanh gốc của ảnh
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = data.description, fontSize = 16.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }

    OutlinedButton(
        onClick = { /* Handle Similar Results */ },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Similar results", color = Green900, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            // Tint unspecified cho icon search màu xanh
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = Green900,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_image),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Image", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Placeholder cho danh sách ảnh (LazyRow sau này)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.Transparent)
        )
    }
}

@Composable
fun WarningsAndUsesTab(data: PlantModel) {
    DetailCard(iconRes = R.drawable.ic_toxicity, title = "Toxicity Details", content = data.toxicityDetails)
    DetailCard(iconRes = R.drawable.ic_problems, title = "Common problems", content = data.commonProblems)
    DetailCard(iconRes = R.drawable.ic_uses, title = "Special uses", content = data.specialUses)
}

@Composable
fun GrowthTab(data: PlantModel) {
    DetailCard(iconRes = R.drawable.ic_growth, title = "Growth rate", content = data.growthRate)
    DetailCard(iconRes = R.drawable.ic_height_width, title = "Mature height & width", content = data.matureSize)
    DetailCard(iconRes = R.drawable.ic_flowering, title = "Flowering", content = data.flowering)
    DetailCard(iconRes = R.drawable.ic_propagation, title = "Propagation methods", content = data.propagation)
    DetailCard(iconRes = R.drawable.ic_lifespan, title = "Lifespan", content = data.lifespan)
}

@Composable
fun CareGuideTab(data: PlantModel) {
    DetailCard(iconRes = R.drawable.ic_light, title = "Light", content = data.light)
    DetailCard(iconRes = R.drawable.ic_watering, title = "Watering", content = data.watering)
    DetailCard(iconRes = R.drawable.ic_soil, title = "Soil", content = data.soil)
    DetailCard(iconRes = R.drawable.ic_temperature, title = "Temperature range", content = data.temperature)
    DetailCard(iconRes = R.drawable.ic_humidity, title = "Humidity", content = data.humidity)
    DetailCard(iconRes = R.drawable.ic_fertilizer, title = "Fertilizer", content = data.fertilizer)
}

// ... (Giữ nguyên các hàm InfoGridItem và DetailCard của bạn) ...
@Composable
fun InfoGridItem(modifier: Modifier = Modifier, title: String, value: String) {
    Column(modifier = modifier.padding(end = 8.dp)) {
        Text(text = title, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
fun DetailCard(
    iconRes: Int,
    iconTint: Color = Color.Unspecified,
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconTint, // Dùng tint được truyền vào (Unspecified)
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, fontSize = 16.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}