package com.aitool.plantid.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R

@Composable
fun DiagnoseContent(data: DiagnoseModel) {
    val tabs = listOf("Information", "Treatment")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. TABS CỐ ĐỊNH Ở TRÊN
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = com.aitool.plantid.ui.Green900)
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title, fontSize = 15.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium) },
                    selectedContentColor = Color.Black,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. NỘI DUNG CUỘN ĐƯỢC
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 120.dp), // Tăng padding bottom để cuộn qua khỏi BottomBar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> DiagnosisInfoTab(data)
                1 -> DiagnosisTreatmentTab(data)
            }
        }
    }
}

@Composable
fun DiagnosisInfoTab(data: DiagnoseModel) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(data.diseaseName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text("Scientific Name: ${data.scientificName}", fontSize = 14.sp, color = Color.Gray)
        }
    }

    DetailCard(
        iconRes = R.drawable.ic_toxicity,
        title = "Symptoms",
        content = data.symptoms
    )

    DetailCard(
        iconRes = R.drawable.ic_problems,
        title = "Common causes",
        content = data.causes
    )

    DetailCard(
        iconRes = R.drawable.ic_uses,
        title = "Affected plants",
        content = data.affectedPlants
    )
}

@Composable
fun DiagnosisTreatmentTab(data: DiagnoseModel) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Treatment steps:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Lặp qua danh sách các bước điều trị mà AI trả về
            data.treatmentSteps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(text = "Step ${index + 1}: ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                    Text(text = step, fontSize = 14.sp, color = Color.Black)
                }
            }
        }
    }

    DetailCard(
        iconRes = R.drawable.ic_problems,
        title = "Monitor progress",
        content = data.monitoring
    )

    DetailCard(
        iconRes = R.drawable.ic_toxicity,
        title = "Tips",
        content = data.tips
    )
}