package com.aitool.plantid.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aitool.plantid.R
import com.aitool.plantid.components.AppBottomSheet
import com.aitool.plantid.data.ChatSessionEntity
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.viewmodel.PlantChatViewModel

@Composable
fun ChatHistoryScreen(
    onChatClick: (String) -> Unit, // 🌟 Thay vì NavController, giờ chỉ cần nhận 1 hàm
    viewModel: PlantChatViewModel = hiltViewModel()
) {
    val chatHistory by viewModel.chatHistorySessions.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedChatIds.collectAsState()

    var expandedMenuId by remember { mutableStateOf<Int?>(null) }
    var chatToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var singleChatToDelete by remember { mutableStateOf<ChatSessionEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (chatHistory.isEmpty()) {
            EmptyHistoryState()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chatHistory) { chat ->
                    val isSelected = selectedIds.contains(chat.sessionId)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelectionMode) {
                                    viewModel.toggleChatSelection(chat.sessionId)
                                } else {
                                    onChatClick(chat.sessionId.toString())
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chat.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelectionMode) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier.size(16.dp).background(color = Green900, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(9.dp))
                                }
                            } else {
                                Box(modifier = Modifier.size(17.dp).border(width = 1.5.dp, color = Color.LightGray, shape = CircleShape))
                            }
                        } else {
                            Box {
                                IconButton(onClick = { expandedMenuId = chat.sessionId }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                                }

                                DropdownMenu(
                                    expanded = expandedMenuId == chat.sessionId,
                                    onDismissRequest = { expandedMenuId = null },
                                    modifier = Modifier.background(Color.White).width(130.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Icon(painterResource(R.drawable.ic_rename), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Rename", color = Color.Black)
                                            }
                                        },
                                        onClick = { expandedMenuId = null; chatToRename = chat }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Icon(painterResource(R.drawable.ic_trashbin), contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Delete", color = Color.Red)
                                            }
                                        },
                                        onClick = { expandedMenuId = null; singleChatToDelete = chat; showDeleteDialog = true }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (isSelectionMode && selectedIds.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { singleChatToDelete = null; showDeleteDialog = true }
                        .padding(8.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_trashbin), contentDescription = "Delete Selected", tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Delete", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    chatToRename?.let { chat ->
        var newTitle by remember { mutableStateOf(chat.title) }

        AlertDialog(
            onDismissRequest = { chatToRename = null },
            containerColor = Color.White,
            title = { Text("Rename chat", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { chatToRename = null }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.DarkGray)
                    }
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) viewModel.renameChatSession(chat.sessionId, newTitle)
                            chatToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        )
    }

    AppBottomSheet(
        showSheet = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        containerColor = Color.White,
        showDragHandle = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isMultiDelete = singleChatToDelete == null
            val titleText = if (isMultiDelete) "Delete ${selectedIds.size} chats?" else "Delete this chat?"
            val descText = if (isMultiDelete) "These conversations will be permanently deleted." else "This conversation will be permanently deleted."

            Text(text = titleText, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = descText, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = Color.DarkGray)
                }
                Button(
                    onClick = {
                        if (isMultiDelete) viewModel.deleteSelectedChats()
                        else singleChatToDelete?.let { viewModel.deleteChatSession(it.sessionId) }
                        showDeleteDialog = false
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

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_no_chat), contentDescription = "Empty", modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Empty History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "You haven't had any conversation yet", fontSize = 15.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}