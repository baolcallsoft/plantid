package com.aitool.plantid.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aitool.plantid.R
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.viewmodel.PlantChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantBotScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    // 🌟 KHAI BÁO CÁC HÀNH ĐỘNG
    onNavigateToChatDetail: (String?, String?) -> Unit,
    onOpenCamera: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: PlantChatViewModel = hiltViewModel()
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("New chat", "Chat history")

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedChatIds.collectAsState()
    val chatHistory by viewModel.chatHistorySessions.collectAsState()

    var showTopMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode && selectedTabIndex == 1) {
                        Text("${selectedIds.size} Selected", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    } else {
                        Text("PlantBot", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) viewModel.setSelectionMode(false) else onBackClick()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    if (selectedTabIndex == 1) {
                        if (isSelectionMode) {
                            val isAllSelected = chatHistory.isNotEmpty() && selectedIds.size == chatHistory.size
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if (isAllSelected) viewModel.selectAllChats(emptySet())
                                        else viewModel.selectAllChats(chatHistory.map { it.sessionId }.toSet())
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("All", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(if (isAllSelected) Green900 else Color.LightGray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Select All", tint = Color.White, modifier = Modifier.size(9.dp))
                                }
                            }
                        } else {
                            Box {
                                IconButton(onClick = { showTopMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Black)
                                }
                                DropdownMenu(
                                    expanded = showTopMenu,
                                    onDismissRequest = { showTopMenu = false },
                                    modifier = Modifier.background(Color.White).width(160.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(painterResource(R.drawable.ic_check_circle), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Select", color = Color.Black, fontSize = 16.sp)
                                            }
                                        },
                                        onClick = { viewModel.setSelectionMode(true); showTopMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(painterResource(R.drawable.ic_trashbin), contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Delete all", color = Color.Red, fontSize = 16.sp)
                                            }
                                        },
                                        onClick = { showTopMenu = false; if (chatHistory.isNotEmpty()) showDeleteAllDialog = true }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.White)
        ) {
            if (!isSelectionMode) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary, height = 2.dp
                        )
                    },
                    divider = { HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected, onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 16.sp, color = if (isSelected) Color.Black else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }
            } else {
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }

            // 🌟 CHIA PHÁT CÁC HÀNH ĐỘNG CHO MÀN HÌNH CON
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> NewChatScreen(
                        navController = navController,
                        onNavigateToChatDetail = onNavigateToChatDetail,
                        onOpenCamera = onOpenCamera
                    )
                    1 -> ChatHistoryScreen(
                        onChatClick = onNavigateToChat,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    com.aitool.plantid.components.AppBottomSheet(
        showSheet = showDeleteAllDialog,
        onDismiss = { showDeleteAllDialog = false },
        containerColor = Color.White,
        showDragHandle = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Delete all chats?", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text("This will permanently delete all your conversations.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showDeleteAllDialog = false }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = Color.DarkGray)
                }
                Button(
                    onClick = {
                        viewModel.selectAllChats(chatHistory.map { it.sessionId }.toSet())
                        viewModel.deleteSelectedChats()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}