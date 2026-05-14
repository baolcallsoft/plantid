package com.aitool.plantid.view

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aitool.plantid.R
import com.aitool.plantid.camera.CameraPermissionSheet
import com.aitool.plantid.components.AppBottomSheet
import com.aitool.plantid.ui.Green100
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral100
import com.aitool.plantid.ui.Neutral50
import com.aitool.plantid.viewmodel.PlantChatViewModel

data class ChatMessage(val text: String, val isFromUser: Boolean, val isLoading: Boolean = false, val imageUri: String? = null)

@Composable
fun ChatDetailScreen(
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    navController: NavController,
    chatId: String,
    initialMsg: String?,
    initialImageUri: String? = null,
    viewModel: PlantChatViewModel = hiltViewModel()
) {
    var chatText by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()

    val activeSessionId by viewModel.currentSessionId.collectAsState()
    val chatHistory by viewModel.chatHistorySessions.collectAsState()
    val currentSession = chatHistory.find { it.sessionId == activeSessionId }
    val displayTitle = currentSession?.title ?: "PlantBot"

    var expandedMenu by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cameraResult by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("camera_result", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    var showPermissionSheet by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onOpenCamera() // ĐÃ SỬA: Dùng hàm thay vì navigate
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    LaunchedEffect(cameraResult) {
        cameraResult?.let { uriString ->
            selectedImageUri = android.net.Uri.parse(uriString)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("camera_result")
        }
    }

    LaunchedEffect(chatId, initialMsg, initialImageUri) {
        val hasInitialContent = !initialMsg.isNullOrEmpty() || !initialImageUri.isNullOrEmpty()

        if (hasInitialContent) {
            val msgToSend = if (initialMsg == "null" || initialMsg.isNullOrEmpty()) "" else initialMsg
            val uriToSend = if (initialImageUri == "null" || initialImageUri.isNullOrEmpty()) null else android.net.Uri.parse(initialImageUri)
            viewModel.sendMessage(msgToSend, uriToSend)
        } else if (chatId != "-1" && chatId.isNotBlank()) {
            viewModel.loadChatSession(chatId.toInt())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) { // ĐÃ SỬA
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }

            Text(
                text = displayTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )

            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Black)
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(Color.White).width(140.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedMenu = false
                                navController.popBackStack("chatbot", inclusive = false) // Khúc này giữ nguyên vì điều hướng rất sâu
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "New chat", tint = Color.Gray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("New chat", color = Color.Black, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedMenu = false
                                activeSessionId?.let { id -> viewModel.deleteChatSession(id) }
                                onBack() // ĐÃ SỬA
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_trashbin), contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete", color = Color.Red, fontSize = 16.sp)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 8.dp)
                .padding(horizontal = 20.dp)
        ) {
            selectedImageUri?.let { uri ->
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_remove), contentDescription = "Remove", tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 140.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(1.dp, Neutral100, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_image),
                        contentDescription = "Gallery",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp).clickable { showAttachmentSheet = true }
                    )

                    Box(modifier = Modifier.padding(horizontal = 12.dp).width(1.dp).height(24.dp).background(Neutral100))

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (chatText.isEmpty()) Text("Typing...", color = Color.Gray, fontSize = 15.sp)
                        BasicTextField(
                            value = chatText,
                            onValueChange = { chatText = it },
                            maxLines = 3,
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (chatText.isNotBlank() || selectedImageUri != null) Color(0xFF00C853) else Color(0xFFF2F2F2),
                            shape = CircleShape
                        )
                        .clickable(enabled = chatText.isNotBlank() || selectedImageUri != null) {
                            if (chatText.isNotBlank() || selectedImageUri != null) {
                                val msgToSend = chatText.ifBlank { "Please analyze this plant image." }
                                viewModel.sendMessage(msgToSend, selectedImageUri)
                                chatText = ""
                                selectedImageUri = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_up),
                        contentDescription = "Send",
                        tint = if (chatText.isNotBlank() || selectedImageUri != null) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    AppBottomSheet(
        showSheet = showAttachmentSheet,
        onDismiss = { showAttachmentSheet = false },
        showDragHandle = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Add a photo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                IconButton(onClick = { showAttachmentSheet = false }, modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
            }

            HorizontalDivider(color = Neutral100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green900)
                    .clickable {
                        showAttachmentSheet = false
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(id = R.drawable.ic_photo), contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose from Gallery", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green100)
                    .clickable {
                        showAttachmentSheet = false
                        val permission = android.Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            onOpenCamera() // ĐÃ SỬA
                        } else {
                            showPermissionSheet = true
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(id = R.drawable.ic_camera), contentDescription = "Camera", tint = Green900, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take a Photo", color = Green900, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    CameraPermissionSheet(
        showSheet = showPermissionSheet,
        onDismiss = { showPermissionSheet = false },
        onAllow = {
            showPermissionSheet = false
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    )
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val boxAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val columnAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromUser) Green900 else Neutral50
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = boxAlignment) {
        Column(horizontalAlignment = columnAlignment) {
            if (!message.imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = "Attached Image",
                    modifier = Modifier
                        .padding(bottom = if (message.text.isNotBlank()) 4.dp else 0.dp)
                        .widthIn(max = 150.dp)
                        .heightIn(max = 150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (message.text.isNotBlank() || message.isLoading) {
                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    ),
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (!message.isLoading) {
                                        clipboardManager.setText(AnnotatedString(message.text))
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 15.sp,
                        color = if (message.isLoading) Color.Gray else if (message.isFromUser) Color.White else Color.Black
                    )
                }
            }
        }
    }
}