package com.aitool.plantid.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aitool.plantid.R
import com.aitool.plantid.data.HistoryEntity
import com.aitool.plantid.ui.*
import com.aitool.plantid.viewmodel.HistoryViewModel
import com.aitool.plantid.viewmodel.SharedScannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onIdentifyClick: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
    sharedViewModel: SharedScannerViewModel = hiltViewModel()
) {
    val historyList by viewModel.historyList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAllHistory() }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Clear All", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // NẾU TRỐNG VÀ KHÔNG SEARCH GÌ THÌ HIỆN MÀN HÌNH TRỐNG
            if (historyList.isEmpty() && searchQuery.isEmpty()) {
                EmptyHistoryContent(onIdentifyClick = onIdentifyClick)
            } else {
                // THANH TÌM KIẾM (SEARCH BAR)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search plant...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Neutral50,
                        unfocusedContainerColor = Neutral50,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Green900
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                // KẾT QUẢ TÌM KIẾM TRỐNG
                if (historyList.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painterResource(R.drawable.ic_no_history), contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No plants found", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Try a different name or check your spelling.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    // DANH SÁCH LỊCH SỬ
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(historyList, key = { it.id }) { item ->
                            HistoryItemRow(
                                item = item,
                                onClick = { 
                                    // Map scanType string back to ResultType
                                    val resultType = when(item.scanType) {
                                        "Mushroom" -> ResultType.MUSHROOM
                                        "Flower" -> ResultType.FLOWER
                                        "Diagnose plant" -> ResultType.DIAGNOSE_PLANT
                                        else -> ResultType.IDENTIFY_PLANT
                                    }
                                    sharedViewModel.loadFromHistory(item.aiJsonData, resultType, item.imageUri)
                                    onNavigateToResult(item.imageUri)
                                },
                                onDelete = { viewModel.deleteHistory(item.id) }
                            )
                            Divider(color = Neutral50, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                        }

                        // DÒNG CHỮ Ở ĐÁY DANH SÁCH
                        item {
                            Text(
                                text = "Only the 50 most recent plants are saved. Older ones\nwill be deleted when the list is full.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: HistoryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ẢNH VUÔNG BO GÓC
        AsyncImage(
            model = item.imageUri,
            contentDescription = item.plantName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Neutral100)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // CHI TIẾT
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.plantName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTimestamp(item.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                // HIỂN THỊ TAG MÀU SẮC
                ScanTypeTag(scanType = item.scanType)
            }
        }

        // NÚT 3 CHẤM (Để xóa)
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "Delete", tint = Color.Gray)
        }
    }
}

// Hàm hỗ trợ vẽ Tag màu sắc
@Composable
fun ScanTypeTag(scanType: String) {
    // Xác định màu chữ và màu nền dựa vào tên loại quét
    val (bgColor, textColor) = when (scanType) {
        "Mushroom" -> Pair(Mushroom.copy(alpha = 0.15f), Mushroom)
        "Flower" -> Pair(Flower.copy(alpha = 0.15f), Flower)
        "Diagnose plant" -> Pair(Diagnose.copy(alpha = 0.15f), Diagnose)
        else -> Pair(Green900.copy(alpha = 0.15f), Green900) // Mặc định là Identify plant
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = scanType,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Hàm hỗ trợ format Date
fun formatTimestamp(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timeInMillis))
}

@Composable
fun EmptyHistoryContent(onIdentifyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_history),
            contentDescription = "No history",
            tint = Color.Unspecified, // Dùng ảnh drawable gốc nếu có
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "No history yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your recent plant scans will be saved here.",
            fontSize = 15.sp, color = Color.Gray, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIdentifyClick,
            colors = ButtonDefaults.buttonColors(containerColor = Green900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(200.dp).height(50.dp)
        ) {
            Text("Identify plant", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}